package com.neuCloudBrainMedical.admin.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.neuCloudBrainMedical.admin.AdminApplication;

class DualDataSourceContextTest {

	@Test
	void loadsBothNamedDataSources() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(AdminApplication.class)
				.profiles("dual-ds-test")
				.properties("spring.main.web-application-type=none")
				.run()) {
			assertThat(context.containsBean("adminDataSource")).isTrue();
			assertThat(context.containsBean("bizDataSource")).isTrue();
		}
	}
}
