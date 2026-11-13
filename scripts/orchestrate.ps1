<#
Chạy tự động luồng: ý tưởng -> đánh giá -> kế hoạch/contract -> thiết kế -> test trước -> code
-> verify + mutation -> săn bug -> review agent -> review persona -> báo cáo cuối.
Người dùng chỉ đọc .agents/runs/<RunId>/FINAL_REPORT.md.

Dùng:
  .\scripts\orchestrate.ps1 -Idea "mô tả ý tưởng" [-SnapshotDirty]
  .\scripts\orchestrate.ps1 -IdeaFile ý_tưởng.md
  .\scripts\orchestrate.ps1 -Idea "x" -DryRun            # chạy thử luồng bằng câu trả lời giả, không gọi model/git/Gradle
  .\scripts\orchestrate.ps1 -RunId 20261113-101500        # chạy tiếp từ bước chưa xong
Làm việc trong git worktree + nhánh auto/<RunId> dưới .claude/worktrees/. Không push, không PR, không merge.
#>
param(
    [string]$Idea = "",
    [string]$IdeaFile = "",
    [string]$RunId = "",
    [string]$From = "",
    [string]$To = "",
    [int]$MaxFixLoops = -1,
    [int]$MaxAgentCalls = -1,
    [int]$MaxParallel = -1,
    [switch]$DryRun,
    [switch]$SnapshotDirty
)

$ErrorActionPreference = "Stop"
$Utf8 = New-Object System.Text.UTF8Encoding($false)
[Console]::OutputEncoding = $Utf8
$OutputEncoding = $Utf8
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

function Read-Text([string]$p) { return [IO.File]::ReadAllText($p, $Utf8) }
function Write-Text([string]$p, [string]$s) { [IO.File]::WriteAllText($p, $s, $Utf8) }

$Flow = (Read-Text "$Root\.agents\flow.json") | ConvertFrom-Json
$Roster = (Read-Text "$Root\.agents\roster.json") | ConvertFrom-Json
if ($MaxFixLoops -lt 0) { $MaxFixLoops = [int]$Flow.limits.maxFixLoops }
if ($MaxAgentCalls -lt 0) { $MaxAgentCalls = [int]$Flow.limits.maxAgentCalls }
if ($MaxParallel -lt 0) { $MaxParallel = [int]$Flow.limits.maxParallel }
$TestPattern = [string]$Flow.testPattern
$MaxDiffPerTask = [int]$Flow.limits.maxDiffPerTask

# ---------- Trạng thái ----------
if (-not $RunId) { $RunId = (Get-Date).ToString("yyyyMMdd-HHmmss") }
# RunId đi vào đường dẫn thư mục và tên nhánh, nên chỉ nhận một đoạn tên không có dấu phân cách hay '..'.
if ($RunId -notmatch '^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$') { throw "RunId không hợp lệ: chỉ dùng chữ, số, '-' và '_' (tối đa 64 ký tự)." }
$RunDir = Join-Path $Root ".agents\runs\$RunId"
New-Item -ItemType Directory -Force -Path "$RunDir\prompts", "$RunDir\logs", "$RunDir\shots" | Out-Null
$StatePath = Join-Path $RunDir "state.json"
$State = @{ idea = ""; runId = $RunId; base = ""; worktree = ""; branch = ""; testsCommit = ""; outcome = ""; calls = 0; fixLoops = 0;
            done = @(); stages = @(); gradle = @(); mutations = @(); notes = @(); verdicts = @(); shots = @(); fellBack = @() }
if (Test-Path $StatePath) {
    $loaded = (Read-Text $StatePath) | ConvertFrom-Json
    foreach ($prop in $loaded.PSObject.Properties) {
        if ($prop.Value -is [System.Array]) { $State[$prop.Name] = @($prop.Value) } else { $State[$prop.Name] = $prop.Value }
    }
    # Kết quả của lượt trước (BLOCKED/ERROR...) không được mang sang lượt chạy tiếp; báo cáo tính lại từ đầu.
    $State.outcome = ""
}
if ($IdeaFile) { $Idea = Read-Text $IdeaFile }
if ($Idea) { $State.idea = $Idea }
if (-not $State.idea) { throw "Cần -Idea hoặc -IdeaFile (hoặc -RunId của lượt đã có)." }

function Save-State { Write-Text $StatePath ($State | ConvertTo-Json -Depth 8) }
function Add-State([string]$name, [string]$value) { $State[$name] = @($State[$name]) + $value; Save-State }
function Note([string]$m) { Add-State "notes" $m; Step "GHI CHÚ: $m" }
function Step([string]$m) { Write-Host ("[{0}] {1}" -f (Get-Date).ToString("HH:mm:ss"), $m) }
function Log-Event([hashtable]$ev) {
    $ev.t = (Get-Date).ToString("o"); $ev.run = $RunId
    [IO.File]::AppendAllText("$RunDir\runs.jsonl", (($ev | ConvertTo-Json -Compress) + "`n"), $Utf8)
}
function Get-Out([string]$name) { $p = Join-Path $RunDir $name; if (Test-Path $p) { return Read-Text $p } else { return "" } }
function Clip([string]$s, [int]$n = 12000) { if ($null -eq $s) { return "" }; if ($s.Length -le $n) { return $s }; return $s.Substring(0, $n) + "`n...(cắt bớt)" }
function Stop-Run([string]$outcome, [string]$reason) { throw ("STOP|{0}|{1}" -f $outcome, $reason) }
# Khi một bước chạy lại, bỏ phán quyết cũ của bước đó để báo cáo không lẫn kết quả hai lượt.
function Reset-Verdicts([string]$pattern) { $State.verdicts = @(@($State.verdicts) | Where-Object { $_ -notmatch $pattern }); Save-State }

# ---------- Môi trường ----------
$WT = $State.worktree
if ($DryRun -and -not $WT) { $WT = Join-Path $RunDir "dryrun-wt"; New-Item -ItemType Directory -Force -Path $WT | Out-Null }

function Resolve-Exe([string]$name) {
    $c = Get-Command $name -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($c) { return $c.Source }
    return $null
}
function Q([string]$s) { if ($s -eq "" -or $s -match '[\s"]') { return '"' + ($s -replace '"', '\"') + '"' }; return $s }

function Assert-Env {
    if ($DryRun) { return }
    if (-not $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME)) { throw "JAVA_HOME chưa đặt hoặc không tồn tại (cần JDK 21)." }
    if (-not $env:ANDROID_HOME -or -not (Test-Path $env:ANDROID_HOME)) { throw "ANDROID_HOME chưa đặt hoặc không tồn tại." }
    if (-not (Resolve-Exe "git")) { throw "Không tìm thấy git." }
}

function With-Lock([string]$name, [scriptblock]$body) {
    if ($DryRun) { return (& $body) }
    $dir = Join-Path $Root ".agents\locks"; New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $path = Join-Path $dir "$name.lock"; $deadline = (Get-Date).AddMinutes(90)
    while ($true) {
        try {
            $fs = [IO.File]::Open($path, [IO.FileMode]::CreateNew, [IO.FileAccess]::Write, [IO.FileShare]::None)
            $b = $Utf8.GetBytes("$PID $RunId"); $fs.Write($b, 0, $b.Length); $fs.Close(); break
        } catch [System.IO.IOException] {
            $stale = $false
            try {
                $age = (Get-Date) - (Get-Item $path).LastWriteTime
                $owner = [int](((Read-Text $path) -split " ")[0])
                if ($age.TotalMinutes -gt 120 -or -not (Get-Process -Id $owner -ErrorAction SilentlyContinue)) { $stale = $true }
            } catch { }
            if ($stale) { Remove-Item $path -Force -ErrorAction SilentlyContinue; continue }
            if ((Get-Date) -gt $deadline) { throw "Chờ khóa '$name' quá 90 phút." }
            Start-Sleep -Seconds 10
        }
    }
    try { return (& $body) } finally { Remove-Item $path -Force -ErrorAction SilentlyContinue }
}

