package fi.livi.digitraffic.tie.conf;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.resource.TransformedResource;

import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2D2LogicalModelHttpMessageConverter;
import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2Datex2ResponseHttpMessageConverter;
import fi.livi.digitraffic.tie.converter.Datex2MessagetypeParameterStringToEnumConverter;

@ConditionalOnWebApplication
@Configuration
public class RoadWebApplicationConfiguration implements WebMvcConfigurer {
    private final ConfigurableApplicationContext applicationContext;

    // Match when there is no http in location start
    private final static String SCHEMA_LOCATION_REGEXP = "schemaLocation=\"((?!http))";
    private final static String SCHEMA_PATH = "/schemas/datex2/";
    private final String schemaDomainUrlAndPath;

    @Autowired
    public RoadWebApplicationConfiguration(final ConfigurableApplicationContext applicationContext,
                                           final @Value("${dt.domain.url}") String schemaDomainUrl) {
        this.applicationContext = applicationContext;
        this.schemaDomainUrlAndPath = schemaDomainUrl + SCHEMA_PATH;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // put first
        converters.add(0, new Jaxb2D2LogicalModelHttpMessageConverter(schemaDomainUrlAndPath));
        converters.add(0, new Jaxb2Datex2ResponseHttpMessageConverter(schemaDomainUrlAndPath));
    }

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        // Converter for Controller Datex2Messagetype parameters
        registry.addConverter(new Datex2MessagetypeParameterStringToEnumConverter());
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
     * This redirects requests from root / to /swagger-ui/index.html.
     * After that nginx redirects /swagger-ui.html to /api/v1/metadata/documentation/swagger-ui.html
     * @param registry
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/swagger-ui/index.html");
    }
}
