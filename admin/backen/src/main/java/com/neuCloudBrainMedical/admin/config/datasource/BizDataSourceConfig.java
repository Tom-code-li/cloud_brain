package com.neuCloudBrainMedical.admin.config.datasource;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
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
	public SqlSessionFactory bizSqlSessionFactory(@Qualifier("bizDataSource") DataSource bizDataSource,
			MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
		MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
		factory.setDataSource(bizDataSource);
		factory.setPlugins(mybatisPlusInterceptor);
		return factory.getObject();
	}

	@Primary
	@Bean(name = "bizSqlSessionTemplate")
	public SqlSessionTemplate bizSqlSessionTemplate(
			@Qualifier("bizSqlSessionFactory") SqlSessionFactory bizSqlSessionFactory) {
		return new SqlSessionTemplate(bizSqlSessionFactory);
	}

	@Primary
	@Bean(name = "bizTransactionManager")
	public DataSourceTransactionManager bizTransactionManager(@Qualifier("bizDataSource") DataSource bizDataSource) {
		return new DataSourceTransactionManager(bizDataSource);
	}
}
