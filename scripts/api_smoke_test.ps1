param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Username = "admin",
    [string]$Password = "admin123",
    [string]$FilePath = "$PSScriptRoot\sample.md"
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        $Body,
        [string]$Token
    )
    $headers = @{}
    if ($Token) {
        $headers.Authorization = "Bearer $Token"
    }
    $params = @{
        Method      = $Method
        Uri         = "$BaseUrl$Path"
        Headers     = $headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 10)
    }
    return Invoke-RestMethod @params
}

Write-Host "1. Login"
$login = Invoke-Api -Method Post -Path "/api/auth/login" -Body @{
    username = $Username
    password = $Password
}
$accessToken = $login.accessToken

Write-Host "2. Create knowledge base"
$kb = Invoke-Api -Method Post -Path "/api/knowledge-bases" -Body @{
    name        = "联调知识库"
    description = "API smoke test"
} -Token $accessToken

Write-Host "3. Upload document"
$form = @{ file = Get-Item $FilePath }
$doc = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/knowledge-bases/$($kb.id)/documents" `
    -Headers @{ Authorization = "Bearer $accessToken" } -Form $form

Write-Host "4. Wait for document READY/FAILED"
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 2
    $current = Invoke-Api -Method Get -Path "/api/documents/$($doc.id)" -Token $accessToken
    if ($current.status -in @("READY", "FAILED")) {
        Write-Host "status=$($current.status) chunks=$($current.chunkCount)"
        break
    }
}

Write-Host "5. Create session"
$session = Invoke-Api -Method Post -Path "/api/sessions" -Body @{
    knowledgeBaseId = $kb.id
    title           = "联调会话"
} -Token $accessToken

Write-Host "6. Chat (SSE)"
$chatBody = @{
    knowledgeBaseId = $kb.id
    question        = "这个文档讲了什么？"
    sessionId       = $session.id
} | ConvertTo-Json -Compress
& curl.exe -s -N -X POST "$BaseUrl/api/chat" `
    -H "Authorization: Bearer $accessToken" `
    -H "Content-Type: application/json" `
    -d $chatBody

Write-Host ""
Write-Host "Smoke test finished."
