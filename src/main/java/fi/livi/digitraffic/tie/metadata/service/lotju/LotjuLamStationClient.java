package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.HaeKaikkiLAMAsemat;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.HaeKaikkiLAMAsematResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.HaeKaikkiLAMLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.HaeKaikkiLAMLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.HaeLAMAsemanLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.HaeLAMAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.ObjectFactory;

public class LotjuLamStationClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(LotjuLamStationClient.class);
    public static final String FETCHED = "Fetched ";

    private String address;

    public List<LamAsemaVO> getLamAsemas() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLAMAsemat request = new HaeKaikkiLAMAsemat();

        log.info("Fetching LamAsemas");
        final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiLAMAsemat(request));

        log.info(FETCHED + response.getValue().getAsemat().size() + " LamAsemas");
        return response.getValue().getAsemat();
    }

    public List<LamLaskennallinenAnturiVO> getTiesaaLaskennallinenAnturis(final Long lamAsemaLotjuId) {

//        log.info("Fetching LamLaskennallinenAnturis for LamAsema with lotjuId: " + lamAsemaLotjuId);

        final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturiMapByLamLotjuId = new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeLAMAsemanLaskennallisetAnturit request = new HaeLAMAsemanLaskennallisetAnturit();

        request.setId(lamAsemaLotjuId);
        final JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse> response = (JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse>)
                    getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeLAMAsemanLaskennallisetAnturit(request));
        final List<LamLaskennallinenAnturiVO> anturis = response.getValue().getLamlaskennallisetanturit();

//        log.info(FETCHED + anturis.size() + " LamLaskennallinenAnturis for LamAsema with lotjuId: " + lamAsemaLotjuId);
        return anturis;
    }

    public List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLAMLaskennallisetAnturit request = new HaeKaikkiLAMLaskennallisetAnturit();

        log.info("Fetching LAMLaskennallisetAnturis");
        final JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiLAMLaskennallisetAnturit(request));

        log.info(FETCHED + response.getValue().getLaskennallinenAnturi().size() + " LAMLaskennallisetAnturis");
        return response.getValue().getLaskennallinenAnturi();
    }

    public void setAddress(final String address) {
        this.address = address;
    }
}
