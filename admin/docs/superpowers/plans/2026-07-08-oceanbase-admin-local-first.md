# OceanBase Admin Local-First Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Run the admin project locally with OceanBase as the write/read source for admin-domain tables and MySQL as the source for business-domain tables, while adding migration scripts and a cutover-ready runbook.

**Architecture:** Keep the current backend as a single Spring Boot application, but split persistence into two explicit MyBatis-Plus data sources: `admin` for OceanBase MySQL mode and `biz` for the existing MySQL database. Move admin-domain mappers under an admin mapper scan, keep transaction-domain mappers under a business mapper scan, and add local Docker-based OceanBase bootstrap plus SQL migration assets so the split can be verified on one Windows workstation before OMS-based production cutover.

**Tech Stack:** Spring Boot 3.5, MyBatis-Plus 3.5, MySQL Connector/J, OceanBase Community Edition in MySQL-compatible mode, Docker Desktop/WSL2, PowerShell, JUnit 5, H2 for configuration tests.

---

## Planned File Structure

- Create: `infra/oceanbase/docker-compose.yml`
- Create: `infra/oceanbase/scripts/start-local-oceanbase.ps1`
- Create: `infra/oceanbase/scripts/check-local-oceanbase.ps1`
- Create: `infra/oceanbase/sql/01-create-admin-db.sql`
- Create: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/AdminDataSourceConfig.java`
- Create: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/BizDataSourceConfig.java`
- Create: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/AdminDataSourceProperties.java`
- Create: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/BizDataSourceProperties.java`
- Create: `backen/src/test/java/com/neuCloudBrainMedical/admin/config/DualDataSourceContextTest.java`
- Create: `backen/src/test/java/com/neuCloudBrainMedical/admin/config/MapperDataSourceRoutingTest.java`
- Create: `backen/src/test/resources/application-dual-ds-test.properties`
- Create: `backen/src/test/resources/sql/admin-schema.sql`
- Create: `backen/src/test/resources/sql/biz-schema.sql`
- Create: `backen/sql/admin-domain-oceanbase.sql`
- Create: `backen/sql/admin-domain-mysql-mirror.sql`
- Create: `backen/sql/sync-admin-domain-to-oceanbase.ps1`
- Create: `docs/oceanbase-local-setup.md`
- Modify: `backen/src/main/resources/application.properties`
- Modify: `backen/src/main/java/com/neuCloudBrainMedical/admin/AdminApplication.java`
- Modify: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/MyBatisPlusConfig.java`
- Move/Modify: admin-domain mappers from `backen/src/main/java/com/neuCloudBrainMedical/admin/mapper/**` into `backen/src/main/java/com/neuCloudBrainMedical/admin/mapper/admin/**`
- Move/Modify: business-domain mappers into `backen/src/main/java/com/neuCloudBrainMedical/admin/mapper/biz/**`

> **Workspace note:** `C:\Users\李博\OneDrive\桌面\admin` currently is not a Git checkout. If implementation happens inside a real repository later, execute the commit steps as written. If Git is still absent, treat commit steps as checkpoints and initialize Git before merging.

### Task 1: Bootstrap a local OceanBase environment

**Files:**
- Create: `infra/oceanbase/docker-compose.yml`
- Create: `infra/oceanbase/scripts/start-local-oceanbase.ps1`
- Create: `infra/oceanbase/scripts/check-local-oceanbase.ps1`
- Create: `infra/oceanbase/sql/01-create-admin-db.sql`
- Test: `infra/oceanbase/scripts/check-local-oceanbase.ps1`

- [ ] **Step 1: Write the failing local health-check script**

```powershell
param(
  [string]$ContainerName = "ob-admin-local",
  [string]$AdminDb = "his_admin"
)

$ErrorActionPreference = "Stop"

docker info | Out-Null

$running = docker ps --format "{{.Names}}" | Select-String -SimpleMatch $ContainerName
if (-not $running) {
  throw "OceanBase container '$ContainerName' is not running."
}

$result = docker exec $ContainerName ob-mysql root -e "SHOW DATABASES LIKE '$AdminDb';"
if ($result -notmatch $AdminDb) {
  throw "Database '$AdminDb' was not found in OceanBase."
}

Write-Host "OceanBase local check passed."
```

- [ ] **Step 2: Run the health check and verify it fails**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin
powershell -ExecutionPolicy Bypass -File .\infra\oceanbase\scripts\check-local-oceanbase.ps1
```

Expected: FAIL with either Docker daemon unavailable or container/database missing.

- [ ] **Step 3: Write the minimal OceanBase Docker bootstrap**

`infra/oceanbase/docker-compose.yml`

```yaml
services:
  oceanbase:
    image: oceanbase/oceanbase-ce
    container_name: ob-admin-local
    environment:
      MINI_MODE: "1"
    ports:
      - "2881:2881"
    volumes:
      - ./sql:/docker-entrypoint-initdb.d
```

`infra/oceanbase/scripts/start-local-oceanbase.ps1`

```powershell
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..
docker compose up -d

Write-Host "Waiting for OceanBase container to accept connections..."
for ($i = 0; $i -lt 30; $i++) {
  try {
    docker exec ob-admin-local ob-mysql root -e "SELECT 1;" | Out-Null
    Write-Host "OceanBase is ready."
    exit 0
  } catch {
    Start-Sleep -Seconds 10
  }
}

throw "OceanBase did not become ready within 5 minutes."
```

`infra/oceanbase/sql/01-create-admin-db.sql`

```sql
CREATE DATABASE IF NOT EXISTS his_admin DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'admin_app' IDENTIFIED BY 'Admin123456!';
GRANT ALL PRIVILEGES ON his_admin.* TO 'admin_app';
```

- [ ] **Step 4: Start OceanBase and verify the health check passes**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin
powershell -ExecutionPolicy Bypass -File .\infra\oceanbase\scripts\start-local-oceanbase.ps1
powershell -ExecutionPolicy Bypass -File .\infra\oceanbase\scripts\check-local-oceanbase.ps1
```

Expected: PASS with `OceanBase local check passed.`

- [ ] **Step 5: Commit**

```bash
git add infra/oceanbase
git commit -m "chore: add local oceanbase bootstrap"
```

### Task 2: Introduce dual-datasource configuration contracts

**Files:**
- Create: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/AdminDataSourceProperties.java`
- Create: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/BizDataSourceProperties.java`
- Create: `backen/src/test/java/com/neuCloudBrainMedical/admin/config/DualDataSourceContextTest.java`
- Create: `backen/src/test/resources/application-dual-ds-test.properties`
- Modify: `backen/src/main/resources/application.properties`
- Test: `backen/src/test/java/com/neuCloudBrainMedical/admin/config/DualDataSourceContextTest.java`

- [ ] **Step 1: Write the failing Spring context test**

```java
package com.neuCloudBrainMedical.admin.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dual-ds-test")
class DualDataSourceContextTest {

    @Autowired
    private DataSource adminDataSource;

    @Autowired
    private DataSource bizDataSource;

    @Test
    void loadsBothNamedDataSources() {
        assertThat(adminDataSource).isNotNull();
        assertThat(bizDataSource).isNotNull();
    }
}
```

- [ ] **Step 2: Run the test and verify it fails**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd -Dtest=DualDataSourceContextTest test
```

Expected: FAIL because `adminDataSource` and `bizDataSource` beans do not exist yet.

- [ ] **Step 3: Add the properties classes and configuration values**

`backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/AdminDataSourceProperties.java`

```java
package com.neuCloudBrainMedical.admin.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource.admin")
public class AdminDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
}
```

`backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/BizDataSourceProperties.java`

```java
package com.neuCloudBrainMedical.admin.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource.biz")
public class BizDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
}
```

`backen/src/test/resources/application-dual-ds-test.properties`

```properties
app.datasource.admin.driver-class-name=org.h2.Driver
app.datasource.admin.url=jdbc:h2:mem:admin;MODE=MySQL;DB_CLOSE_DELAY=-1
app.datasource.admin.username=sa
app.datasource.admin.password=

app.datasource.biz.driver-class-name=org.h2.Driver
app.datasource.biz.url=jdbc:h2:mem:biz;MODE=MySQL;DB_CLOSE_DELAY=-1
app.datasource.biz.username=sa
app.datasource.biz.password=
```

`backen/src/main/resources/application.properties`

```properties
app.datasource.admin.driver-class-name=com.mysql.cj.jdbc.Driver
app.datasource.admin.url=jdbc:mysql://127.0.0.1:2881/his_admin?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
app.datasource.admin.username=admin_app
app.datasource.admin.password=Admin123456!

app.datasource.biz.driver-class-name=com.mysql.cj.jdbc.Driver
app.datasource.biz.url=jdbc:mysql://localhost:3306/doctor_platform?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
app.datasource.biz.username=root
app.datasource.biz.password=123456
```

- [ ] **Step 4: Run the context test again**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd -Dtest=DualDataSourceContextTest test
```

Expected: still FAIL, but now only because the actual configuration beans have not been wired.

- [ ] **Step 5: Commit**

```bash
git add backen/src/main/resources/application.properties backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource backen/src/test/java/com/neuCloudBrainMedical/admin/config/DualDataSourceContextTest.java backen/src/test/resources/application-dual-ds-test.properties
git commit -m "test: define dual datasource configuration contract"
```

### Task 3: Wire Spring Boot and MyBatis-Plus for two explicit data sources

**Files:**
- Create: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/AdminDataSourceConfig.java`
- Create: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/BizDataSourceConfig.java`
- Modify: `backen/src/main/java/com/neuCloudBrainMedical/admin/AdminApplication.java`
- Modify: `backen/src/main/java/com/neuCloudBrainMedical/admin/config/MyBatisPlusConfig.java`
- Test: `backen/src/test/java/com/neuCloudBrainMedical/admin/config/DualDataSourceContextTest.java`

- [ ] **Step 1: Implement the failing bean definitions**

`backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/AdminDataSourceConfig.java`

```java
package com.neuCloudBrainMedical.admin.config.datasource;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableConfigurationProperties(AdminDataSourceProperties.class)
@MapperScan(
    basePackages = "com.neuCloudBrainMedical.admin.mapper.admin",
    sqlSessionTemplateRef = "adminSqlSessionTemplate"
)
public class AdminDataSourceConfig {

    @Bean(name = "adminDataSource")
    public DataSource adminDataSource(AdminDataSourceProperties props) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(props.getDriverClassName());
        ds.setJdbcUrl(props.getUrl());
        ds.setUsername(props.getUsername());
        ds.setPassword(props.getPassword());
        return ds;
    }

    @Bean(name = "adminSqlSessionFactory")
    public SqlSessionFactory adminSqlSessionFactory(DataSource adminDataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(adminDataSource);
        factory.setVfs(SpringBootVFS.class);
        return factory.getObject();
    }

    @Bean(name = "adminSqlSessionTemplate")
    public SqlSessionTemplate adminSqlSessionTemplate(SqlSessionFactory adminSqlSessionFactory) {
        return new SqlSessionTemplate(adminSqlSessionFactory);
    }

    @Bean(name = "adminTransactionManager")
    public DataSourceTransactionManager adminTransactionManager(DataSource adminDataSource) {
        return new DataSourceTransactionManager(adminDataSource);
    }
}
```

`backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource/BizDataSourceConfig.java`

```java
package com.neuCloudBrainMedical.admin.config.datasource;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableConfigurationProperties(BizDataSourceProperties.class)
@MapperScan(
    basePackages = "com.neuCloudBrainMedical.admin.mapper.biz",
    sqlSessionTemplateRef = "bizSqlSessionTemplate"
)
public class BizDataSourceConfig {

    @Primary
    @Bean(name = "bizDataSource")
    public DataSource bizDataSource(BizDataSourceProperties props) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(props.getDriverClassName());
        ds.setJdbcUrl(props.getUrl());
        ds.setUsername(props.getUsername());
        ds.setPassword(props.getPassword());
        return ds;
    }

    @Primary
    @Bean(name = "bizSqlSessionFactory")
    public SqlSessionFactory bizSqlSessionFactory(DataSource bizDataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(bizDataSource);
        factory.setVfs(SpringBootVFS.class);
        return factory.getObject();
    }

    @Primary
    @Bean(name = "bizSqlSessionTemplate")
    public SqlSessionTemplate bizSqlSessionTemplate(SqlSessionFactory bizSqlSessionFactory) {
        return new SqlSessionTemplate(bizSqlSessionFactory);
    }

    @Primary
    @Bean(name = "bizTransactionManager")
    public DataSourceTransactionManager bizTransactionManager(DataSource bizDataSource) {
        return new DataSourceTransactionManager(bizDataSource);
    }
}
```

`backen/src/main/java/com/neuCloudBrainMedical/admin/AdminApplication.java`

```java
package com.neuCloudBrainMedical.admin;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdminApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

`backen/src/main/java/com/neuCloudBrainMedical/admin/config/MyBatisPlusConfig.java`

```java
package com.neuCloudBrainMedical.admin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

- [ ] **Step 2: Run the context test to verify it passes**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd -Dtest=DualDataSourceContextTest test
```

Expected: PASS.

- [ ] **Step 3: Verify the full backend test suite still starts**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd test
```

Expected: existing tests run; failures here identify code still coupled to the old single mapper scan.

- [ ] **Step 4: Fix any bootstrapping-only regressions before moving on**

```java
// Example of the only allowed change in this cleanup step:
// before: import com.neuCloudBrainMedical.admin.mapper.SysUserMapper;
// after:  import com.neuCloudBrainMedical.admin.mapper.admin.SysUserMapper;
//
// before: @MapperScan("com.neuCloudBrainMedical.admin.mapper")
// after:  remove the global scan and rely on the two datasource config scans.
```

- [ ] **Step 5: Commit**

```bash
git add backen/src/main/java/com/neuCloudBrainMedical/admin/AdminApplication.java backen/src/main/java/com/neuCloudBrainMedical/admin/config/MyBatisPlusConfig.java backen/src/main/java/com/neuCloudBrainMedical/admin/config/datasource
git commit -m "feat: add explicit admin and biz datasources"
```

### Task 4: Route mappers by source and prove the split with an integration test

**Files:**
- Create: `backen/src/test/java/com/neuCloudBrainMedical/admin/config/MapperDataSourceRoutingTest.java`
- Create: `backen/src/test/resources/sql/admin-schema.sql`
- Create: `backen/src/test/resources/sql/biz-schema.sql`
- Move/Modify: admin-domain mappers into `backen/src/main/java/com/neuCloudBrainMedical/admin/mapper/admin/**`
- Move/Modify: business-domain mappers into `backen/src/main/java/com/neuCloudBrainMedical/admin/mapper/biz/**`
- Test: `backen/src/test/java/com/neuCloudBrainMedical/admin/config/MapperDataSourceRoutingTest.java`

- [ ] **Step 1: Write the failing routing test**

```java
package com.neuCloudBrainMedical.admin.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.neuCloudBrainMedical.admin.mapper.admin.SysRoleMapper;
import com.neuCloudBrainMedical.admin.mapper.biz.RegistrationMapper;

@SpringBootTest
@ActiveProfiles("dual-ds-test")
class MapperDataSourceRoutingTest {

    @Autowired private DataSource adminDataSource;
    @Autowired private DataSource bizDataSource;
    @Autowired private SysRoleMapper sysRoleMapper;
    @Autowired private RegistrationMapper registrationMapper;

    @BeforeEach
    void seed() {
        JdbcTemplate adminJdbc = new JdbcTemplate(adminDataSource);
        JdbcTemplate bizJdbc = new JdbcTemplate(bizDataSource);

        adminJdbc.execute("DROP TABLE IF EXISTS sys_role");
        adminJdbc.execute("CREATE TABLE sys_role (role_id BIGINT PRIMARY KEY, role_code VARCHAR(50), role_name VARCHAR(50), status TINYINT)");
        adminJdbc.update("INSERT INTO sys_role(role_id, role_code, role_name, status) VALUES (1, 'ADMIN', 'Admin', 1), (2, 'DOCTOR', 'Doctor', 1)");

        bizJdbc.execute("DROP TABLE IF EXISTS registration");
        bizJdbc.execute("CREATE TABLE registration (registration_id BIGINT PRIMARY KEY, schedule_id BIGINT, patient_id BIGINT, doctor_id BIGINT, status VARCHAR(30), created_at TIMESTAMP)");
        bizJdbc.update("INSERT INTO registration(registration_id, schedule_id, patient_id, doctor_id, status, created_at) VALUES (1, 100, 10, 20, '已预约', CURRENT_TIMESTAMP)");
    }

    @Test
    void adminAndBizMappersQueryDifferentDatabases() {
        assertThat(sysRoleMapper.selectCount(null)).isEqualTo(2);
        assertThat(registrationMapper.selectCount(null)).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Run the routing test and verify it fails**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd -Dtest=MapperDataSourceRoutingTest test
```

Expected: FAIL because the mapper packages and imports still use the old single namespace.

- [ ] **Step 3: Move admin and business mappers into explicit packages**

```text
Admin source:
- SysUserMapper -> com.neuCloudBrainMedical.admin.mapper.admin
- SysRoleMapper -> com.neuCloudBrainMedical.admin.mapper.admin
- DepartmentMapper -> com.neuCloudBrainMedical.admin.mapper.admin.department
- DoctorMapper -> com.neuCloudBrainMedical.admin.mapper.admin.doctor
- ScheduleMapper -> com.neuCloudBrainMedical.admin.mapper.admin.schedule
- AiScheduleSuggestionMapper -> com.neuCloudBrainMedical.admin.mapper.admin.schedule
- AiScheduleSuggestionDetailMapper -> com.neuCloudBrainMedical.admin.mapper.admin.schedule

Business source:
- RegistrationMapper -> com.neuCloudBrainMedical.admin.mapper.biz
- AiConsultationMapper -> com.neuCloudBrainMedical.admin.mapper.biz.dashboard
```

Apply these exact import/package updates anywhere the old packages are referenced:

```java
// before
import com.neuCloudBrainMedical.admin.mapper.SysRoleMapper;
import com.neuCloudBrainMedical.admin.mapper.RegistrationMapper;

// after
import com.neuCloudBrainMedical.admin.mapper.admin.SysRoleMapper;
import com.neuCloudBrainMedical.admin.mapper.biz.RegistrationMapper;
```

- [ ] **Step 4: Re-run mapper routing and full backend tests**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd -Dtest=MapperDataSourceRoutingTest test
.\mvnw.cmd test
```

Expected: routing test PASS; any remaining failures now indicate services still assume old package names and must be fixed immediately.

- [ ] **Step 5: Commit**

```bash
git add backen/src/main/java/com/neuCloudBrainMedical/admin/mapper backen/src/test/java/com/neuCloudBrainMedical/admin/config/MapperDataSourceRoutingTest.java
git commit -m "refactor: split mappers by admin and biz datasources"
```

### Task 5: Add first-stage OceanBase admin schema and MySQL mirror scripts

**Files:**
- Create: `backen/sql/admin-domain-oceanbase.sql`
- Create: `backen/sql/admin-domain-mysql-mirror.sql`
- Create: `backen/sql/sync-admin-domain-to-oceanbase.ps1`
- Test: `infra/oceanbase/scripts/check-local-oceanbase.ps1`

- [ ] **Step 1: Write a failing schema verification command**

Run:

```powershell
docker exec ob-admin-local ob-mysql root his_admin -e "SHOW TABLES LIKE 'sys_user'; SHOW TABLES LIKE 'doctor_schedule';"
```

Expected: FAIL semantically because the tables do not exist yet.

- [ ] **Step 2: Add the first-stage admin-domain OceanBase schema**

`backen/sql/admin-domain-oceanbase.sql`

```sql
CREATE TABLE IF NOT EXISTS sys_role (
  role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sys_user_role FOREIGN KEY (role_id) REFERENCES sys_role(role_id)
);

CREATE TABLE IF NOT EXISTS department (
  dept_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT,
  dept_code VARCHAR(50) NOT NULL UNIQUE,
  dept_name VARCHAR(100) NOT NULL,
  dept_type VARCHAR(30) NOT NULL,
  floor VARCHAR(50),
  phone VARCHAR(30),
  location VARCHAR(100),
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES department(dept_id)
);

CREATE TABLE IF NOT EXISTS doctor (
  doctor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  dept_id BIGINT NOT NULL,
  doctor_no VARCHAR(50) NOT NULL UNIQUE,
  doctor_type VARCHAR(50) NOT NULL,
  title VARCHAR(50),
  specialty VARCHAR(255),
  introduction TEXT,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
  CONSTRAINT fk_doctor_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
);

CREATE TABLE IF NOT EXISTS doctor_schedule (
  schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  work_date DATE NOT NULL,
  time_period VARCHAR(20) NOT NULL,
  start_time TIME,
  end_time TIME,
  total_quota INT NOT NULL DEFAULT 0,
  remain_quota INT NOT NULL DEFAULT 0,
  registration_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(20) NOT NULL DEFAULT '可预约',
  source VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_doctor_schedule (doctor_id, work_date, time_period),
  CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_schedule_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
);

CREATE TABLE IF NOT EXISTS ai_schedule_suggestion (
  suggestion_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doctor_id BIGINT,
  dept_id BIGINT,
  work_date DATE,
  time_period VARCHAR(20),
  suggested_quota INT,
  suggestion_reason TEXT,
  status VARCHAR(20) NOT NULL DEFAULT '待确认',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmed_at DATETIME,
  CONSTRAINT fk_ai_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
  CONSTRAINT fk_ai_schedule_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
);

CREATE TABLE IF NOT EXISTS ai_schedule_suggestion_detail (
  detail_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  suggestion_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  doctor_name VARCHAR(50) NOT NULL,
  schedule_date DATE NOT NULL,
  time_slot VARCHAR(20) NOT NULL,
  max_appointments INT NOT NULL,
  reason TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  CONSTRAINT fk_ai_schedule_detail_suggestion FOREIGN KEY (suggestion_id) REFERENCES ai_schedule_suggestion(suggestion_id),
  CONSTRAINT fk_ai_schedule_detail_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
);
```

`backen/sql/admin-domain-mysql-mirror.sql`

```sql
-- This script creates the same seven admin-domain tables in MySQL for downstream mirror/sync scenarios.
SOURCE backen/sql/admin-domain-oceanbase.sql;
```

`backen/sql/sync-admin-domain-to-oceanbase.ps1`

```powershell
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

$tmpDir = Join-Path $PSScriptRoot "tmp-admin-sync"
New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null

& mysql -h $targetHost -P $targetPort -u $targetUser "-p$targetPassword" $targetDb -e "SET foreign_key_checks = 0;"
foreach ($table in $tables) {
  $dumpFile = Join-Path $tmpDir "$table.sql"
  & mysqldump -h $sourceHost -P $sourcePort -u $sourceUser "-p$sourcePassword" --skip-triggers --no-create-info --compact $sourceDb $table > $dumpFile
  & mysql -h $targetHost -P $targetPort -u $targetUser "-p$targetPassword" $targetDb < $dumpFile
}
& mysql -h $targetHost -P $targetPort -u $targetUser "-p$targetPassword" $targetDb -e "SET foreign_key_checks = 1;"
Write-Host "Admin-domain data synchronized into OceanBase."
```

- [ ] **Step 3: Load the schema into OceanBase**

Run:

```powershell
mysql -h 127.0.0.1 -P 2881 -u admin_app -pAdmin123456! his_admin < C:\Users\李博\OneDrive\桌面\admin\backen\sql\admin-domain-oceanbase.sql
powershell -ExecutionPolicy Bypass -File C:\Users\李博\OneDrive\桌面\admin\backen\sql\sync-admin-domain-to-oceanbase.ps1
```

- [ ] **Step 4: Verify the schema now exists**

Run:

```powershell
mysql -h 127.0.0.1 -P 2881 -u admin_app -pAdmin123456! his_admin -e "SHOW TABLES LIKE 'sys_user'; SHOW TABLES LIKE 'doctor_schedule'; SELECT COUNT(*) AS role_count FROM sys_role;"
```

Expected: PASS with both table names returned.

- [ ] **Step 5: Commit**

```bash
git add backen/sql/admin-domain-oceanbase.sql backen/sql/admin-domain-mysql-mirror.sql backen/sql/sync-admin-domain-to-oceanbase.ps1
git commit -m "feat: add oceanbase admin-domain schema and load scripts"
```

### Task 6: Document the local runbook and prove end-to-end startup

**Files:**
- Create: `docs/oceanbase-local-setup.md`
- Modify: `backen/src/main/resources/application.properties`
- Test: full backend startup against local OceanBase + MySQL

- [ ] **Step 1: Write the failing run command**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd spring-boot:run
```

Expected: FAIL until OceanBase admin schema and MySQL business schema are both reachable with the new properties.

- [ ] **Step 2: Document the exact local setup flow**

`docs/oceanbase-local-setup.md`

```markdown
# Local OceanBase Setup

1. Start Docker Desktop and confirm `docker info` succeeds.
2. Run `powershell -ExecutionPolicy Bypass -File .\infra\oceanbase\scripts\start-local-oceanbase.ps1`.
3. Confirm the OceanBase health check passes.
4. Ensure MySQL `doctor_platform` is running on port 3306.
5. Load `backen/sql/admin-domain-oceanbase.sql` into OceanBase `his_admin`.
6. Run `powershell -ExecutionPolicy Bypass -File .\backen\sql\sync-admin-domain-to-oceanbase.ps1`.
7. Start the backend with `.\mvnw.cmd spring-boot:run`.
8. Verify login, doctor list, department list, and schedule list all return HTTP 200.
```

- [ ] **Step 3: Run the backend and smoke test admin endpoints**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd spring-boot:run
```

In another shell:

```powershell
Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"123456"}'
Invoke-WebRequest -Uri "http://localhost:8081/api/admin/department" -Headers @{ Authorization = "Bearer <token>" }
Invoke-WebRequest -Uri "http://localhost:8081/api/admin/doctor?pageNum=1&pageSize=10" -Headers @{ Authorization = "Bearer <token>" }
Invoke-WebRequest -Uri "http://localhost:8081/api/admin/schedules?startDate=2026-07-08&endDate=2026-07-08" -Headers @{ Authorization = "Bearer <token>" }
```

Expected: login succeeds; admin-domain reads succeed; any registration-related endpoint still reads from MySQL.

- [ ] **Step 4: Run the final verification suite**

Run:

```powershell
Set-Location C:\Users\李博\OneDrive\桌面\admin\backen
.\mvnw.cmd test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add docs/oceanbase-local-setup.md
git commit -m "docs: add local oceanbase split-datasource runbook"
```

## Self-Review

- **Spec coverage:** This plan covers local OceanBase bootstrap, dual datasource contracts, Spring/MyBatis wiring, mapper routing, admin-domain schema creation, and end-to-end local verification. Production OMS reverse-sync deployment is intentionally documented as a later cutover concern rather than implemented in this first local milestone.
- **Placeholder scan:** No `TODO`, `TBD`, or abbreviated schema markers remain.
- **Type consistency:** Bean names are consistent across tests and configuration: `adminDataSource`, `bizDataSource`, `adminSqlSessionTemplate`, and `bizSqlSessionTemplate`. Mapper packages are consistently split into `mapper.admin` and `mapper.biz`.
