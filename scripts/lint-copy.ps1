# Quét từ cấm trong văn phong (CLAUDE.md / AGENTS.override.md). Thoát 1 nếu có vi phạm.
# Dùng: .\scripts\lint-copy.ps1 -Path file1,file2   (file .md, .kt, .txt)
# Dòng chỉ trích dẫn quy tắc (ví dụ liệt kê từ cấm) cần ghi thêm "lint-copy: rule" để được bỏ qua.
param([Parameter(Mandatory = $true)][string[]]$Path)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Cụm từ cấm. "hết hạn" chỉ cấm khi là chữ hiển thị; mã/identifier tiếng Anh không bị quét.
$banned = @(
    "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "streak", "chuỗi ngày liên tiếp",
    "điểm số", "xếp hạng", "so với người khác", "đáng xấu hổ"
)

# Chỉ dòng tự đánh dấu "lint-copy: rule" (dòng trích dẫn quy tắc) mới được bỏ qua. Từ khóa như "tránh" hay
# "không dùng" không miễn trừ cả dòng, vì chúng cũng xuất hiện trong chữ hiển thị thật.
$ruleMarker = 'lint-copy:\s*rule'
# File Kotlin chỉ quét nội dung chuỗi "..." để không bắt nhầm tên biến hay chú thích.
$kotlinString = [regex]'"((?:[^"\\]|\\.)*)"'

$hits = @()
foreach ($p in $Path) {
    if (-not (Test-Path -LiteralPath $p)) { continue }
    $isKotlin = $p -match '\.kts?$'
    $lineNo = 0
    foreach ($line in (Get-Content -LiteralPath $p -Encoding UTF8)) {
        $lineNo++
        if ($line -match $ruleMarker) { continue }
        $scan = $line
        if ($isKotlin) { $scan = (@($kotlinString.Matches($line) | ForEach-Object { $_.Groups[1].Value }) -join "`n") }
        $low = $scan.ToLowerInvariant()
        foreach ($w in $banned) {
            if ($low.Contains($w)) { $hits += ("{0}:{1}: '{2}' -> {3}" -f $p, $lineNo, $w, $line.Trim()) }
        }
    }
}

if ($hits.Count -gt 0) {
    $hits | ForEach-Object { Write-Output $_ }
    Write-Output ("LINT: FAIL ({0} vi phạm)" -f $hits.Count)
    exit 1
}
Write-Output "LINT: PASS"
exit 0
