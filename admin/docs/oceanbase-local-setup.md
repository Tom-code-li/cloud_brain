# Local OceanBase Setup

1. Start Docker Desktop and confirm `docker info` succeeds.
2. Ensure `%UserProfile%\.wslconfig` includes at least `memory=10GB`, then run `wsl --shutdown` and restart Docker Desktop.
3. Run `powershell -ExecutionPolicy Bypass -File .\infra\oceanbase\scripts\start-local-oceanbase.ps1`.
4. Run `powershell -ExecutionPolicy Bypass -File .\infra\oceanbase\scripts\check-local-oceanbase.ps1`.
5. Ensure MySQL `doctor_platform` is running on port `3306`.
6. Load the admin schema with:

```powershell
Get-Content -Raw .\backen\sql\admin-domain-oceanbase.sql | mysql -h 127.0.0.1 -P 2881 -u admin_app -pAdmin123456! his_admin
```

7. Synchronize the existing MySQL admin-domain data into OceanBase:

```powershell
powershell -ExecutionPolicy Bypass -File .\backen\sql\sync-admin-domain-to-oceanbase.ps1
```

8. Start the backend:

```powershell
Set-Location .\backen
mvn spring-boot:run
```

9. Verify these APIs return HTTP `200`:
   - `POST /api/auth/login`
   - `GET /api/admin/department`
   - `GET /api/admin/doctor?pageNum=1&pageSize=10`
   - `GET /api/admin/schedules?startDate=2026-07-08&endDate=2026-07-08`

10. If OceanBase startup fails with memory errors, re-check Docker Desktop memory from `docker info` and confirm `Total Memory` is close to `10 GiB`.