# ---------- Git ----------
function Invoke-Git([string[]]$a, [string]$dir = $WT) {
    # git coi -C "" là thư mục hiện tại, tức checkout của người dùng; không bao giờ chạy khi chưa có worktree.
    if (-not $dir) { throw "Chưa có worktree nên không chạy git $($a -join ' ')." }
    $o =& git -c core.quotepath=false -C $dir @a
    if ($LASTEXITCODE -ne 0) { throw ("git {0} thất bại (mã {1})" -f ($a -join " "), $LASTEXITCODE) }
    return $o
}
function Get-Changed {
    if ($DryRun) { return @() }
    $out = @(Invoke-Git @("status", "--porcelain=v1", "-uall"))
    $r = @()
    foreach ($l in $out) {
        if ($l.Length -lt 4) { continue }
        $p = $l.Substring(3); if ($p -match " -> ") { $p = ($p -split " -> ")[-1] }
        $r += $p.Trim('"')
    }
    return $r
}
function Commit-All([string]$msg) {
    if ($DryRun) { Step "[dry-run] commit: $msg"; return }
    if ((Get-Changed).Count -eq 0) { return }
    Invoke-Git @("add", "-A") | Out-Null
    Invoke-Git @("commit", "-m", "$msg`n`nRun: $RunId") | Out-Null
}
function Revert-Path([string]$rel) {
    $tracked = @(Invoke-Git @("ls-files", "--", $rel))
    if ($tracked.Count -gt 0) { Invoke-Git @("checkout", "HEAD", "--", $rel) | Out-Null }
    else { Remove-Item -LiteralPath (Join-Path $WT $rel) -Force -ErrorAction SilentlyContinue }
}
# Đường dẫn do agent đưa ra: trả đường dẫn đầy đủ nếu nằm trong worktree, ngược lại $null ('..' hoặc đường dẫn tuyệt đối).
function Resolve-InWorktree([string]$rel) {
    if (-not $rel -or -not $WT) { return $null }
    if ([IO.Path]::IsPathRooted($rel)) { return $null }
    $rootFull = [IO.Path]::GetFullPath($WT).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    $full = [IO.Path]::GetFullPath((Join-Path $WT $rel))
    if (-not $full.StartsWith($rootFull, [StringComparison]::OrdinalIgnoreCase)) { return $null }
    return $full
}
function Test-ContractPath([string]$rel) { return [bool]($rel -match '^docs/tests/[A-Za-z0-9._-]+\.md$' -and $rel -notmatch '\.\.') }
# Trả về danh sách file vi phạm quyền ghi của vai (đã hoàn tác).
function Assert-Writes($plan, [string[]]$owned) {
    if ($DryRun) { return @() }
    $viol = @()
    $ownedPaths = @($owned | ForEach-Object { $_ -replace "\\", "/" })
    foreach ($c in (Get-Changed)) {
        $ok = $false
        foreach ($w in $plan.Writes) {
            if ($w -eq "@owned") {
                if ($c -match $TestPattern) { continue }
                # Chỉ khớp đúng đường dẫn trong `owns`; file mới cần được planner liệt kê sẵn.
                if ($ownedPaths -contains $c) { $ok = $true }
            } elseif ($c -match $w) { $ok = $true }
        }
        if (-not $ok) { $viol += $c; Revert-Path $c }
    }
    if ($viol.Count -gt 0) { Note ("Vai {0} ghi ngoài quyền, đã hoàn tác: {1}" -f $plan.Name, ($viol -join ", ")) }
    return $viol
}
function Revert-TestTampering {
    if ($DryRun -or -not $State.testsCommit) { return @() }
    $changed = @(Invoke-Git @("diff", "--name-only", $State.testsCommit)) + @(Get-Changed) | Where-Object { $_ -match $TestPattern } | Select-Object -Unique
    foreach ($f in $changed) {
        $in = @(Invoke-Git @("ls-tree", "--name-only", $State.testsCommit, "--", $f))
        if ($in.Count -gt 0) { Invoke-Git @("checkout", $State.testsCommit, "--", $f) | Out-Null } else { Remove-Item -LiteralPath (Join-Path $WT $f) -Force -ErrorAction SilentlyContinue }
    }
    if (@($changed).Count -gt 0) { Note ("Executor đụng file test, đã hoàn tác: {0}" -f (@($changed) -join ", ")) }
    return @($changed)
}
# Bước viết test chỉ được thêm: file test đã có mà bị xóa hoặc mất dòng thì hoàn tác về HEAD.
function Revert-ExistingTestEdits {
    if ($DryRun) { return @() }
    $bad = @()
    foreach ($l in @(Invoke-Git @("diff", "--numstat", "--no-renames", "HEAD"))) {
        $p = $l -split "`t"
        if ($p.Count -lt 3 -or $p[2] -notmatch $TestPattern) { continue }
        if ($p[1] -ne "0") { $bad += $p[2] }
    }
    foreach ($f in $bad) { Invoke-Git @("checkout", "HEAD", "--", $f) | Out-Null }
    if ($bad.Count -gt 0) { Note ("Test writer sửa hoặc xóa test đã có, đã hoàn tác: {0}" -f ($bad -join ", ")) }
    return $bad
}
function Setup-Worktree {
    if ($DryRun -or $State.worktree) { return }
    Assert-Env
    $dirty = @(Invoke-Git @("status", "--porcelain") $Root)
    $dirtyTracked = @($dirty | Where-Object { $_ -notmatch '^\?\?' })
    if ($dirty.Count -gt 0 -and -not $SnapshotDirty) {
        throw "Working tree đang có thay đổi chưa commit ($($dirty.Count) mục). Worktree chỉ lấy từ commit; chạy lại với -SnapshotDirty để chụp working tree (kể cả file chưa theo dõi) làm base."
    }
    $wt = Join-Path $Root ".claude\worktrees\auto-$RunId"; $branch = "auto/$RunId"
    New-Item -ItemType Directory -Force -Path (Split-Path $wt -Parent) | Out-Null
    Invoke-Git @("worktree", "add", "-b", $branch, $wt, "HEAD") $Root | Out-Null
    $State.worktree = $wt; $State.branch = $branch; $script:WT = $wt
    if ($dirty.Count -gt 0) {
        if ($dirtyTracked.Count -gt 0) {
            $snap = (@(Invoke-Git @("stash", "create") $Root) -join "").Trim()
            if ($snap) { Invoke-Git @("stash", "apply", $snap) $wt | Out-Null }
        }
        # File chưa theo dõi (không bị .gitignore) cũng được chép sang để build giống working tree.
        $untracked = @(Invoke-Git @("ls-files", "--others", "--exclude-standard") $Root)
        foreach ($u in $untracked) {
            $src = Join-Path $Root $u; $dst = Join-Path $wt $u
            if (-not (Test-Path -LiteralPath $src -PathType Leaf)) { continue }
            New-Item -ItemType Directory -Force -Path (Split-Path $dst -Parent) | Out-Null
            Copy-Item -LiteralPath $src -Destination $dst -Force
        }
        Commit-All "auto: snapshot working tree lúc bắt đầu"
        Note "Base là snapshot working tree lúc bắt đầu ($($dirtyTracked.Count) file đã theo dõi, $($untracked.Count) file chưa theo dõi). Merge nhánh này vào working tree có sẵn thay đổi cũ có thể xung đột."
    }
    $State.base = (@(Invoke-Git @("rev-parse", "HEAD") $wt) -join "").Trim()
    Save-State
    Step "Worktree: $wt (nhánh $branch, base $($State.base.Substring(0,8)))"
}
function Get-DiffFiles {
    if ($DryRun) { return @("shared/src/commonMain/kotlin/dry/Run.kt") }
    return @(Invoke-Git @("diff", "--name-only", "$($State.base)..HEAD"))
}

