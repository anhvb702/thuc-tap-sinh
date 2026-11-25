# Gọi Jev (TypeSafe System One) qua HTTP.
# Key đọc từ biến môi trường TYPESAFE_API_KEY, không in ra màn hình.
#
# Kiểm tra key:      .\scripts\jev.ps1 -Models
# Gửi câu hỏi:       .\scripts\jev.ps1 -RequestFile .\scripts\jev_request.json
#                    .\scripts\jev.ps1 -RequestFile req.json -OutFile result.json
#
# Định dạng request (đã kiểm chứng với API, xem scripts/jev_request.example.json):
# { "state": "...", "model": "jev-latest", "questions": { "<tên>": { ... } } }
#   choice: { "type": "choice", "question": "...", "choices": [...], "criteria": { "<lựa chọn>": "<mô tả>" } }
#   score:  { "type": "score", "question": "...", "criteria": ["mức 0", "mức 1", ...] }  (criteria là danh sách)
#   noul:   { "type": "noul", "question": "...", "instructions": "..." }                 (bắt buộc instructions hoặc criteria)

param(
    [Parameter(Mandatory = $false)]
    [string]$RequestFile = "",

    [Parameter(Mandatory = $false)]
    [string]$OutFile = "",

    [Parameter(Mandatory = $false)]
    [switch]$Models,
    [switch]$Verify,
    [switch]$SkipGradle,
    [string]$ReportFile = "docs/PROOF_REPORT.md"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

if ($Verify) {
    $errors = @()
    $branch = $head = 'UNKNOWN'
    $ownership = 'REVIEW REQUIRED'
    $status = $stat = @()
    $gradle = 'SKIPPED'
    $lint = 'SKIPPED (no changed copy files)'
    Push-Location (Split-Path -Parent $PSScriptRoot)
    try {
        $reportPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($ReportFile)
        $gitOk = $false
        try {
            function Read-VerifyGit {
                $output = & git -c core.quotepath=false @args
                if ($LASTEXITCODE -ne 0) { throw "git $args exited with code $LASTEXITCODE." }
                $output
            }
            $branch = Read-VerifyGit rev-parse --abbrev-ref HEAD
            $head = Read-VerifyGit rev-parse --short HEAD
            $status = @(Read-VerifyGit status --short)
            $stat = @(Read-VerifyGit diff --stat)
            $staged = @(Read-VerifyGit diff --cached --name-only)
            $untracked = @(Read-VerifyGit ls-files --others --exclude-standard)
            $changed = @(Read-VerifyGit diff --name-only) + $staged + $untracked
            $gitOk = $true
            $ownership = 'PASS (0 local config staged)'
            if ($staged -contains 'gradle.properties') {
                $ownership = 'BLOCKED (local config staged)'
                $errors += 'gradle.properties is staged'
            }
        }
        catch { $errors += $_.Exception.Message }

        if (-not $SkipGradle) {
            $gradle = 'FAIL'
            try {
                if (-not $env:JAVA_HOME) { throw 'Missing JAVA_HOME environment variable' }
                if (-not $env:ANDROID_HOME -and $env:LOCALAPPDATA) {
                    $androidSdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
                    if (Test-Path -LiteralPath $androidSdk -PathType Container) {
                        $env:ANDROID_HOME = $androidSdk
                    }
                }
                $gradleOutput = & {
                    $ErrorActionPreference = 'Continue'
                    & .\gradlew.bat :shared:testDebugUnitTest :composeApp:testDebugUnitTest --no-configuration-cache --max-workers=1 2>&1
                }
                $gradleExit = $LASTEXITCODE
                if ($gradleExit -ne 0) {
                    $errors += "Gradle exited with code $gradleExit."
                    $errors += @($gradleOutput | ForEach-Object { "$_" } |
                        Where-Object { $_ -match '(?i)error|failed|exception|^e:|^> ' } | Select-Object -Last 3)
                }
                $tests = $failed = $skipped = 0
                $reportsOk = $true
                foreach ($module in 'shared', 'composeApp') {
                    $resultsPath = "$module/build/test-results/testDebugUnitTest"
                    $reports = @(if (Test-Path -LiteralPath $resultsPath) {
                        Get-ChildItem -LiteralPath $resultsPath -Filter 'TEST-*.xml' -File
                    })
                    if (-not $reports.Count) {
                        $reportsOk = $false
                        $errors += "No test XML for $module."
                    }
                    foreach ($report in $reports) {
                        $suite = ([xml](Get-Content -LiteralPath $report.FullName -Raw -Encoding UTF8)).testsuite
                        $tests += [int]$suite.tests
                        $failed += [int]$suite.failures + [int]$suite.errors
                        $skipped += [int]$suite.skipped
                    }
                }
                if ($gradleExit -eq 0 -and $reportsOk -and $tests -gt 0 -and $failed -eq 0 -and $skipped -eq 0) {
                    $gradle = 'PASS'
                }
                $gradle += " ($tests tests, $failed failed, $skipped skipped)"
                if ($gradleExit -ne 0) { $gradle += ' (XML may be from an earlier run)' }
            }
            catch { $errors += $_.Exception.Message }
        }

        if ($gitOk) {
            $changedCopyFiles = @($changed | Sort-Object -Unique | Where-Object {
                $_ -match '\.(kt|md|txt)$' -and $_ -notin @('gradle.properties', 'docs/PROOF_REPORT.md') -and
                $_ -notlike '.agents/teamwork/*' -and
                (Test-Path -LiteralPath $_ -PathType Leaf) -and
                [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $_)) -ne $reportPath
            })
            try {
                if ($changedCopyFiles.Count) {
                    $lintOutput = & .\scripts\lint-copy.ps1 -Path $changedCopyFiles
                    $lint = 'PASS'
                    if ($LASTEXITCODE -ne 0) {
                        $lint = 'FAIL'
                        $errors += @($lintOutput)
                    }
                }
            }
            catch { $lint = 'FAIL'; $errors += $_.Exception.Message }
        }
        else { $lint = 'SKIPPED (Git check failed)' }

        $ready = $errors.Count -eq 0 -and $gradle.StartsWith('PASS')
        $conclusion = if ($ready) {
            'STATIC_GATE_PASS (can du Mutation + Review cheo + Visual Verify truoc khi merge)'
        } else { 'BLOCKED' }
        $lines = @('# Proof Report', "- Branch: $branch / HEAD: $head", "- File Ownership: $ownership",
            "- Gradle: $gradle", "- Lint-copy: $lint")
        if ($errors.Count) {
            $lines += '## Errors'
            $lines += @(($errors -join "`n") -split '\r?\n|\r' | Select-Object -First 5)
        }
        $lines += '## git status --short'
        $lines += @($status | Select-Object -First 5)
        $lines += '## git diff --stat'
        $lines += @($stat | Select-Object -First 5)
        if ($status.Count -gt 5 -or $stat.Count -gt 5) { $lines += '(Git output shortened.)' }
        $lines = @(($lines -join "`n") -split '\r?\n|\r' | Select-Object -First 24)
        $lines += "- Conclusion: $conclusion"
        $null = [System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($reportPath))
        [System.IO.File]::WriteAllLines($reportPath, [string[]]$lines, [System.Text.UTF8Encoding]::new($false))
        Write-Output "Report: $reportPath"
        Write-Output $lines
        if ($ready) { exit 0 }
        exit 1
    }
    finally { Pop-Location }
}

