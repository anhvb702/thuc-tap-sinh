param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("auto", "planner", "advisor", "codex-executor", "claude-haiku", "reviewer", "verify")]
    [string]$Role = "auto",

    [Parameter(Mandatory=$false)]
    [string]$TaskType = "",

    [Parameter(Mandatory=$false)]
    [string]$Workspace = (Get-Location).Path,

    [Parameter(Mandatory=$false)]
    [string]$Prompt = "",

    [Parameter(Mandatory=$false)]
    [string]$PromptFile = ""
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$ClaudeBin = "C:\Users\loitu\.local\bin\claude.exe"
if (-not (Test-Path $ClaudeBin)) { $ClaudeBin = "claude" }

# Tìm codex.exe để không bị vấp ExecutionPolicy của codex.ps1
$CodexBin = "C:\Users\loitu\AppData\Local\OpenAI\Codex\bin\codex.exe"
if (-not (Test-Path $CodexBin)) {
    $foundCodex = Get-ChildItem "C:\Users\loitu\AppData\Local\OpenAI\Codex\bin" -Filter "codex.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($foundCodex) { $CodexBin = $foundCodex.FullName } else { $CodexBin = "codex.cmd" }
}

Set-Location $Workspace
$PipelineDir = Join-Path $Workspace ".agents\pipeline"
if (-not (Test-Path $PipelineDir)) {
    New-Item -ItemType Directory -Path $PipelineDir -Force | Out-Null
}

if ($PromptFile -and (Test-Path $PromptFile)) {
    $FileContent = Get-Content $PromptFile -Raw -Encoding UTF8
    $Prompt = "$Prompt`n`n$FileContent"
}

# --- JEV-STYLE AUTO ROUTER (Phân loại tự động khi Role = "auto") ---
function Select-Route([string]$type, [string]$text) {
    $combined = "$type $text".ToLower()
    if ($combined -match "plan|kiến trúc|architecture|trade-off|spec|contract|advisor") {
        return "planner"
    }
    if ($combined -match "ui|compose|screen|màn hình|giao diện|impeccable|màu|button|card") {
        return "claude-haiku"
    }
    if ($combined -match "review|mutation|kiểm tra|thẩm định|verify") {
        return "reviewer"
    }
    # Mặc định các task code logic, repository, SQLDelight, unit test đẩy về Codex Luna (nhanh & rẻ)
    return "codex-executor"
}

$SelectedRole = if ($Role -eq "auto") { Select-Route -type $TaskType -text $Prompt } else { $Role }
Write-Host "[Auto-Router] Đã định tuyến tác vụ sang: $SelectedRole" -ForegroundColor Cyan

switch ($SelectedRole) {
    "planner" {
        $fullPrompt = @"
Đóng vai Planner & Technical Advisor (theo CLAUDE.md và AGENTS.override.md).
Đọc HANDOFF.md và nội dung yêu cầu dưới đây:
$Prompt

Hãy thực hiện:
1. Phân tích kiến trúc, trade-off và rủi ro kỹ thuật.
2. Chia thành các Task nhỏ độc lập (<= 300 dòng diff), thiết kế Test Contract và 6 kiểu Mutation Test.
3. Lưu kế hoạch chi tiết vào .agents/pipeline/02_plan_packet.md và test contract vào docs/tests/ nếu phù hợp.
"@
        $outFile = Join-Path $PipelineDir "02_plan_packet.md"
        & $ClaudeBin -p $fullPrompt --allowedTools "Read,Grep,Glob,Edit,Write" | Tee-Object -FilePath $outFile
    }

    "advisor" {
        $fullPrompt = @"
Đóng vai Technical Advisor độc lập:
Phân tích yêu cầu, so sánh với PRODUCT.md, HANDOFF.md, CLAUDE.md và đưa ra nhận định về trade-off, rủi ro và khuyến nghị:
$Prompt
"@
        & $ClaudeBin -p $fullPrompt --allowedTools "Read,Grep,Glob"
    }

    "codex-executor" {
        $fullPrompt = @"
Đóng vai Task Executor (theo AGENTS.override.md / yourrefri_builder.toml):
Thực thi đúng yêu cầu sau, giữ diff <= 300 dòng, TUYỆT ĐỐI KHÔNG sửa hoặc làm yếu unit test đã có:
$Prompt
"@
        & $CodexBin exec -m "gpt-6-luna" $fullPrompt
    }

    "claude-haiku" {
        $fullPrompt = @"
Đóng vai UI Executor (sử dụng triết lý thiết kế impeccable: Warm Minimalism, chống AI-slop, vùng chạm >= 48dp):
Thực thi giao diện theo yêu cầu sau (diff <= 300 dòng, không sửa test):
$Prompt
"@
        & $ClaudeBin -p $fullPrompt --model haiku --allowedTools "Read,Grep,Glob,Edit,Write"
    }

    "reviewer" {
        $fullPrompt = @"
Đóng vai Reviewer độc lập:
Kiểm tra git diff hiện tại so với Test Contract trong docs/tests/, kiểm tra tuân thủ Warm Minimalism và đánh giá 6 kiểu mutation test:
$Prompt
"@
        & $ClaudeBin -p $fullPrompt --allowedTools "Read,Grep,Glob,Bash(git diff*)"
    }

    "verify" {
        if (Test-Path ".\gradlew.bat") {
            if (-not $env:JAVA_HOME -and (Test-Path "E:\Dev\Java\jdk-21.0.13+9")) {
                $env:JAVA_HOME = "E:\Dev\Java\jdk-21.0.13+9"
            }
            if (-not $env:ANDROID_HOME -and (Test-Path "E:\Dev\Android\Sdk")) {
                $env:ANDROID_HOME = "E:\Dev\Android\Sdk"
            }
            Write-Host "[Verify Gate] Đang chạy Unit Test (:shared:testDebugUnitTest)..." -ForegroundColor Yellow
            .\gradlew.bat :shared:testDebugUnitTest
        } else {
            Write-Host "[Verify Gate] Không tìm thấy gradlew.bat trong thư mục hiện tại." -ForegroundColor Yellow
        }
    }
}
