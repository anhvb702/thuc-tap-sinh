# Gọi Jev (TypeSafe System One) qua HTTP.
# Key đọc từ biến môi trường TYPESAFE_API_KEY, không in ra màn hình.
#
# Kiểm tra key:      .\scripts\jev.ps1 -Models
# Gửi câu hỏi:       .\scripts\jev.ps1 -RequestFile .\scripts\jev_request.json
#                    .\scripts\jev.ps1 -RequestFile req.json -OutFile result.json
#
# Định dạng request theo https://docs.typesafe.ai/introduction/quickstart.md:
# { "state": "...", "model": "jev-latest", "questions": { "<tên>": { "type": "choice|score|noul", ... } } }

param(
    [Parameter(Mandatory = $false)]
    [string]$RequestFile = "",

    [Parameter(Mandatory = $false)]
    [string]$OutFile = "",

    [Parameter(Mandatory = $false)]
    [switch]$Models
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$ApiKey = $env:TYPESAFE_API_KEY
if ([string]::IsNullOrWhiteSpace($ApiKey)) {
    Write-Error "Chưa có TYPESAFE_API_KEY trong môi trường. Đặt biến rồi chạy lại."
    exit 1
}

$Headers = @{ Authorization = "Bearer $ApiKey" }

try {
    if ($Models) {
        $resp = Invoke-RestMethod -Method Get -Uri "https://api.typesafe.ai/v1/models" -Headers $Headers -TimeoutSec 30
        $json = $resp | ConvertTo-Json -Depth 10
    }
    else {
        if ([string]::IsNullOrWhiteSpace($RequestFile) -or -not (Test-Path $RequestFile)) {
            Write-Error "Cần -RequestFile trỏ tới file JSON có tồn tại (hoặc dùng -Models)."
            exit 1
        }
        # Đọc bằng UTF-8 và gửi dạng byte để giữ nguyên tiếng Việt.
        $raw = Get-Content -Path $RequestFile -Raw -Encoding UTF8
        $null = $raw | ConvertFrom-Json   # kiểm tra JSON hợp lệ trước khi gửi
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($raw)
        $resp = Invoke-RestMethod -Method Post -Uri "https://api.typesafe.ai/v1/systemone" `
            -Headers $Headers -ContentType "application/json; charset=utf-8" -Body $bytes -TimeoutSec 120
        $json = $resp | ConvertTo-Json -Depth 20
    }
}
catch {
    if ($_.Exception.Response) {
        $status = [int]$_.Exception.Response.StatusCode
        Write-Error ("Jev lỗi HTTP {0}: {1}" -f $status, $_.Exception.Message)
    }
    else {
        Write-Error $_.Exception.Message
    }
    exit 1
}

if ($OutFile) {
    $outPath = if ([System.IO.Path]::IsPathRooted($OutFile)) { $OutFile } else { Join-Path (Get-Location) $OutFile }
    [System.IO.File]::WriteAllText($outPath, $json, (New-Object System.Text.UTF8Encoding($false)))
    Write-Host "Đã ghi kết quả vào $OutFile"
}
else {
    Write-Output $json
}
