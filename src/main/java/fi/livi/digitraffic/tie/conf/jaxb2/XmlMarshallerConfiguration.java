package fi.livi.digitraffic.tie.conf.jaxb2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class XmlMarshallerConfiguration {
    @Bean
    public Jaxb2Marshaller kameraMetadataJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
                // from WSDLs generate classes. Only higest
                "fi.livi.digitraffic.tie.external.lotju.metadata.kamera");
        return marshaller;
    }

    @Bean
    public Jaxb2Marshaller lamMetadataJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // from WSDLs generate classes. Only higest
            "fi.livi.digitraffic.tie.external.lotju.metadata.lam");
        return marshaller;
    }

    @Bean
    public Jaxb2Marshaller tiesaaMetadataJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // from WSDLs generate classes. Only higest
            "fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa");
        return marshaller;
    }

    @Primary
    @Bean
    public Jaxb2Marshaller commonJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // From xsds generated classes
            "fi.livi.digitraffic.tie.datex2",
            "fi.livi.digitraffic.tie.datex2.response", // t-loik ???
            "fi.livi.digitraffic.tie.lotju.xsd.metatietomuutos.kameratietovirta",
            "fi.livi.digitraffic.tie.external.tloik.ims");
        return marshaller;
    }
}