# ---------- Agent ----------
function Get-RolePlan([string]$name, [bool]$forceFallback = $false) {
    $r = $Roster.roles.$name
    if (-not $r) { throw "Vai không có trong roster: $name" }
    $p = [string]$r.provider; $m = [string]$r.model; $fb = $false
    # Vai Gemini chỉ dùng model dự phòng khi máy chưa có gemini CLI hoặc lượt gọi Gemini vừa lỗi.
    if ($p -eq "gemini" -and ($forceFallback -or -not (Resolve-Exe "gemini"))) {
        if (-not $r.fallback) { throw "Vai $name khai báo gemini, chưa có gemini CLI và roster không có fallback." }
        $p = [string]$r.fallback.provider; $m = [string]$r.fallback.model; $fb = $true
        if (@($State.fellBack) -notcontains $name) { Add-State "fellBack" $name }
    }
    return [pscustomobject]@{ Name = $name; Provider = $p; Model = $m; Effort = $r.effort; Access = [string]$r.access; Writes = @($r.writes); Fallback = $fb; Declared = [string]$r.provider }
}
function Render([string]$tpl, [hashtable]$vars) {
    foreach ($k in $vars.Keys) { $tpl = $tpl.Replace("{{$k}}", (Clip ([string]$vars[$k]))) }
    return ([regex]::Replace($tpl, '\{\{[A-Z_]+\}\}', '(không có)'))
}
function Build-Prompt([string]$name, [hashtable]$vars) {
    $common = Read-Text "$Root\.agents\prompts\_common.md"
    $stage = Read-Text "$Root\.agents\prompts\$name.md"
    return $common + "`n`n---`n`n" + (Render $stage $vars)
}
function Get-Tag([string]$text) {
    $m = [regex]::Matches($text, '(?im)RESULT:\s*([A-Za-z\-]+)')
    if ($m.Count -eq 0) { return "" }
    return $m[$m.Count - 1].Groups[1].Value.ToUpperInvariant()
}
function Get-JsonBlock([string]$text) {
    $m = [regex]::Matches($text, '(?s)```json\s*(.*?)```')
    $script:JsonError = ""
    if ($m.Count -eq 0) { $script:JsonError = 'không có khối json'; return $null }
    # JSON hỏng trả $null để vòng thử lại của bước gọi xử lý, thay vì làm dừng cả lượt chạy.
    try { return ($m[$m.Count - 1].Groups[1].Value | ConvertFrom-Json) }
    catch { $script:JsonError = $_.Exception.Message; return $null }
}
function Get-DryText($spec) {
    $l = $spec.Label
    if ($l -like "s02*") { return "Ràng buộc: ĐẠT.`nRESULT: GO" }
    if ($l -like "s03*") {
        return "Kế hoạch giả lập.`n``````json`n" + '{ "feature": "dry", "tasks": [ { "id": "D-01", "title": "logic", "kind": "logic", "deps": [], "owns": ["shared/src/commonMain/kotlin/dry/A.kt"], "tests": ["shared/src/commonTest/kotlin/dry/ATest.kt"], "gradle": [":shared:testDebugUnitTest"], "contract": "docs/tests/D-01.md", "est_diff": 80 }, { "id": "D-02", "title": "ui", "kind": "ui", "deps": ["D-01"], "owns": ["composeApp/src/main/kotlin/dry/Screen.kt"], "tests": ["composeApp/src/test/kotlin/dry/ScreenTest.kt"], "gradle": [":composeApp:testDebugUnitTest"], "contract": "docs/tests/D-02.md", "est_diff": 120 } ] }' + "`n``````" + "`nRESULT: DONE"
    }
    if ($l -like "s04*") { return "Đặc tả giả lập.`n| key | tiếng Việt |`n| a | Nên dùng trước, khoảng 2 ngày (ước tính) |`nRESULT: DONE" }
    if ($l -like "s05*") { return "Không có lỗi.`nRESULT: PASS" }
    if ($l -like "s09-mut*") { return "``````json`n" + '[ { "task": "D-01", "id": "m1", "file": "shared/src/commonMain/kotlin/dry/A.kt", "find": "a", "replace": "b", "why": "dry" } ]' + "`n``````" + "`nRESULT: DONE" }
    if ($l -like "s10*") { return "Không thấy bug S1/S2.`nRESULT: PASS" }
    if ($l -like "s11*") { return "Các mục bắt buộc ĐẠT (giả lập).`nRESULT: PASS" }
    if ($l -like "s12*") { return "Dùng được (giả lập, chưa có ảnh thật).`nRESULT: OK" }
    return "Xong (giả lập).`nRESULT: DONE"
}
function Start-One($spec) {
    if ($State.calls -ge $MaxAgentCalls) { Stop-Run "BLOCKED" "Hết ngân sách $MaxAgentCalls lượt gọi agent" }
    $State.calls = [int]$State.calls + 1; Save-State
    $plan = Get-RolePlan $spec.Role ([int]$spec.Attempt -ge 1)
    $h = @{ Spec = $spec; Plan = $plan; Started = Get-Date; Proc = $null; Fake = $null; Attempt = [int]$spec.Attempt }
    Step ("Gọi {0} ({1}/{2}{3}) cho {4}" -f $spec.Role, $plan.Provider, $plan.Model, $(if ($plan.Fallback) { ", thay " + $plan.Declared } else { "" }), $spec.Label)
    if ($DryRun) { $h.Fake = Get-DryText $spec; return $h }
    $pf = "$RunDir\prompts\$($spec.Label).txt"; Write-Text $pf $spec.Prompt
    $out = "$RunDir\logs\$($spec.Label).out"; $err = "$RunDir\logs\$($spec.Label).err"; $last = "$RunDir\logs\$($spec.Label).last"
    Remove-Item $out, $err, $last -Force -ErrorAction SilentlyContinue
    if ($plan.Provider -eq "claude") {
        $exe = Resolve-Exe "claude"; if (-not $exe) { throw "Không tìm thấy claude CLI." }
        $a = @("-p", "--model", $plan.Model, "--permission-mode", "dontAsk", "--allowedTools", $Roster.access.($plan.Access).claude, "--add-dir", $RunDir, "--output-format", "text")
        if ($plan.Effort) { $a += @("--effort", [string]$plan.Effort) }
    } elseif ($plan.Provider -eq "codex") {
        $exe = Resolve-Exe "codex"; if (-not $exe) { throw "Không tìm thấy codex CLI." }
        $a = @("exec", "-m", $plan.Model, "-s", $Roster.access.($plan.Access).codex, "-C", $spec.Cwd, "--skip-git-repo-check", "--ephemeral", "-o", $last)
        if ($plan.Effort) { $a += @("-c", "model_reasoning_effort=$($plan.Effort)") }
        $a += "-"
    } elseif ($plan.Provider -eq "gemini") {
        # Prompt đi qua stdin; không bật chế độ tự duyệt nên Gemini chỉ đọc. Lượt lỗi sẽ thử lại bằng model dự phòng.
        $exe = Resolve-Exe "gemini"; if (-not $exe) { throw "Không tìm thấy gemini CLI." }
        $a = @("-m", $plan.Model)
    } else { throw "Provider không hỗ trợ: $($plan.Provider)" }
    $argStr = ($a | ForEach-Object { Q ([string]$_) }) -join " "
    $h.Proc = Start-Process -FilePath $exe -ArgumentList $argStr -WorkingDirectory $spec.Cwd -RedirectStandardInput $pf -RedirectStandardOutput $out -RedirectStandardError $err -NoNewWindow -PassThru
    $null = $h.Proc.Handle
    $h.Out = $out; $h.Err = $err; $h.Last = $last
    return $h
}
function Finish-One($h) {
    $text = ""; $exit = 0
    if ($h.Fake -ne $null) { $text = $h.Fake }
    else {
        $exit = $h.Proc.ExitCode
        if ($h.Plan.Provider -eq "codex" -and (Test-Path $h.Last)) { $text = Read-Text $h.Last }
        elseif (Test-Path $h.Out) { $text = Read-Text $h.Out }
    }
    $ok = ($exit -eq 0 -and $text.Trim().Length -gt 0)
    $secs = [int]((Get-Date) - $h.Started).TotalSeconds
    Log-Event @{ kind = "agent"; role = $h.Spec.Role; provider = $h.Plan.Provider; model = $h.Plan.Model; label = $h.Spec.Label; exit = $exit; ok = $ok; seconds = $secs }
    return @{ Spec = $h.Spec; Text = $text; Exit = $exit; Ok = $ok; Tag = (Get-Tag $text) }
}
# Chạy nhiều agent, tối đa $MaxParallel cùng lúc; lỗi thì thử lại 1 lần. Trả hashtable Label -> kết quả.
function Invoke-Agents([object[]]$specs) {
    $results = @{}
    $pending = New-Object System.Collections.ArrayList
    foreach ($s in $specs) { $s.Attempt = 0; [void]$pending.Add($s) }
    $running = New-Object System.Collections.ArrayList
    while ($pending.Count -gt 0 -or $running.Count -gt 0) {
        while ($pending.Count -gt 0 -and $running.Count -lt $MaxParallel) {
            $s = $pending[0]; $pending.RemoveAt(0); [void]$running.Add((Start-One $s))
        }
        $finished = $null
        foreach ($h in $running) {
            if ($h.Fake -ne $null -or $h.Proc.HasExited) { $finished = $h; break }
            if (((Get-Date) - $h.Started).TotalMinutes -gt 45) {
                & taskkill /PID $h.Proc.Id /T /F | Out-Null; Start-Sleep -Seconds 1
                $finished = $h; break
            }
        }
        if ($null -eq $finished) { Start-Sleep -Seconds 3; continue }
        $running.Remove($finished)
        $r = Finish-One $finished
        if (-not $r.Ok -and $finished.Spec.Attempt -lt 1) {
            $finished.Spec.Attempt = 1; $finished.Spec.Label = $finished.Spec.Label + "-retry"
            Note ("Agent {0} lỗi (mã {1}), thử lại 1 lần." -f $finished.Spec.Role, $r.Exit)
            [void]$pending.Add($finished.Spec)
        } else { $results[$finished.Spec.Label] = $r }
    }
    return $results
}
function Invoke-One([string]$role, [string]$prompt, [string]$label) {
    $res = Invoke-Agents @(@{ Role = $role; Prompt = $prompt; Label = $label; Cwd = $WT })
    $r = $res.Values | Select-Object -First 1
    if (-not $r.Ok) { Stop-Run "BLOCKED" "Agent $role ($label) thất bại: mã $($r.Exit), xem logs\$label*.err" }
    return $r
}

