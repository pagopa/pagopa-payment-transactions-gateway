package it.pagopa.pm.gateway;

import it.pagopa.pm.gateway.config.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.*;

@ComponentScan(basePackages = "it.pagopa.pm.gateway")
@EnableAutoConfiguration
@EntityScan({ "it.pagopa.pm.gateway.entity" })
@EnableJpaRepositories(basePackages = "it.pagopa.pm.gateway.repository")
@Configuration
public class Application extends SpringBootServletInitializer {

	// private static final Class<CoreDataSourceConfiguration> coreDataSourceConfiguration = CoreDataSourceConfiguration.class;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// @Override
	// @ConditionalOnProperty(name = "pagopa.datasource.enable", havingValue = "true")
	// protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	// 	return application.sources(Application.class, coreDataSourceConfiguration);
	// }

}
