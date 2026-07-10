$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..
docker compose up -d

Write-Host "Waiting for OceanBase container to accept connections..."
for ($i = 0; $i -lt 30; $i++) {
  try {
    docker exec ob-admin-local obclient -h 127.0.0.1 -P 2881 -u root@sys -D oceanbase -A -e "SELECT 1;" | Out-Null
    if ($LASTEXITCODE -eq 0) {
      Write-Host "OceanBase is ready."
      break
    }
  } catch {
    Start-Sleep -Seconds 10
  }
}

if ($i -ge 30) {
  throw "OceanBase did not become ready within 5 minutes."
}

docker exec ob-admin-local obclient -h 127.0.0.1 -P 2881 -u root@sys -D oceanbase -A -e "source /docker-entrypoint-initdb.d/01-create-admin-db.sql"
if ($LASTEXITCODE -ne 0) {
  throw "OceanBase started, but admin database bootstrap failed."
}