# ---------- Gradle / thiết bị ----------
function Invoke-Gradle([string[]]$tasks, [string]$label, [int]$dryExit = 0) {
    $log = "$RunDir\logs\gradle-$label.log"
    if ($DryRun) {
        Step "[dry-run] gradle $($tasks -join ' ')"
        Add-State "gradle" ("{0}|{1}|dry-run" -f $label, ($tasks -join " "))
        return @{ Exit = $dryExit; Tail = "dry-run"; Compile = $false; TestFail = $false; Log = $log }
    }
    Assert-Env
    $res = With-Lock "gradle" {
        $argStr = (@($tasks) + @("--console=plain", "--continue") | ForEach-Object { Q $_ }) -join " "
        Step "Gradle: $argStr"
        $p = Start-Process -FilePath (Join-Path $WT "gradlew.bat") -ArgumentList $argStr -WorkingDirectory $WT -RedirectStandardOutput $log -RedirectStandardError "$log.err" -NoNewWindow -PassThru
        $null = $p.Handle
        if (-not $p.WaitForExit(2400000)) { & taskkill /PID $p.Id /T /F | Out-Null; return @{ Exit = 124 } }
        return @{ Exit = $p.ExitCode }
    }
    $txt = ""; if (Test-Path $log) { $txt = Read-Text $log }
    if (Test-Path "$log.err") { $txt += "`n" + (Read-Text "$log.err") }
    $lines = $txt -split "`r?`n"
    $tail = ($lines | Select-Object -Last 120) -join "`n"
    $compile = [bool]($txt -match "Execution failed for task ':[^']*compile|^e: file:///" )
    Add-State "gradle" ("{0}|{1}|exit {2}" -f $label, ($tasks -join " "), $res.Exit)
    Log-Event @{ kind = "gradle"; label = $label; tasks = ($tasks -join " "); exit = $res.Exit }
    $testFail = [bool]($txt -match "There were failing tests")
    return @{ Exit = $res.Exit; Tail = $tail; Compile = $compile; TestFail = $testFail; Log = $log }
}
function Get-TestCounts {
    $r = @()
    foreach ($m in @("shared", "composeApp")) {
        $d = Join-Path $WT "$m\build\test-results\testDebugUnitTest"
        if (-not (Test-Path $d)) { continue }
        $t = 0; $f = 0; $s = 0
        foreach ($x in (Get-ChildItem $d -Filter "*.xml")) {
            $doc = [xml](Read-Text $x.FullName)
            $t += [int]$doc.testsuite.tests; $f += [int]$doc.testsuite.failures + [int]$doc.testsuite.errors; $s += [int]$doc.testsuite.skipped
        }
        $r += ("{0}: {1} test, {2} lỗi, {3} bỏ qua" -f $m, $t, $f, $s)
    }
    return $r
}
function Invoke-Device {
    $res = @{ Note = ""; Shots = @(); Crash = $false }
    if ($DryRun) { $res.Note = "dry-run: bỏ qua thiết bị"; return $res }
    $adb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
    if (-not (Test-Path $adb)) { $res.Note = "Không có adb; chưa có ảnh thật."; return $res }
    function Adb([string[]]$a) {
        $o = "$RunDir\logs\adb.out"; Remove-Item $o -Force -ErrorAction SilentlyContinue
        $p = Start-Process -FilePath $adb -ArgumentList (($a | ForEach-Object { Q $_ }) -join " ") -RedirectStandardOutput $o -RedirectStandardError "$o.err" -NoNewWindow -Wait -PassThru
        $t = ""; if (Test-Path $o) { $t = Read-Text $o }
        return @{ Exit = $p.ExitCode; Text = $t }
    }
    $dev = (Adb @("devices")).Text -split "`r?`n" | Where-Object { $_ -match "\tdevice$" } | Select-Object -First 1
    if (-not $dev) { $res.Note = "Không có emulator/thiết bị đang chạy; chưa có ảnh thật (không tự khởi động emulator)."; return $res }
    $serial = ($dev -split "\t")[0]
    $apk = Join-Path $WT "composeApp\build\outputs\apk\debug\composeApp-debug.apk"
    if (-not (Test-Path $apk)) { $res.Note = "Chưa có APK debug."; return $res }
    With-Lock "emulator" {
        Adb @("-s", $serial, "logcat", "-c") | Out-Null
        $inst = Adb @("-s", $serial, "install", "-r", $apk)
        if ($inst.Exit -ne 0) { $res.Note = "Cài APK thất bại: $($inst.Text.Trim())"; return }
        Adb @("-s", $serial, "shell", "am", "start", "-n", "com.loitu.yourrefri/.MainActivity") | Out-Null
        Start-Sleep -Seconds 4
        Adb @("-s", $serial, "shell", "screencap", "-p", "/sdcard/yr_launch.png") | Out-Null
        $png = "$RunDir\shots\launch.png"
        Adb @("-s", $serial, "pull", "/sdcard/yr_launch.png", $png) | Out-Null
        if (Test-Path $png) { $res.Shots += $png }
        $crash = (Adb @("-s", $serial, "logcat", "-d", "-b", "crash")).Text
        if ($crash -match "FATAL EXCEPTION") { $res.Crash = $true; Write-Text "$RunDir\logs\crash.txt" $crash }
    } | Out-Null
    if (-not $res.Note) { $res.Note = "Đã chạy trên $serial; ảnh chỉ là màn khởi động (chưa có kịch bản bấm qua các tab)." }
    if ($res.Crash) { $res.Note += " PHÁT HIỆN CRASH (FATAL EXCEPTION), xem logs\crash.txt." }
    return $res
}

