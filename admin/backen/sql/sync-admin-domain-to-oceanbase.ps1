$ErrorActionPreference = "Stop"

$sourceHost = "127.0.0.1"
$sourcePort = 3306
$sourceDb = "doctor_platform"
$sourceUser = "root"
$sourcePassword = "123456"

$targetHost = "127.0.0.1"
$targetPort = 2881
$targetDb = "his_admin"
$targetUser = "admin_app"
$targetPassword = "Admin123456!"

$tables = @(
  "sys_role",
  "sys_user",
  "department",
  "doctor",
  "doctor_schedule",
  "ai_schedule_suggestion",
  "ai_schedule_suggestion_detail"
)

$truncateOrder = @(
  "ai_schedule_suggestion_detail",
  "ai_schedule_suggestion",
  "doctor_schedule",
  "doctor",
  "department",
  "sys_user",
  "sys_role"
)

$tmpDir = Join-Path $PSScriptRoot "tmp-admin-sync"
New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null

mysql -h $targetHost -P $targetPort -u $targetUser "-p$targetPassword" $targetDb -e "SET foreign_key_checks = 0;"
if ($LASTEXITCODE -ne 0) {
  throw "Failed to disable foreign key checks in OceanBase target database."
}

foreach ($table in $truncateOrder) {
  mysql -h $targetHost -P $targetPort -u $targetUser "-p$targetPassword" $targetDb -e "DELETE FROM $table;"
  if ($LASTEXITCODE -ne 0) {
    throw "Failed to clear target table '$table' before synchronization."
  }
}

foreach ($table in $tables) {
  $dumpFile = Join-Path $tmpDir "$table.sql"
  mysqldump -h $sourceHost -P $sourcePort -u $sourceUser "-p$sourcePassword" --skip-triggers --no-create-info --compact $sourceDb $table > $dumpFile
  if ($LASTEXITCODE -ne 0) {
    throw "Failed to export source table '$table' from MySQL."
  }

  $dumpSql = Get-Content -Raw $dumpFile
  $dumpSql = $dumpSql -replace 'INSERT INTO', 'INSERT IGNORE INTO'
  $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
  [System.IO.File]::WriteAllText($dumpFile, $dumpSql, $utf8NoBom)

  Get-Content -Raw $dumpFile | mysql -h $targetHost -P $targetPort -u $targetUser "-p$targetPassword" $targetDb
  if ($LASTEXITCODE -ne 0) {
    throw "Failed to import table '$table' into OceanBase."
  }
}

mysql -h $targetHost -P $targetPort -u $targetUser "-p$targetPassword" $targetDb -e "SET foreign_key_checks = 1;"
if ($LASTEXITCODE -ne 0) {
  throw "Failed to re-enable foreign key checks in OceanBase target database."
}

Write-Host "Admin-domain data synchronized into OceanBase."
