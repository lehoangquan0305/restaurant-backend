# Smoke test for restaurant backend
# Usage: Open PowerShell, cd to d:\backend\scripts and run: .\smoke-test.ps1
# It will attempt to log in as admin/admin (or admin/123) and perform basic CRUD via API.

$apiBase = "http://localhost:8080"
$adminUser = Read-Host "Admin username (default: admin)" -Prompt "Admin username" -AsSecureString
# Note: Read-Host with -AsSecureString returns SecureString; for simplicity we fallback to default below if empty
$username = $null
try { $username = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($adminUser)) } catch { $username = 'admin' }
$password = Read-Host "Admin password (default: 123)"
if ([string]::IsNullOrWhiteSpace($password)) { $password = '123' }
if ([string]::IsNullOrWhiteSpace($username)) { $username = 'admin' }

function Login($user, $pass){
    try{
        $resp = Invoke-RestMethod -Method Post -Uri "$apiBase/api/auth/login" -ContentType 'application/json' -Body (@{ username=$user; password=$pass } | ConvertTo-Json)
        return $resp.token
    } catch {
        Write-Host "Login failed: $_" -ForegroundColor Red
        return $null
    }
}

token = Login $username $password
if (-not $token) { Write-Host "Cannot continue without token. Fix backend and credentials."; exit 1 }

$headers = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }

Write-Host "Creating a test table..." -ForegroundColor Cyan
$tbl = @{ name = "Test Table 1"; capacity = 4; status = "AVAILABLE" } | ConvertTo-Json
$createdTable = Invoke-RestMethod -Method Post -Uri "$apiBase/api/tables" -Headers $headers -Body $tbl
Write-Host "Created table id:" $createdTable.id

Write-Host "Creating a test menu item..." -ForegroundColor Cyan
$menu = @{ name = "Test Dish"; description = "Smoke test item"; price = 50000; category = "Test"; available = $true } | ConvertTo-Json
$createdMenu = Invoke-RestMethod -Method Post -Uri "$apiBase/api/menu" -Headers $headers -Body $menu
Write-Host "Created menu id:" $createdMenu.id

Write-Host "Creating a test reservation on the created table..." -ForegroundColor Cyan
$res = @{ customerName = "Khach Test"; customerPhone = "0123456789"; partySize = 2; reservationTime = (Get-Date).ToString('s'); table = @{ id = $createdTable.id } } | ConvertTo-Json
$createdRes = Invoke-RestMethod -Method Post -Uri "$apiBase/api/reservations" -Headers $headers -Body $res
Write-Host "Created reservation id:" $createdRes.id

Write-Host "Creating a test employee..." -ForegroundColor Cyan
$roleList = Invoke-RestMethod -Method Get -Uri "$apiBase/api/employees/roles" -Headers $headers
$roleId = $roleList[0].id
$emp = @{ username = "smoketest"; password = "smoke123"; fullName = "Smoke Tester"; email = "smoke@example.com"; phone = "0987654321"; roles = @( @{ id = $roleId } ) } | ConvertTo-Json
$createdEmp = Invoke-RestMethod -Method Post -Uri "$apiBase/api/employees" -Headers $headers -Body $emp
Write-Host "Created employee id:" $createdEmp.id

Write-Host "Smoke test finished. You may want to manually verify data in DB or via GET endpoints." -ForegroundColor Green