# Decode Vietnamese messages independently of the Windows PowerShell source code page.
function Get-JevMessage([string]$Message) {
    [regex]::Unescape($Message)
}

$ApiKey = $env:TYPESAFE_API_KEY
if ([string]::IsNullOrWhiteSpace($ApiKey)) {
    Write-Error (Get-JevMessage 'Ch\u01b0a c\u00f3 TYPESAFE_API_KEY trong m\u00f4i tr\u01b0\u1eddng. \u0110\u1eb7t bi\u1ebfn r\u1ed3i ch\u1ea1y l\u1ea1i.')
    exit 1
}

$Headers = @{ Authorization = "Bearer $ApiKey" }

# Invoke-RestMethod trên PowerShell 5.1 giải mã sai tiếng Việt; đọc byte và giải mã UTF-8.
function Invoke-Jev([string]$Method, [string]$Uri, [byte[]]$Body = $null) {
    $params = @{ Method = $Method; Uri = $Uri; Headers = $Headers; TimeoutSec = 120; UseBasicParsing = $true }
    if ($Body) { $params.ContentType = "application/json; charset=utf-8"; $params.Body = $Body }
    $r = Invoke-WebRequest @params
    $text = [System.Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
    return ($text | ConvertFrom-Json)
}

try {
    if ($Models) {
        $resp = Invoke-Jev -Method Get -Uri "https://api.typesafe.ai/v1/models"
        $json = $resp | ConvertTo-Json -Depth 10
    }
    else {
        if ([string]::IsNullOrWhiteSpace($RequestFile) -or -not (Test-Path $RequestFile)) {
            Write-Error (Get-JevMessage 'C\u1ea7n -RequestFile tr\u1ecf t\u1edbi file JSON c\u00f3 t\u1ed3n t\u1ea1i (ho\u1eb7c d\u00f9ng -Models).')
            exit 1
        }
        # Đọc bằng UTF-8 và gửi dạng byte để giữ nguyên tiếng Việt.
        $raw = Get-Content -Path $RequestFile -Raw -Encoding UTF8
        $null = $raw | ConvertFrom-Json   # kiểm tra JSON hợp lệ trước khi gửi
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($raw)
        $resp = Invoke-Jev -Method Post -Uri "https://api.typesafe.ai/v1/systemone" -Body $bytes
        $json = $resp | ConvertTo-Json -Depth 20
    }
}
catch {
    if ($_.Exception.Response) {
        $status = [int]$_.Exception.Response.StatusCode
        Write-Error ((Get-JevMessage 'Jev l\u1ed7i HTTP {0}: {1}') -f $status, $_.Exception.Message)
    }
    else {
        Write-Error $_.Exception.Message
    }
    exit 1
}

if ($OutFile) {
    $outPath = if ([System.IO.Path]::IsPathRooted($OutFile)) { $OutFile } else { Join-Path (Get-Location) $OutFile }
    [System.IO.File]::WriteAllText($outPath, $json, (New-Object System.Text.UTF8Encoding($false)))
    Write-Host ((Get-JevMessage '\u0110\u00e3 ghi k\u1ebft qu\u1ea3 v\u00e0o {0}') -f $OutFile)
}
else {
    Write-Output $json
}
