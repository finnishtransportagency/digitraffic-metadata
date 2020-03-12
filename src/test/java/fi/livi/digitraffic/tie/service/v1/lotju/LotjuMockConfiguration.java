package fi.livi.digitraffic.tie.service.v1.lotju;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@ConditionalOnNotWebApplication
@Configuration
public class LotjuMockConfiguration {

    @Autowired
    LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @Autowired
    LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @Autowired
    LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;

    @Bean
    public LotjuKameraPerustiedotServiceEndpointMock lotjuKameraPerustiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            @Qualifier("kameraMetadataJaxb2Marshaller")
            final Jaxb2Marshaller kameraMetadataJaxb2Marshaller) {
        return LotjuKameraPerustiedotServiceEndpointMock.getInstance(lotjuCameraStationMetadataClient.getServerAddress(), resourceLoader, kameraMetadataJaxb2Marshaller);
    }

    @Bean
    public LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            @Qualifier("lamMetadataJaxb2Marshaller")
            final Jaxb2Marshaller lamMetadataJaxb2Marshaller) {
        return LotjuLAMMetatiedotServiceEndpointMock.getInstance(lotjuTmsStationMetadataClient.getServerAddress(), resourceLoader, lamMetadataJaxb2Marshaller);
    }

    @Bean
    public LotjuTiesaaPerustiedotServiceEndpointMock lotjuTiesaaPerustiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            @Qualifier("tiesaaMetadataJaxb2Marshaller")
            final Jaxb2Marshaller tiesaaMetadataJaxb2Marshaller) {
        return LotjuTiesaaPerustiedotServiceEndpointMock.getInstance(lotjuWeatherStationMetadataClient.getServerAddress(), resourceLoader, tiesaaMetadataJaxb2Marshaller);
    }

}
