package com.neuCloudBrainMedical.admin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

class AdminApplicationTests {

	private ConfigurableApplicationContext context;

	@AfterEach
	void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	void contextLoads() {
		context = new SpringApplicationBuilder(AdminApplication.class)
				.properties(
						"spring.main.web-application-type=none",
						"spring.datasource.driver-class-name=org.h2.Driver",
						"spring.datasource.url=jdbc:h2:mem:admin_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
						"spring.datasource.username=sa",
						"spring.datasource.password=",
						"app.datasource.admin.driver-class-name=org.h2.Driver",
						"app.datasource.admin.url=jdbc:h2:mem:admin_test_admin;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
						"app.datasource.admin.username=sa",
						"app.datasource.admin.password=",
						"app.datasource.biz.driver-class-name=org.h2.Driver",
						"app.datasource.biz.url=jdbc:h2:mem:admin_test_biz;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
						"app.datasource.biz.username=sa",
						"app.datasource.biz.password=",
						"spring.jpa.hibernate.ddl-auto=create-drop",
						"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
				.run();
	}

}
