package fi.livi.digitraffic.tie.conf;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.resource.TransformedResource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2Datex2ResponseHttpMessageConverter;

@Configuration
@EnableJpaRepositories(basePackages = {"fi.livi.digitraffic.tie.metadata.dao", "fi.livi.digitraffic.tie.data.dao"},
    enableDefaultTransactions = false)
@EnableTransactionManagement
@EnableRetry
public class RoadApplicationConfiguration implements WebMvcConfigurer {

    public static final String API_V1_BASE_PATH = "/api/v1";
    public static final String API_V2_BASE_PATH = "/api/v2";
    public static final String API_BETA_BASE_PATH = "/api/beta";

    public static final String API_METADATA_PART_PATH = "/metadata";
    public static final String API_DATA_PART_PATH = "/data";
    public static final String API_MAINTENANCE_PART_PATH = "/maintenance";

    private final ConfigurableApplicationContext applicationContext;

    // Match when there is no http in location start
    private final static String SCHEMA_LOCATION_REGEXP = "schemaLocation=\"((?!http))";
    private final static String SCHEMA_PATH = "/schemas/datex2/";
    private final String schemaDomainUrlAndPath;

    @Autowired
    public RoadApplicationConfiguration(final ConfigurableApplicationContext applicationContext,
                                        final @Value("${dt.domain.url}") String schemaDomainUrl) {
        this.applicationContext = applicationContext;
        this.schemaDomainUrlAndPath = schemaDomainUrl + SCHEMA_PATH;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // put first
        converters.add(0, new Jaxb2Datex2ResponseHttpMessageConverter(schemaDomainUrlAndPath));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Add datex2 schema locations to default static file path and
        registry.addResourceHandler(SCHEMA_PATH + "/**")
            .addResourceLocations("classpath:/schemas/datex2/")
            // cache resource -> this is converted only once per resource
            .resourceChain(true)
            // add current domain and path to schemaLocation-attribute
            .addTransformer((request, resource, transformerChain) -> {
                final String schema = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                final String newSchema =
                    RegExUtils.replaceAll(schema, SCHEMA_LOCATION_REGEXP, String.format("schemaLocation=\"%s", schemaDomainUrlAndPath));
                return new TransformedResource(resource, newSchema.getBytes());
            });
    }

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

    /**
     * Enables bean validation for controller parameters
     * @return
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = applicationContext.getBean(LocaleChangeInterceptor.class);
        Assert.notNull(localeChangeInterceptor, "LocaleChangeInterceptor cannot be null");
        registry.addInterceptor(localeChangeInterceptor);
    }

    /**
     * This redirects requests from root to /swagger-ui.html.
     * After that nginx redirects /swagger-ui.html to /api/v1/metadata/documentation/swagger-ui.html
     * @param registry
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:swagger-ui.html");
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
        config.setIdleTimeout(500000);
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
    public org.springframework.core.convert.ConversionService getConversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.afterPropertiesSet();
        ConversionService object = bean.getObject();
        return object;
    }
}
