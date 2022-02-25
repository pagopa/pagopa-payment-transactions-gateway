package it.pagopa.pm.gateway;

import it.pagopa.pm.gateway.config.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = "it.pagopa.pm.gateway")
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Configuration
public class Application extends SpringBootServletInitializer {

	private static final Class<CoreDataSourceConfiguration> coreDataSourceConfiguration = CoreDataSourceConfiguration.class;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class, coreDataSourceConfiguration);
	}

}