# ---------- Kế hoạch ----------
$PlanObj = $null
function Load-Plan { $p = Join-Path $RunDir "plan.json"; if (Test-Path $p) { $script:PlanObj = (Read-Text $p) | ConvertFrom-Json } }
function Test-Plan($plan) {
    $err = @()
    if (-not $plan -or -not $plan.tasks -or @($plan.tasks).Count -eq 0) { return @("Kế hoạch không có task.") }
    $ids = @($plan.tasks | ForEach-Object { $_.id })
    if (($ids | Select-Object -Unique).Count -ne $ids.Count) { $err += "ID task bị trùng." }
    $seen = @{}
    foreach ($t in $plan.tasks) {
        if (-not $t.id) { $err += "Task thiếu id."; continue }
        if (@($t.owns).Count -eq 0) { $err += "$($t.id): thiếu owns." }
        if (@($t.tests).Count -eq 0) { $err += "$($t.id): thiếu tests." }
        if ([int]$t.est_diff -gt $MaxDiffPerTask) { $err += "$($t.id): est_diff $($t.est_diff) > $MaxDiffPerTask, cần tách." }
        if (-not $t.contract) { $err += "$($t.id): thiếu contract." }
        elseif (-not (Test-ContractPath ([string]$t.contract))) { $err += "$($t.id): contract phải có dạng docs/tests/<tên>.md, nhận '$($t.contract)'." }
        foreach ($o in @($t.owns)) {
            if ($o -match $TestPattern) { $err += "$($t.id): owns chứa file test $o." }
            if ($seen.ContainsKey($o)) { $err += "$($t.id): file $o đã thuộc $($seen[$o])." } else { $seen[$o] = $t.id }
        }
        foreach ($d in @($t.deps)) { if ($ids -notcontains $d) { $err += "$($t.id): phụ thuộc $d không tồn tại." } }
    }
    if ($err.Count -eq 0) { try { [void](Get-Waves $plan) } catch { $err += $_.Exception.Message } }
    return $err
}
function Get-Waves($plan) {
    $level = @{}; $tasks = @($plan.tasks)
    for ($pass = 0; $pass -le $tasks.Count; $pass++) {
        foreach ($t in $tasks) {
            $l = 0; $ready = $true
            foreach ($d in @($t.deps)) { if ($level.ContainsKey($d)) { $l = [Math]::Max($l, $level[$d] + 1) } else { $ready = $false } }
            if ($ready) { $level[$t.id] = $l }
        }
    }
    foreach ($t in $tasks) { if (-not $level.ContainsKey($t.id)) { throw "Phụ thuộc vòng tròn quanh $($t.id)." } }
    $max = ($level.Values | Measure-Object -Maximum).Maximum
    $waves = @()
    for ($i = 0; $i -le $max; $i++) { $waves += , @($tasks | Where-Object { $level[$_.id] -eq $i }) }
    return $waves
}
function Get-PlanGradle {
    $g = @(); foreach ($t in $PlanObj.tasks) { $g += @($t.gradle) }
    $g = @($g | Where-Object { $_ } | Select-Object -Unique)
    if ($g.Count -eq 0) { $g = @(":shared:testDebugUnitTest") }
    return $g
}
function Test-HasUi { return [bool]@($PlanObj.tasks | Where-Object { $_.kind -eq "ui" }).Count }
function Task-Vars($t) {
    return @{ TASK = ($t | ConvertTo-Json -Depth 5); OWNS = (@($t.owns) -join ", "); TESTS = (@($t.tests) -join ", ") }
}

