package com.home.global.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@Profile("batch")
public class BatchDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.oltp")
	public DataSourceProperties oltpDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.olap")
	public DataSourceProperties olapDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean(name = "oltpDataSource")
	public DataSource oltpDataSource() {
		return oltpDataSourceProperties()
			.initializeDataSourceBuilder()
			.build();
	}

	@Bean(name = "olapDataSource")
	@Primary
	public DataSource olapDataSource() {
		return olapDataSourceProperties()
			.initializeDataSourceBuilder()
			.build();
	}

	@Bean(name = "olapJdbc")
	public NamedParameterJdbcTemplate olapJdbc(@Qualifier("olapDataSource") DataSource ds) {
		return new NamedParameterJdbcTemplate(ds);
	}

	@Bean(name = "oltpJdbc")
	public NamedParameterJdbcTemplate oltpJdbc(@Qualifier("oltpDataSource") DataSource ds) {
		return new NamedParameterJdbcTemplate(ds);
	}
}
