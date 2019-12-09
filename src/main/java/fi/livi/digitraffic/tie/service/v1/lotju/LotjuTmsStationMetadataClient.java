package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioArvoVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2017._05._02.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeAsemanAnturiVakio;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeAsemanAnturiVakioResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeKaikkiAnturiVakioArvot;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeKaikkiAnturiVakioArvotResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeKaikkiLAMAsemat;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeKaikkiLAMAsematResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeKaikkiLAMLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeKaikkiLAMLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeLAMAsemanLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.HaeLAMAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.ObjectFactory;

@ConditionalOnNotWebApplication
@Service
public class LotjuTmsStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationMetadataClient.class);
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuTmsStationMetadataClient(Jaxb2Marshaller marshaller,
                                         @Value("${metadata.server.address.tms}") final String tmsMetadataServerAddress) {
        super(marshaller, tmsMetadataServerAddress, log);
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