# ---------- Các bước ----------
function Stage-Agent($st, [hashtable]$vars) {
    $r = Invoke-One $st.role (Build-Prompt $st.prompt $vars) $st.id
    Write-Text (Join-Path $RunDir $st.out) $r.Text
    return $r
}
function Stage-Idea($st) {
    Setup-Worktree
    $r = Stage-Agent $st @{ IDEA = $State.idea }
    Add-State "stages" "$($st.id)|xong|$($r.Tag)"
}
function Stage-Evaluate($st) {
    $r = Stage-Agent $st @{ BRIEF = (Get-Out "01_idea_brief.md") }
    Add-State "stages" "$($st.id)|xong|$($r.Tag)"
    if ($r.Tag -ne "GO") { Stop-Run "NO-GO" "Advisor không cho GO (xem 02_evaluation.md)" }
}
function Stage-Plan($st) {
    $feedback = ""
    for ($i = 0; $i -le 1; $i++) {
        $vars = @{ BRIEF = (Get-Out "01_idea_brief.md"); EVAL = (Get-Out "02_evaluation.md") }
        if ($feedback) { $vars.EVAL += "`n`nKế hoạch lần trước bị từ chối, sửa các lỗi sau:`n$feedback" }
        $r = Invoke-One $st.role (Build-Prompt $st.prompt $vars) "$($st.id)-$i"
        Write-Text (Join-Path $RunDir $st.out) $r.Text
        [void](Assert-Writes (Get-RolePlan $st.role) @())
        $plan = Get-JsonBlock $r.Text
        $errs = @(Test-Plan $plan)
        if ($null -eq $plan -and $script:JsonError) { $errs += "Khối JSON không đọc được: $($script:JsonError)" }
        # Các bước sau đọc contract, nên thiếu file contract cũng là kế hoạch chưa hợp lệ.
        if ($errs.Count -eq 0 -and -not $DryRun) {
            foreach ($t in $plan.tasks) {
                $cf = Resolve-InWorktree ([string]$t.contract)
                if (-not $cf -or -not (Test-Path -LiteralPath $cf -PathType Leaf) -or (Get-Item -LiteralPath $cf).Length -eq 0) {
                    $errs += "$($t.id): chưa tạo contract $($t.contract) (file phải tồn tại và có nội dung)."
                }
            }
        }
        if ($errs.Count -eq 0) {
            Write-Text "$RunDir\plan.json" ($plan | ConvertTo-Json -Depth 8); Load-Plan
            Commit-All "plan: contract $($plan.feature)"
            Add-State "stages" "$($st.id)|xong|$(@($plan.tasks).Count) task"; return
        }
        $feedback = $errs -join "`n"
    }
    Stop-Run "BLOCKED" ("Planner không tạo được kế hoạch hợp lệ: " + $feedback.Replace("`n", "; "))
}
function Stage-Design($st) {
    if (-not (Test-HasUi)) { Add-State "stages" "$($st.id)|bỏ qua|không có task ui"; return }
    Reset-Verdicts '^design_review\|'
    $feedback = ""
    for ($i = 0; $i -le $MaxFixLoops; $i++) {
        $vars = @{ PLAN = (Get-Out "plan.json") }
        if ($feedback) { $vars.PLAN += "`n`nBản thiết kế trước bị từ chối, sửa:`n$feedback" }
        $r = Invoke-One "designer" (Build-Prompt "design" $vars) "s04_design-$i"
        Write-Text "$RunDir\04_design_spec.md" $r.Text
        $lint = (& "$Root\scripts\lint-copy.ps1" -Path "$RunDir\04_design_spec.md" | Out-String)
        if ($LASTEXITCODE -ne 0) { $feedback = "Lint từ cấm (không nhắc lại từ cấm trong đặc tả; dòng chỉ trích dẫn quy tắc thì thêm 'lint-copy: rule'):`n$lint"; $State.fixLoops = [int]$State.fixLoops + 1; continue }
        $rv = Invoke-One "design_reviewer" (Build-Prompt "design_review" @{ DESIGN = $r.Text; LINT = $lint }) "s05_design_review-$i"
        Write-Text "$RunDir\05_design_review.md" $rv.Text
        if ($rv.Tag -eq "PASS") {
            Add-State "stages" "s04_design|xong|lặp $i"; Add-State "stages" "s05_design_review|PASS|lặp $i"; return
        }
        $feedback = $rv.Text; $State.fixLoops = [int]$State.fixLoops + 1
    }
    Add-State "stages" "s05_design_review|chưa đạt|xem 05_design_review.md"
    Add-State "verdicts" "design_review|FAIL"
    # Bản thiết kế bị từ chối không được dùng cho handoff, test và code.
    Stop-Run "BLOCKED" "Thiết kế chưa qua duyệt sau $MaxFixLoops lần sửa (xem 05_design_review.md); chạy lại từ s04_design sau khi quyết định"
}
function Stage-Handoff($st) {
    if (-not (Test-HasUi)) { Add-State "stages" "$($st.id)|bỏ qua|không có task ui"; return }
    $r = Stage-Agent $st @{ DESIGN = (Get-Out "04_design_spec.md"); PLAN = (Get-Out "plan.json") }
    Add-State "stages" "$($st.id)|xong|"
}
function Stage-TestsFirst($st) {
    $planGradle = Get-PlanGradle
    $base = Invoke-Gradle $planGradle "baseline"
    if ($base.Exit -ne 0) { Stop-Run "BLOCKED" "Base không xanh trước khi bắt đầu (xem logs\gradle-baseline.log)" }
    $feedback = ""
    for ($i = 0; $i -le 1; $i++) {
        $vars = @{ PLAN = (Get-Out "plan.json"); HANDOFF = (Get-Out "06_code_handoff.md") }
        if ($feedback) { $vars.PLAN += "`n`n$feedback" }
        $r = Invoke-One $st.role (Build-Prompt $st.prompt $vars) "$($st.id)-$i"
        Write-Text (Join-Path $RunDir $st.out) $r.Text
        [void](Assert-Writes (Get-RolePlan $st.role) @())
        [void](Revert-ExistingTestEdits)
        Commit-All "tests-first: test cho $($PlanObj.feature)"
        if (-not $DryRun) { $State.testsCommit = (@(Invoke-Git @("rev-parse", "HEAD")) -join "").Trim() } else { $State.testsCommit = "dry" }
        Save-State
        $missing = @()
        if (-not $DryRun) {
            foreach ($t in $PlanObj.tasks) { foreach ($tf in @($t.tests)) {
                $full = Resolve-InWorktree ([string]$tf)
                if (-not $full -or -not (Test-Path -LiteralPath $full -PathType Leaf)) { $missing += [string]$tf }
            } }
        }
        if ($missing.Count -gt 0) { $feedback = "Chưa có các file test đã khai báo trong kế hoạch: $($missing -join ', '). Viết đủ các file đó (chỉ thêm, không sửa test cũ)."; continue }
        $red = Invoke-Gradle $planGradle "red-$i" 1
        if ($red.Exit -ne 0) {
            # Chỉ lỗi biên dịch (thiếu code) hoặc test thất bại mới là bằng chứng đỏ; lỗi cấu hình hay mạng thì không.
            if (-not $DryRun -and -not $red.Compile -and -not $red.TestFail) { Stop-Run "BLOCKED" "Gradle lỗi nhưng không phải do biên dịch hay test thất bại (logs\gradle-red-$i.log)" }
            Add-State "stages" "$($st.id)|xong|đỏ như kỳ vọng"; return
        }
        $feedback = "Test chạy XANH trước khi có code, nghĩa là chưa kiểm hành vi mới. Viết thêm test chặt hơn (chỉ thêm, không sửa test cũ)."
    }
    Add-State "stages" "$($st.id)|chưa đạt|chưa có test đỏ trước khi code"
    Stop-Run "BLOCKED" ("Chưa có bằng chứng test đỏ trước khi code: " + $feedback)
}
# Chạy executor lần lượt từng task và commit sau mỗi task, để kiểm quyền ghi theo đúng `owns` của chính task đó.
function Invoke-Executors($tasks, [string]$labelPrefix, [string]$failures) {
    foreach ($t in @($tasks)) {
        $v = Task-Vars $t; $v.HANDOFF = (Get-Out "06_code_handoff.md")
        if ($failures) { $v.FAILURES = $failures }
        $label = "$labelPrefix-$($t.id)"
        $res = Invoke-Agents @(@{ Role = "executor"; Prompt = (Build-Prompt "code" $v); Label = $label; Cwd = $WT })
        $r = $res.Values | Select-Object -First 1
        if (-not $r -or -not $r.Ok) { Stop-Run "BLOCKED" "Executor $label thất bại" }
        if ($r.Tag -eq "BLOCKED") { Stop-Run "BLOCKED" "Executor báo test/contract mâu thuẫn ($label): xem logs, cần người quyết định" }
        [void](Assert-Writes (Get-RolePlan "executor") @($t.owns))
        [void](Revert-TestTampering)
        Commit-All "code: $label"
    }
}
function Stage-Code($st) {
    $wi = 0
    foreach ($wave in (Get-Waves $PlanObj)) {
        $wi++
        $failures = ""
        for ($loop = 0; $loop -le $MaxFixLoops; $loop++) {
            $fl = ""
            if ($failures) { $fl = "Lần chạy trước thất bại. Sửa theo log sau (không sửa test):`n$failures" }
            Invoke-Executors $wave "s08-w$wi-l$loop" $fl
            $g = Invoke-Gradle (@($wave | ForEach-Object { @($_.gradle) } | Select-Object -Unique)) "s08-w$wi-$loop"
            if ($g.Exit -eq 0) { break }
            if ($loop -eq $MaxFixLoops) { Stop-Run "BLOCKED" "Wave $wi vẫn đỏ sau $MaxFixLoops lần sửa (logs\gradle-s08-w$wi-$loop.log)" }
            $failures = Clip $g.Tail 6000; $State.fixLoops = [int]$State.fixLoops + 1
        }
    }
    Write-Text (Join-Path $RunDir $st.out) "Code xong theo $wi wave."
    Add-State "stages" "$($st.id)|xong|$wi wave"
}
function Get-ModuleTasks([string[]]$files) {
    $t = @()
    if (@($files | Where-Object { $_ -like "shared/*" }).Count -gt 0) { $t += ":shared:testDebugUnitTest" }
    if (@($files | Where-Object { $_ -like "composeApp/*" }).Count -gt 0) { $t += ":composeApp:testDebugUnitTest" }
    if ($t.Count -eq 0) { $t = @(":shared:testDebugUnitTest") }
    return $t
}
$MutationsPerTask = 6
function Run-Mutations($muts) {
    $perTask = @{}; $extra = 0
    foreach ($m in $muts) {
        # Giới hạn theo từng task (6 mỗi task theo checklist), không cắt theo tổng số.
        $tk = [string]$m.task
        if (-not $perTask.ContainsKey($tk)) { $perTask[$tk] = 0 }
        if ($perTask[$tk] -ge $MutationsPerTask) { $extra++; continue }
        $perTask[$tk]++
        $rel = ([string]$m.file) -replace "\\", "/"
        $row = "{0}|{1}|{2}|" -f $m.task, $m.id, $rel
        if ($DryRun) { Add-State "mutations" ($row + "bị bắt (dry-run)"); continue }
        # File do agent chỉ định: phải nằm trong worktree, là file code đã theo dõi, để checkout hoàn tác được.
        $file = Resolve-InWorktree $rel
        if (-not $file) { Add-State "mutations" ($row + "không hợp lệ: đường dẫn nằm ngoài worktree"); continue }
        $tracked = @(Invoke-Git @("ls-files", "--", $rel))
        if ($tracked.Count -eq 0 -or ($rel -match $TestPattern) -or ($rel -notmatch '^(shared|composeApp)/src/')) { Add-State "mutations" ($row + "không hợp lệ: không phải file code đã theo dõi"); continue }
        $text = Read-Text $file
        $cnt = ([regex]::Matches($text, [regex]::Escape([string]$m.find))).Count
        if ($cnt -ne 1) { Add-State "mutations" ($row + "không hợp lệ: 'find' xuất hiện $cnt lần"); continue }
        try {
            Write-Text $file ($text.Replace([string]$m.find, [string]$m.replace))
            $g = Invoke-Gradle (Get-ModuleTasks @($rel)) "mut-$($m.task)-$($m.id)"
        } finally { Invoke-Git @("checkout", "HEAD", "--", $rel) | Out-Null }
        if ($g.Exit -eq 0) { Add-State "mutations" ($row + "SỐNG SÓT (test yếu)") }
        elseif ($g.Compile) { Add-State "mutations" ($row + "không hợp lệ: lỗi biên dịch, không chứng minh test bắt được") }
        else { Add-State "mutations" ($row + "bị bắt") }
    }
    if ($extra -gt 0) { Note "Bỏ $extra mutation vượt quá $MutationsPerTask mỗi task." }
    if (-not $DryRun -and (Get-Changed).Count -gt 0) { Invoke-Git @("checkout", "HEAD", "--", ".") | Out-Null; Stop-Run "BLOCKED" "Working tree không sạch sau mutation, đã hoàn tác." }
}
# Quét từ cấm trong chuỗi hiển thị của các file Kotlin đã đổi; ghi thành phán quyết để báo cáo không thể READY khi còn vi phạm.
function Invoke-CopyLint {
    Reset-Verdicts '^lint_copy\|'
    if ($DryRun) { return }
    $kt = @(Get-DiffFiles | Where-Object { $_ -match '^(shared/src/(commonMain|androidMain)|composeApp/src/main)/.*\.kt$' } | ForEach-Object { Resolve-InWorktree $_ } | Where-Object { $_ -and (Test-Path -LiteralPath $_) })
    if ($kt.Count -eq 0) { return }
    $lint = (& "$Root\scripts\lint-copy.ps1" -Path $kt | Out-String)
    if ($LASTEXITCODE -ne 0) {
        Write-Text "$RunDir\09_lint_copy.txt" $lint
        Add-State "verdicts" "lint_copy|FAIL"; Note "Lint từ cấm thấy vi phạm trong chuỗi hiển thị của code (xem 09_lint_copy.txt)."
    } else { Add-State "verdicts" "lint_copy|PASS" }
}
function Stage-Verify($st) {
    $State.mutations = @(); Reset-Verdicts '^mutation'
    $files = Get-DiffFiles
    $tasks = @(Get-ModuleTasks $files) + @(":composeApp:assembleDebug")
    $g = Invoke-Gradle $tasks "verify-final"
    if ($g.Exit -ne 0) { Stop-Run "BLOCKED" "Verify cuối đỏ (logs\gradle-verify-final.log)" }
    foreach ($c in (Get-TestCounts)) { Add-State "gradle" "đếm|$c|" }
    Invoke-CopyLint
    $r = Invoke-One $st.role (Build-Prompt $st.prompt @{ DIFF_FILES = ($files -join "`n"); PLAN = (Get-Out "plan.json") }) "s09-mut"
    $muts = @(Get-JsonBlock $r.Text)
    Write-Text (Join-Path $RunDir $st.out) $r.Text
    if ($muts.Count -eq 0 -or $null -eq $muts[0]) { Note "Mutation designer không trả JSON hợp lệ; chưa có mutation."; Add-State "verdicts" "mutation|CHƯA CHẠY"; }
    else { Run-Mutations $muts }
    # Task chưa đủ số mutation có kết quả thì ghi rõ là chưa đủ, để báo cáo không coi là đã kiểm.
    if (-not $DryRun -and $PlanObj) {
        foreach ($t in $PlanObj.tasks) {
            $ran = @(@($State.mutations) | Where-Object { $_.StartsWith("$($t.id)|") -and $_ -match "\|(bị bắt|SỐNG SÓT)" }).Count
            if ($ran -lt $MutationsPerTask) { Add-State "verdicts" "mutation $($t.id): $ran/$MutationsPerTask có kết quả|CHƯA ĐỦ" }
        }
    }
    $surv = @(@($State.mutations) | Where-Object { $_ -match "SỐNG SÓT" }).Count
    $valid = @(@($State.mutations) | Where-Object { $_ -match "\|bị bắt" }).Count
    Add-State "verdicts" ("mutation|bị bắt $valid, sống sót $surv")
    Add-State "stages" "$($st.id)|xong|mutation bị bắt $valid, sống sót $surv"
}
function Verify-Summary { return ((@($State.gradle) -join "`n") + "`n" + (@($State.mutations) -join "`n")) }
function Stage-Bugcheck($st) {
    Reset-Verdicts '^bugcheck\|'
    $dev = Invoke-Device
    $State.shots = @($dev.Shots); Save-State
    Add-State "stages" "device|ghi nhận|$($dev.Note)"
    $bugs = ""
    for ($i = 0; $i -le $MaxFixLoops; $i++) {
        $r = Invoke-One $st.role (Build-Prompt $st.prompt @{ DIFF_FILES = ((Get-DiffFiles) -join "`n"); VERIFY = (Verify-Summary); DEVICE = $dev.Note }) "s10-$i"
        $bugs = $r.Text; Write-Text (Join-Path $RunDir $st.out) $bugs
        if ($r.Tag -eq "PASS" -and -not $dev.Crash) { Add-State "stages" "$($st.id)|PASS|lặp $i"; Add-State "verdicts" "bugcheck|PASS"; return }
        # Chỉ RESULT: PASS mới là đạt. Thiếu hoặc sai nhãn thì không coi là đạt và cũng không tự sửa theo nội dung không rõ.
        if ($r.Tag -ne "PASS" -and $r.Tag -ne "FAIL") {
            Note "Bug hunter không trả RESULT: PASS hoặc FAIL (nhận '$($r.Tag)'); không coi là đạt."
            Add-State "stages" "$($st.id)|không rõ|thiếu RESULT hợp lệ, xem 10_bugs.md"; Add-State "verdicts" "bugcheck|LỖI"; return
        }
        if ($i -eq $MaxFixLoops) { break }
        $fb = "Bug cần sửa:`n$bugs"
        if ($dev.Crash) { $fb = "App crash khi khởi động trên thiết bị. Log:`n" + (Clip (Get-Out "logs\crash.txt") 2500) + "`n`n" + $fb }
        Fix-Code $fb "s10fix-$i"
        # Cài lại bản vừa sửa để trạng thái crash phản ánh code mới, không phải lần chạy đầu.
        $dev = Invoke-Device
        $State.shots = @($dev.Shots); Save-State
    }
    Add-State "stages" "$($st.id)|còn bug S1/S2|xem 10_bugs.md"; Add-State "verdicts" "bugcheck|FAIL"
}
function Fix-Code([string]$feedback, [string]$label) {
    Invoke-Executors @($PlanObj.tasks) $label "Sửa theo phản hồi sau (chỉ trong file được sửa, không sửa test):`n$(Clip $feedback 6000)"
    $g = Invoke-Gradle ((Get-ModuleTasks (Get-DiffFiles)) + @(":composeApp:assembleDebug")) "$label"
    if ($g.Exit -ne 0) { Stop-Run "BLOCKED" "Sau khi sửa ($label) Gradle đỏ (logs\gradle-$label.log)" }
    Invoke-CopyLint
    $State.fixLoops = [int]$State.fixLoops + 1; Save-State
}
function Stage-Review($st) {
    Reset-Verdicts '^review1\|'
    $bugs = Get-Out "10_bugs.md"; $r = $null
    for ($i = 0; $i -le $MaxFixLoops; $i++) {
        $r = Invoke-One $st.role (Build-Prompt $st.prompt @{ PLAN = (Get-Out "plan.json"); VERIFY = (Verify-Summary); BUGS = $bugs; DIFF_FILES = ((Get-DiffFiles) -join "`n") }) "s11-$i"
        Write-Text (Join-Path $RunDir $st.out) $r.Text
        if ($r.Tag -eq "PASS") { Add-State "stages" "$($st.id)|PASS|lặp $i"; Add-State "verdicts" "review1|PASS"; return }
        if ($i -eq $MaxFixLoops) { break }
        Fix-Code "Review đợt 1 yêu cầu sửa:`n$($r.Text)" "s11fix-$i"
    }
    Add-State "stages" "$($st.id)|FAIL|xem 11_review1.md"; Add-State "verdicts" "review1|FAIL"
}
function Stage-Personas($st) {
    Reset-Verdicts '^customer_'
    $shots = if (@($State.shots).Count -gt 0) { (@($State.shots) -join "`n") } else { "(không có ảnh thật)" }
    $names = @{ customer_mai = "Chị Mai (phụ huynh bận rộn, lướt nhanh tủ lạnh, thêm đồ sau khi đi chợ)"; customer_nam = "Anh Nam (người cùng nấu ăn, quan tâm gợi ý món, nguyên liệu, thao tác sau khi nấu)"; customer_hanh = "Cô Hạnh (thành viên lớn tuổi, cần chữ lớn, thao tác đơn giản, hiểu cài đặt và sao lưu)" }
    $design = Get-Out "04_design_spec.md"; if (-not $design) { $design = "(Không có thay đổi giao diện; đánh giá theo kế hoạch)`n" + (Get-Out "plan.json") }
    $specs = @()
    foreach ($role in @($st.roles)) { $specs += @{ Role = $role; Label = "s12-$role"; Cwd = $WT; Prompt = (Build-Prompt "persona" @{ PERSONA = $names[$role]; DESIGN = $design; SHOTS = $shots }) } }
    $res = Invoke-Agents $specs
    foreach ($role in @($st.roles)) {
        $r = $res["s12-$role"]; if (-not $r) { $r = $res["s12-$role-retry"] }
        if (-not $r -or -not $r.Ok) { Add-State "verdicts" "$role|LỖI"; continue }
        Write-Text "$RunDir\12_persona_$role.md" $r.Text
        Add-State "verdicts" "$role|$($r.Tag)"
    }
    Add-State "stages" "$($st.id)|xong|"
}
function Stage-Final($st) {
    $pers = ""; foreach ($role in @("customer_mai", "customer_nam", "customer_hanh")) { $pers += "### $role`n" + (Get-Out "12_persona_$role.md") + "`n" }
    $r = Invoke-One $st.role (Build-Prompt $st.prompt @{ PERSONAS = $pers; REVIEW1 = (Get-Out "11_review1.md"); PLAN = (Get-Out "plan.json") }) $st.id
    Write-Text (Join-Path $RunDir $st.out) $r.Text
    Add-State "stages" "$($st.id)|xong|"
}
function Write-Evidence {
    if ($DryRun -or $null -eq $PlanObj) { return }
    foreach ($t in $PlanObj.tasks) {
        if (-not (Test-ContractPath ([string]$t.contract))) { continue }
        $f = Resolve-InWorktree ([string]$t.contract); if (-not $f -or -not (Test-Path -LiteralPath $f -PathType Leaf)) { continue }
        $sec = "`n`n## Biên bản tự động — run $RunId`n`nSinh bởi scripts/orchestrate.ps1 từ kết quả chạy thật; chi tiết ở .agents/runs/$RunId/.`n`n| Kiểm tra | Kết quả |`n| --- | --- |`n"
        foreach ($g in @($State.gradle)) { $p = $g -split "\|"; if ($p.Count -ge 3) { $sec += "| Gradle ``$($p[1])`` ($($p[0])) | $($p[2]) |`n" } }
        foreach ($m in @($State.mutations)) { $p = $m -split "\|"; if ($p[0] -eq $t.id) { $sec += "| Mutation $($p[1]) ($($p[2])) | $($p[3]) |`n" } }
        [IO.File]::AppendAllText($f, $sec, $Utf8)
    }
    Commit-All "docs(tests): biên bản tự động run $RunId"
}

