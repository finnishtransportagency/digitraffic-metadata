package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeKaikkiKameratResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraKokoonpanoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraPerustiedotEndpoint;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraPerustiedotEndpointImplService;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraPerustiedotException;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.ObjectFactory;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.VideopalvelinVO;

public class LotjuKameraPerustiedotServiceEndpointMock extends LotjuServiceEndpointMock implements KameraPerustiedotEndpoint {

    private static final Logger log = LoggerFactory.getLogger(LotjuKameraPerustiedotServiceEndpointMock.class);
    private static LotjuKameraPerustiedotServiceEndpointMock instance;
    private static final String LOTJU_KAMERA_RESOURCE_PATH = "lotju/kamera/";

    private List<KameraVO> initialKameras;
    private List<KameraVO> afterChangeKameras;
    private Map<Long, List<EsiasentoVO>> initialEsiasentos = new HashMap<>();
    private Map<Long, List<EsiasentoVO>> afterChangeEsiasentos = new HashMap<>();

    public static LotjuKameraPerustiedotServiceEndpointMock getInstance(final String metadataServerAddressCamera, final ResourceLoader resourceLoader,
                                                                        final Jaxb2Marshaller jaxb2Marshaller) {
        if (instance == null) {
            instance = new LotjuKameraPerustiedotServiceEndpointMock(metadataServerAddressCamera, resourceLoader, jaxb2Marshaller);
        }
        return instance;
    }

    private LotjuKameraPerustiedotServiceEndpointMock(final String metadataServerAddressCamera, final ResourceLoader resourceLoader,
                                                      final Jaxb2Marshaller jaxb2Marshaller) {
        super(resourceLoader, metadataServerAddressCamera, KameraPerustiedotEndpoint.class,
              KameraPerustiedotEndpointImplService.SERVICE, jaxb2Marshaller, LOTJU_KAMERA_RESOURCE_PATH);
    }

    @Override
    protected Class<?> getObjectFactoryClass() {
        return ObjectFactory.class;
    }

    @Override
    public void initStateAndService() {
        if (!isInited()) {
            initService();
        }
        setStateAfterChange(false);
    }

    /* KameraPerustiedot Service methods */

    @Override
    public KameraVO haeKamera(final Long id) throws KameraPerustiedotException {
        return haeKaikkiKamerat().stream().filter(k -> k.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<KameraVO> haeKameratVideopalvelimenTunnuksella(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKameratVideopalvelimenTunnuksella");
    }

    @Override
    public VideopalvelinVO haeVideopalvelin(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeVideopalvelin");
    }

    @Override
    public List<EsiasentoVO> haeEsiasennotKameranTunnuksella(final Long id) throws KameraPerustiedotException {
        HaeEsiasennotKameranTunnuksellaResponse response = readLotjuSoapResponse(HaeEsiasennotKameranTunnuksellaResponse.class, id);
        if (response != null) {
            return response.getEsiasennot();
        }
        return Collections.emptyList();
    }

    @Override
    public EsiasentoVO haeEsiasento(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeEsiasento");
    }

    @Override
    public KameraKokoonpanoVO haeKokoonpanoKameranTunnuksella(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKokoonpanoKameranTunnuksella");
    }

    @Override
    public KameraVO muutaKameranJulkisuus(Long id, JulkisuusTaso julkisuusTaso, XMLGregorianCalendar alkaen) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKameratVideopalvelimenTunnuksella");
    }

    @Override
    public List<KameraVO> haeKaikkiKamerat() throws KameraPerustiedotException {
        HaeKaikkiKameratResponse response = readLotjuSoapResponse(HaeKaikkiKameratResponse.class);
        if (response != null) {
            return response.getKamerat();
        }
        return Collections.emptyList();
    }

    @Override
    public List<VideopalvelinVO> haeKaikkiVideopalvelimet() throws KameraPerustiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public EsiasentoVO muuttaaEsiasennonJulkisuus(Long id, boolean julkinen) throws KameraPerustiedotException {
        throw new NotImplementedException("muuttaaEsiasennonJulkisuus");
    }
}
