package fi.livi.digitraffic.tie.conf;

import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages =
    {"fi.livi.digitraffic.tie.metadata.dao", "fi.livi.digitraffic.tie.data.dao", "fi.livi.digitraffic.tie.dao"},
                       enableDefaultTransactions = false)
@EnableTransactionManagement
@EnableRetry
public class RoadApplicationConfiguration {

    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    @Primary
    public DataSource dataSource(final @Value("${road.datasource.url}") String url,
                                 final @Value("${road.datasource.username}") String username,
                                 final @Value("${road.datasource.password}") String password,
                                 final @Value("${road.datasource.hikari.maximum-pool-size:20}") Integer maximumPoolSize) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(maximumPoolSize);

        config.setMaxLifetime(570000);
        config.setConnectionTimeout(60000);
        config.setPoolName("application_pool");

        // register mbeans for debug
        config.setRegisterMbeans(true);

        return new HikariDataSource(config);
    }

    @Bean
    // fix bug in spring boot, tries to export hikari beans twice
    public MBeanExporter exporter() {
        final MBeanExporter exporter = new MBeanExporter();

        exporter.setAutodetect(true);
        exporter.setExcludedBeans("dataSource", "quartzDataSource");

        return exporter;
    }

    @Bean("conversionService")
    public ConversionService getConversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.afterPropertiesSet();
        ConversionService object = bean.getObject();
        return object;
    }
}
