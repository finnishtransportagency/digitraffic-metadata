package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V3_BASE_PATH;
import static springfox.documentation.builders.PathSelectors.regex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.controller.v1.DataController;
import fi.livi.digitraffic.tie.controller.v1.MetadataController;
import fi.livi.digitraffic.tie.service.RoadApiInfoGetter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

@ConditionalOnWebApplication
@Configuration
@ComponentScan(basePackageClasses = {
    DataController.class, MetadataController.class
})
public class SwaggerConfiguration {

    private final RoadApiInfoGetter roadApiInfoGetter;
    private final String host;
    private final String scheme;

    @Autowired
    public SwaggerConfiguration(final RoadApiInfoGetter roadApiInfoGetter,
                                final @Value("${dt.domain.url}") String domainUrl) throws URISyntaxException {
        this.roadApiInfoGetter = roadApiInfoGetter;
        URI uri = new URI(domainUrl);

        final int port = uri.getPort();
        if (port > -1) {
            host = uri.getHost() + ":" + port;
        } else {
            host = uri.getHost();
        }
        scheme = uri.getScheme();
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
            .docExpansion(DocExpansion.NONE)
            .defaultModelRendering(ModelRendering.MODEL)
            // There is bugs in online validator, so not use it at the moment ie. https://github.com/swagger-api/validator-badge/issues/97
            //.validatorUrl("https://online.swagger.io/validator")
            .build();
    }

    private Docket getDocket(final String groupName, Predicate<String> apiPaths) {
        return new Docket(DocumentationType.SWAGGER_2)
            .host(host)
            .protocols(Set.of(scheme))
            .groupName(groupName)
            .produces(new HashSet<>(Collections.singletonList(MediaType.APPLICATION_JSON_VALUE)))
            .apiInfo(roadApiInfoGetter.getApiInfo())
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
        return regex(API_V1_BASE_PATH + API_METADATA_PART_PATH + "/*.*").or(
               regex(API_V1_BASE_PATH + API_DATA_PART_PATH + "/*.*")).or(
               regex(API_V2_BASE_PATH + API_METADATA_PART_PATH + "/*.*")).or(
               regex(API_V2_BASE_PATH + API_DATA_PART_PATH + "/*.*")).or(
               regex(API_V3_BASE_PATH + API_METADATA_PART_PATH + "/*.*")).or(
               regex(API_V3_BASE_PATH + API_DATA_PART_PATH + "/*.*"));
    }
}
