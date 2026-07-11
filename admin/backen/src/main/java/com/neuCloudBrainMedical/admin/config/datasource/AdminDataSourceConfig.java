package com.neuCloudBrainMedical.admin.config.datasource;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
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
	public SqlSessionFactory adminSqlSessionFactory(@Qualifier("adminDataSource") DataSource adminDataSource,
			MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
		MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
		factory.setDataSource(adminDataSource);
		factory.setPlugins(mybatisPlusInterceptor);
		return factory.getObject();
	}

	@Bean(name = "adminSqlSessionTemplate")
	public SqlSessionTemplate adminSqlSessionTemplate(
			@Qualifier("adminSqlSessionFactory") SqlSessionFactory adminSqlSessionFactory) {
		return new SqlSessionTemplate(adminSqlSessionFactory);
	}

	@Bean(name = "adminTransactionManager")
	public DataSourceTransactionManager adminTransactionManager(@Qualifier("adminDataSource") DataSource adminDataSource) {
		return new DataSourceTransactionManager(adminDataSource);
	}
}
