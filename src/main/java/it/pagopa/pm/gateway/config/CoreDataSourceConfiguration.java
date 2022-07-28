package it.pagopa.pm.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Objects;

@Configuration
public class CoreDataSourceConfiguration {

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnProperty(name="pagopa.datasource.oracle.enabled", havingValue="true")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws NamingException {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(productDataSource());
        em.setPackagesToScan("it.pagopa.pm.gateway.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.Oracle12cDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    @ConditionalOnProperty(name="pagopa.datasource.oracle.enabled", havingValue="true")
    public PlatformTransactionManager transactionManager() throws NamingException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;
    }

    @Bean
    @ConditionalOnProperty(name="pagopa.datasource.oracle.enabled", havingValue="true")
    public DataSource productDataSource() throws NamingException {
        return (DataSource) new JndiTemplate().lookup(Objects.requireNonNull(
                env.getProperty("pagopa.datasource.jndi.name")));
    }

}
