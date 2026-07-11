param(
  [string]$ContainerName = "ob-admin-local",
  [string]$AdminDb = "his_admin",
  [string]$AdminUser = "admin_app",
  [string]$AdminPassword = "Admin123456!"
)

$ErrorActionPreference = "Stop"

docker info | Out-Null

$running = docker ps --format "{{.Names}}" | Select-String -SimpleMatch $ContainerName
if (-not $running) {
  throw "OceanBase container '$ContainerName' is not running."
}

$result = docker exec $ContainerName obclient -h 127.0.0.1 -P 2881 -u root@sys -D oceanbase -A -e "SHOW DATABASES LIKE '$AdminDb';"
if ($LASTEXITCODE -ne 0) {
  throw "Unable to query OceanBase databases from container '$ContainerName'."
}
$resultText = $result | Out-String
if ($resultText -notmatch $AdminDb) {
  throw "Database '$AdminDb' was not found in OceanBase."
}

$loginCheck = docker exec $ContainerName obclient -h 127.0.0.1 -P 2881 -u $AdminUser "-p$AdminPassword" -D $AdminDb -A -e "SELECT DATABASE();"
if ($LASTEXITCODE -ne 0) {
  throw "Unable to log in as '$AdminUser' and access database '$AdminDb'."
}
$loginCheckText = $loginCheck | Out-String
if ($loginCheckText -notmatch $AdminDb) {
  throw "Login check did not return database '$AdminDb'."
}

Write-Host "OceanBase local check passed."
