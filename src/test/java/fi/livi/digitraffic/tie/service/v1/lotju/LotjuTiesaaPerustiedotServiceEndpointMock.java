package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.AnturiSanomaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.ArvoVastaavuusVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiTiesaaAsematResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaHakuparametrit;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaPerustiedotEndpoint;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaPerustiedotEndpointImplService;

public class LotjuTiesaaPerustiedotServiceEndpointMock extends LotjuServiceEndpointMock implements TiesaaPerustiedotEndpoint {

    private static final Logger log = LoggerFactory.getLogger(LotjuTiesaaPerustiedotServiceEndpointMock.class);
    private static LotjuTiesaaPerustiedotServiceEndpointMock instance;
    private static final String LOTJU_TIESAA_RESOURCE_PATH = "lotju/tiesaa/";

    private List<TiesaaAsemaVO> initialTiesaaAsemas;
    private List<TiesaaAsemaVO> afterChangeTiesaaAsemas;
    private Map<Long, List<TiesaaLaskennallinenAnturiVO>> initialTiesaaAnturisMap = new HashMap<>();
    private final Map<Long, List<TiesaaLaskennallinenAnturiVO>> afterChangeTiesaaAnturisMap = new HashMap<>();
    private List<TiesaaLaskennallinenAnturiVO> initialLaskennallisetAnturis;
    private List<TiesaaLaskennallinenAnturiVO> afterChangeLaskennallisetAnturis;

    public static LotjuTiesaaPerustiedotServiceEndpointMock getInstance(final String metadataServerAddressCamera,
                                                                        final ResourceLoader resourceLoader,
                                                                        final Jaxb2Marshaller jaxb2Marshaller) {
        if (instance == null) {
            instance = new LotjuTiesaaPerustiedotServiceEndpointMock(metadataServerAddressCamera, resourceLoader, jaxb2Marshaller);
        }
        return instance;
    }

    private LotjuTiesaaPerustiedotServiceEndpointMock(final String metadataServerAddressWeather, final ResourceLoader resourceLoader,
                                                      final Jaxb2Marshaller jaxb2Marshaller) {
        super(resourceLoader, metadataServerAddressWeather, TiesaaPerustiedotEndpoint.class,
              TiesaaPerustiedotEndpointImplService.SERVICE, jaxb2Marshaller, LOTJU_TIESAA_RESOURCE_PATH);
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

    /* TiesaaPerustiedot Service methods */

    @Override
    public List<TiesaaLaskennallinenAnturiVO> haeTiesaaAsemanLaskennallisetAnturit(final Long id) {
        HaeTiesaaAsemanLaskennallisetAnturitResponse response = readLotjuSoapResponse(HaeTiesaaAsemanLaskennallisetAnturitResponse.class, id);
        if (response != null) {
            return response.getLaskennallinenAnturi();
        }
        return Collections.emptyList();
    }

    @Override
    public List<ArvoVastaavuusVO> haeKaikkiArvovastaavuudet() {
        throw new NotImplementedException("haeKaikkiArvovastaavuudet");
    }

    @Override
    public List<ArvoVastaavuusVO> haeLaskennallisenAnturinArvovastaavuudet(final Long arg0) {
        throw new NotImplementedException("haeLaskennallisenAnturinArvovastaavuudet");
    }

    @Override
    public ArvoVastaavuusVO haeArvovastaavuus(final Long id) {
        throw new NotImplementedException("haeArvovastaavuus");
    }

    @Override
    public List<TiesaaAnturiVO> haeTiesaaAsemanAnturit(final Long id) {
        throw new NotImplementedException("haeTiesaaAsemanAnturit");
    }

    @Override
    public List<TiesaaAsemaVO> haeKaikkiTiesaaAsemat() {
        HaeKaikkiTiesaaAsematResponse response = readLotjuSoapResponse(HaeKaikkiTiesaaAsematResponse.class);
        if (response != null) {
            return response.getTiesaaAsema();
        }
        return Collections.emptyList();
    }

    @Override
    public TiesaaAsemaVO haeTiesaaAsema(final Long id) {
        throw new NotImplementedException("haeTiesaaAsema");
    }

    @Override
    public TiesaaLaskennallinenAnturiVO haeLaskennallinenAnturi(final Long id) {
        throw new NotImplementedException("haeLaskennallinenAnturi");
    }

    @Override
    public List<TiesaaAsemaVO> haeTiesaaAsemat(final TiesaaAsemaHakuparametrit parametrit) {
        throw new NotImplementedException("haeTiesaaAsemat");
    }

    @Override
    public TiesaaAnturiVO haeAnturi(final Long id) {
        throw new NotImplementedException("haeAnturi");
    }

    @Override
    public List<AnturiSanomaVO> haeKaikkiAnturisanomat() {
        throw new NotImplementedException("haeKaikkiAnturisanomat");
    }

    @Override
    public List<TiesaaLaskennallinenAnturiVO> haeKaikkiLaskennallisetAnturit() {
        HaeKaikkiLaskennallisetAnturitResponse response = readLotjuSoapResponse(HaeKaikkiLaskennallisetAnturitResponse.class);
        if (response != null) {
            return response.getLaskennallinenAnturi();
        }
        return Collections.emptyList();
    }

    @Override
    public List<TiesaaAsemaLaskennallinenAnturiVO> haeTiesaaAsemanLaskennallistenAntureidenTilat(final Long asemaId) {
        throw new NotImplementedException("haeTiesaaAsemanLaskennallistenAntureidenTilat");
    }

    @Override
    public AnturiSanomaVO haeAnturisanoma(final Long id) {
        throw new NotImplementedException("haeAnturisanoma");
    }
}
