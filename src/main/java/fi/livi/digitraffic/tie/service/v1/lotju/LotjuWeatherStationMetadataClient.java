package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiLaskennallisetAnturit;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiTiesaaAsemat;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiTiesaaAsematResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeTiesaaAsemanLaskennallisetAnturit;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;

@ConditionalOnNotWebApplication
@Service
public class LotjuWeatherStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuWeatherStationMetadataClient.class);
    final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuWeatherStationMetadataClient(@Qualifier("tiesaaMetadataJaxb2Marshaller")
                                             final Jaxb2Marshaller tiesaaMetadataJaxb2Marshaller,
                                             final @Value("${metadata.server.address.weather}") String weatherMetadataServerAddress) {
        super(tiesaaMetadataJaxb2Marshaller, weatherMetadataServerAddress, log);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Retryable(maxAttempts = 5)
    public List<TiesaaAsemaVO> getTiesaaAsemas() {
        log.info("Fetching TiesaaAsemas from " + getWebServiceTemplate().getDefaultUri());

        final HaeKaikkiTiesaaAsemat request = new HaeKaikkiTiesaaAsemat();
        final JAXBElement<HaeKaikkiTiesaaAsematResponse> response = (JAXBElement<HaeKaikkiTiesaaAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiTiesaaAsemat(request));

        log.info("roadStationFetchedCount={} TiesaaAsemas", response.getValue().getTiesaaAsema().size());
        return response.getValue().getTiesaaAsema();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    public List<TiesaaLaskennallinenAnturiVO> getAllTiesaaLaskennallinenAnturis() {
        log.info("Fetching all LaskennallisetAnturit from " + getWebServiceTemplate().getDefaultUri());

        final HaeKaikkiLaskennallisetAnturit request = new HaeKaikkiLaskennallisetAnturit();
        final JAXBElement<HaeKaikkiLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLaskennallisetAnturit(request));

        return response.getValue().getLaskennallinenAnturi();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Retryable(maxAttempts = 5)
    public List<TiesaaLaskennallinenAnturiVO> getTiesaaAsemanLaskennallisetAnturit(Long tiesaaAsemaLotjuId) {
        final HaeTiesaaAsemanLaskennallisetAnturit request = new HaeTiesaaAsemanLaskennallisetAnturit();
        request.setId(tiesaaAsemaLotjuId);

        final JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse> response =
                (JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeTiesaaAsemanLaskennallisetAnturit(request));

        return response.getValue().getLaskennallinenAnturi();
    }

}
