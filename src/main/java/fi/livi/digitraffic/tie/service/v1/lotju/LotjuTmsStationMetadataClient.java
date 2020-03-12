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
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAsemanAnturiVakio;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAsemanAnturiVakioResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiAnturiVakioArvot;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiAnturiVakioArvotResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMAsemat;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMAsematResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMLaskennallisetAnturit;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMAsemanLaskennallisetAnturit;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMAsemanLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.ObjectFactory;

@ConditionalOnNotWebApplication
@Service
public class LotjuTmsStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationMetadataClient.class);
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuTmsStationMetadataClient(@Qualifier("lamMetadataJaxb2Marshaller")
                                         Jaxb2Marshaller lamMetadataJaxb2Marshaller,
                                         @Value("${metadata.server.address.tms}") final String tmsMetadataServerAddress) {
        super(lamMetadataJaxb2Marshaller, tmsMetadataServerAddress, log);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    List<LamAsemaVO> getLamAsemas() {

        final HaeKaikkiLAMAsemat request = new HaeKaikkiLAMAsemat();

        log.info("Fetching LamAsemas");
        final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLAMAsemat(request));
        log.info("lamFetchedCount={} LamAsemas", response.getValue().getAsemat().size());
        return response.getValue().getAsemat();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    List<LamLaskennallinenAnturiVO> getTiesaaLaskennallinenAnturis(final Long lamAsemaLotjuId) {

        final HaeLAMAsemanLaskennallisetAnturit request = new HaeLAMAsemanLaskennallisetAnturit();
        request.setId(lamAsemaLotjuId);
        final JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse> response = (JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeLAMAsemanLaskennallisetAnturit(request));
        return response.getValue().getLamlaskennallisetanturit();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        final HaeKaikkiLAMLaskennallisetAnturit request = new HaeKaikkiLAMLaskennallisetAnturit();
        log.info("Fetching LAMLaskennallisetAnturis");
        final JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLAMLaskennallisetAnturit(request));
        log.info("lamFetchedCount={} LAMLaskennallisetAnturis", response.getValue().getLaskennallinenAnturi().size());
        return response.getValue().getLaskennallinenAnturi();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    List<LamAnturiVakioVO> getAsemanAnturiVakios(final Long lotjuId) {
        final HaeAsemanAnturiVakio haeAsemanAnturiVakioRequest =
            new HaeAsemanAnturiVakio();
        haeAsemanAnturiVakioRequest.setAsemaId(lotjuId);

        final JAXBElement<HaeAsemanAnturiVakioResponse> haeAsemanAnturiVakioResponse =
            (JAXBElement< HaeAsemanAnturiVakioResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeAsemanAnturiVakio(haeAsemanAnturiVakioRequest));
        return haeAsemanAnturiVakioResponse.getValue().getLamanturivakiot();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    List<LamAnturiVakioArvoVO> getAllAnturiVakioArvos(final int month, final int dayOfMonth) {
        final HaeKaikkiAnturiVakioArvot haeKaikkiAnturiVakioArvotRequest =
            new HaeKaikkiAnturiVakioArvot();
        haeKaikkiAnturiVakioArvotRequest.setKuukausi(month);
        haeKaikkiAnturiVakioArvotRequest.setPaiva(dayOfMonth);

        final JAXBElement<HaeKaikkiAnturiVakioArvotResponse> haeKaikkiAnturiVakioArvotResponse =
            (JAXBElement<HaeKaikkiAnturiVakioArvotResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiAnturiVakioArvot(haeKaikkiAnturiVakioArvotRequest));
        return haeKaikkiAnturiVakioArvotResponse.getValue().getLamanturivakiot();
    }

}