function Build-Report {
    $o = $State.outcome
    if (-not $o -and @($State.done).Count -lt @($Flow.stages).Count) { $o = "PARTIAL"; $State.outcome = $o }
    if (-not $o) {
        $bad = @(@($State.verdicts) | Where-Object { $_ -match "\|(FAIL|LỖI|CHƯA CHẠY|CHƯA ĐỦ)$" }).Count
        $weak = @(@($State.mutations) | Where-Object { $_ -match "SỐNG SÓT|không hợp lệ" }).Count
        $chg = @(@($State.verdicts) | Where-Object { $_ -match "\|CHANGES$" }).Count
        if ($bad -gt 0 -or $weak -gt 0) { $o = "NEEDS_ATTENTION" } elseif ($chg -gt 0) { $o = "READY_WITH_FEEDBACK" } else { $o = "READY" }
        $State.outcome = $o
    }
    $L = New-Object System.Collections.ArrayList
    [void]$L.Add("# Báo cáo cuối — run $RunId`n")
    [void]$L.Add("**Kết quả: $o**`n")
    [void]$L.Add("Ý tưởng: $(Clip $State.idea 600)`n")
    [void]$L.Add("- Nhánh: ``$($State.branch)`` | Worktree: ``$($State.worktree)`` | Base: ``$($State.base)``")
    [void]$L.Add("- Lượt gọi agent: $($State.calls)/$MaxAgentCalls | Vòng sửa: $($State.fixLoops)/$MaxFixLoops mỗi nhóm | Chế độ: $(if ($DryRun) { 'DRY-RUN (không phải kết quả thật)' } else { 'thật' })`n")
    [void]$L.Add("## Các bước`n`n| Bước | Trạng thái | Chi tiết |`n| --- | --- | --- |")
    foreach ($s in @($State.stages)) { $p = ($s -split "\|", 3); [void]$L.Add("| $($p[0]) | $($p[1]) | $($p[2]) |") }
    if ($PlanObj) { [void]$L.Add("`n## Task`n"); foreach ($t in $PlanObj.tasks) { [void]$L.Add("- ``$($t.id)`` $($t.title) ($($t.kind)); contract ``$($t.contract)``") } }
    [void]$L.Add("`n## Gradle (bằng chứng)`n"); foreach ($g in @($State.gradle)) { [void]$L.Add("- $g") }
    [void]$L.Add("`n## Mutation`n`n| Task | ID | File | Kết quả |`n| --- | --- | --- | --- |")
    foreach ($m in @($State.mutations)) { $p = $m -split "\|"; if ($p.Count -ge 4) { [void]$L.Add("| $($p[0]) | $($p[1]) | $($p[2]) | $($p[3]) |") } }
    [void]$L.Add("`n## Phán quyết`n"); foreach ($v in @($State.verdicts)) { [void]$L.Add("- $v") }
    [void]$L.Add("`n## Ghi chú và lệch so với thiết kế`n")
    foreach ($n in @($State.notes)) { [void]$L.Add("- $n") }
    $fb = @($State.fellBack)
    if ($fb.Count -gt 0) { [void]$L.Add("- Vai khai báo Gemini đã chạy bằng model dự phòng (máy chưa có gemini CLI hoặc lượt gọi Gemini lỗi): $($fb -join ', ').") }
    [void]$L.Add("- Persona là walkthrough giả lập, không phải nghiên cứu người dùng thật. Ảnh thật: $(if (@($State.shots).Count) { @($State.shots).Count } else { 'không có' }).")
    [void]$L.Add("`n## Việc cần bạn (một cổng duy nhất)`n")
    [void]$L.Add("Script không push, không mở PR, không merge. Xem diff rồi tự quyết:`n")
    [void]$L.Add('```')
    [void]$L.Add("git diff --stat $($State.base)..$($State.branch)")
    [void]$L.Add("git merge $($State.branch)        # khi đồng ý")
    [void]$L.Add("git worktree remove `"$($State.worktree)`"; git branch -D $($State.branch)   # khi bỏ")
    [void]$L.Add('```')
    [void]$L.Add("`nĐầu ra chi tiết: ``13_next_iteration.md`` (backlog từ phản hồi), ``10_bugs.md``, ``11_review1.md``, ``12_persona_*.md``.")
    $path = Join-Path $RunDir "FINAL_REPORT.md"
    Write-Text $path ($L -join "`n"); Save-State
    return $path
}

# ---------- Chạy ----------
$order = @($Flow.stages | ForEach-Object { $_.id })
$fromIdx = 0; $toIdx = $order.Count - 1
if ($From) { $fromIdx = [Array]::IndexOf($order, $From); if ($fromIdx -lt 0) { throw "Bước không có: $From" } }
if ($To) { $toIdx = [Array]::IndexOf($order, $To); if ($toIdx -lt 0) { throw "Bước không có: $To" } }
# Review thiết kế chạy trong vòng của s04 nên không thể bắt đầu riêng từ s05.
if ($From -eq "s05_design_review") { throw "Không chạy riêng s05_design_review; dùng -From s04_design để thiết kế và review lại." }
Load-Plan
Step "Run $RunId$(if ($DryRun) { ' [DRY-RUN]' }); báo cáo cuối tại $RunDir\FINAL_REPORT.md"
Log-Event @{ kind = "start"; dryRun = [bool]$DryRun }

try {
    # Tạo worktree trước mọi bước, kể cả khi bắt đầu giữa chừng bằng -From, để git không bao giờ chạy trên checkout của người dùng.
    Setup-Worktree
    for ($i = $fromIdx; $i -le $toIdx; $i++) {
        $st = $Flow.stages[$i]
        if (@($State.done) -contains $st.id -and -not $From) { Step "Bỏ qua $($st.id) (đã xong)"; continue }
        Step "=== $($st.id) ==="
        switch ($st.id) {
            "s01_idea"          { Stage-Idea $st }
            "s02_evaluate"      { Stage-Evaluate $st }
            "s03_plan"          { Stage-Plan $st }
            "s04_design"        { Stage-Design $st }
            "s05_design_review" {
                # Review đã chạy trong vòng của s04; chỉ coi là xong khi có kết quả PASS (hoặc không có task UI).
                if ((Test-HasUi) -and -not (@($State.stages) -match '^s05_design_review\|PASS\|')) { Stop-Run "BLOCKED" "Chưa có kết quả review thiết kế PASS; chạy lại từ s04_design" }
            }
            "s06_handoff"       { Stage-Handoff $st }
            "s07_tests_first"   { Stage-TestsFirst $st }
            "s08_code"          { Stage-Code $st }
            "s09_verify"        { Stage-Verify $st }
            "s10_bugcheck"      { Stage-Bugcheck $st }
            "s11_review1"       { Stage-Review $st }
            "s12_review2"       { Stage-Personas $st }
            "s13_final"         { Stage-Final $st }
        }
        $State.done = @($State.done) + $st.id; Save-State
    }
    Write-Evidence
} catch {
    $msg = $_.Exception.Message
    if ($msg -like "STOP|*") {
        $p = $msg -split "\|", 3
        $State.outcome = $p[1]; Add-State "stages" ("dừng|{0}|{1}" -f $p[1], $p[2])
    } else {
        $State.outcome = "ERROR"; Add-State "stages" "lỗi script|ERROR|$msg"
        Add-State "notes" ("Lỗi script: {0} ({1})" -f $msg, $_.ScriptStackTrace.Split("`n")[0].Trim())
    }
    try { Write-Evidence } catch { }
}
$report = Build-Report
Step "XONG — kết quả: $($State.outcome). Đọc: $report"
if ($State.outcome -eq "ERROR") { exit 2 }
exit 0
