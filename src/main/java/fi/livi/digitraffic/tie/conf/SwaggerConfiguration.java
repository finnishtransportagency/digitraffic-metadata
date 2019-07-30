package fi.livi.digitraffic.tie.conf;

import static com.google.common.base.Predicates.or;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_V2_BASE_PATH;
import static springfox.documentation.builders.PathSelectors.regex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import com.google.common.base.Predicate;

import fi.livi.digitraffic.tie.data.controller.DataController;
import fi.livi.digitraffic.tie.metadata.controller.MetadataController;
import fi.livi.digitraffic.tie.metadata.service.MetadataApiInfoService;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ConditionalOnWebApplication
@Configuration
@EnableSwagger2
@ComponentScan(basePackageClasses = {
    DataController.class, MetadataController.class
})
public class SwaggerConfiguration {

    private final MetadataApiInfoService metadataApiInfoService;

    @Autowired
    public SwaggerConfiguration(final MetadataApiInfoService metadataApiInfoService) {
        Assert.notNull(metadataApiInfoService, "MetadataApiInfoService can't be null");
        this.metadataApiInfoService = metadataApiInfoService;
    }

    @Bean
    public Docket metadataApi() {
        return getDocket("metadata-api", getMetadataApiPaths());
    }

    @Bean
    public Docket betaApi() {
        return getDocket("metadata-api-beta", regex(API_BETA_BASE_PATH + "/*.*"));
    }

    @Bean
    UiConfiguration uiConfiguration() {
        return UiConfigurationBuilder.builder()
            .docExpansion(DocExpansion.LIST)
            .defaultModelRendering(ModelRendering.MODEL)
            .build();
    }

    private Docket getDocket(final String groupName, Predicate<String> apiPaths) {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName(groupName)
            // Issue: https://github.com/springfox/springfox/issues/1021#issuecomment-178626396
            .directModelSubstitute(ZonedDateTime.class, java.util.Date.class)
            .directModelSubstitute(LocalDateTime.class, java.util.Date.class)
            .directModelSubstitute(LocalDate.class, java.sql.Date.class)
            .directModelSubstitute(Date.class, java.sql.Date.class)
            .produces(new HashSet<>(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8_VALUE)))
            .apiInfo(metadataApiInfoService.getApiInfo())
            .select()
            .paths(apiPaths)
            .build()
            .useDefaultResponseMessages(false);
    }

    /**
     * Declares api paths to document by Swagger
     * @return api paths
     */
    private static Predicate<String> getMetadataApiPaths() {
        return or(
            regex(API_V1_BASE_PATH + API_METADATA_PART_PATH + "/*.*"),
            regex(API_V1_BASE_PATH + API_DATA_PART_PATH + "/*.*"),
            regex(API_V2_BASE_PATH + API_METADATA_PART_PATH + "/*.*"),
            regex(API_V2_BASE_PATH + API_DATA_PART_PATH + "/*.*")
        );
    }
}
