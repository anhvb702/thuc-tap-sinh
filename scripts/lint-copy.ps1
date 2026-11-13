# Quét từ cấm trong văn phong (CLAUDE.md / AGENTS.override.md). Thoát 1 nếu có vi phạm.
# Dùng: .\scripts\lint-copy.ps1 -Path file1,file2   (file .md, .kt, .txt)
param([Parameter(Mandatory = $true)][string[]]$Path)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Cụm từ cấm. "hết hạn" chỉ cấm khi là chữ hiển thị; mã/identifier tiếng Anh không bị quét.
$banned = @(
    "bạn quên", "lãng phí", "bỏ lỡ", "hết hạn", "streak", "chuỗi ngày liên tiếp",
    "điểm số", "xếp hạng", "so với người khác", "đáng xấu hổ"
)

$hits = @()
foreach ($p in $Path) {
    if (-not (Test-Path -LiteralPath $p)) { continue }
    $lineNo = 0
    foreach ($line in (Get-Content -LiteralPath $p -Encoding UTF8)) {
        $lineNo++
        $low = $line.ToLowerInvariant()
        foreach ($w in $banned) {
            if ($low.Contains($w)) {
                # Dòng đang liệt kê từ cấm (trích dẫn quy tắc) thì bỏ qua.
                if ($low -match "cấm|không dùng|tránh|banned|không được") { continue }
                $hits += ("{0}:{1}: '{2}' -> {3}" -f $p, $lineNo, $w, $line.Trim())
            }
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
