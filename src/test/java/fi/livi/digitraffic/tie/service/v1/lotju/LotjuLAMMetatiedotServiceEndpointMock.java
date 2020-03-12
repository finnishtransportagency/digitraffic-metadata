package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.ArvoVastaavuusVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAsemanAnturiVakioResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiAnturiVakioArvotResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMAsematResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMAsemanLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LAMMetatiedotEndpoint;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LAMMetatiedotEndpointImplService;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.ObjectFactory;


public class LotjuLAMMetatiedotServiceEndpointMock extends LotjuServiceEndpointMock implements LAMMetatiedotEndpoint {

    private static final Logger log = LoggerFactory.getLogger(LotjuLAMMetatiedotServiceEndpointMock.class);
    private static final String LOTJU_LAM_RESOURCE_PATH = "lotju/lam/";
    private static LotjuLAMMetatiedotServiceEndpointMock instance;

    public static LotjuLAMMetatiedotServiceEndpointMock getInstance(final String tmsMetadataServerAddress, final ResourceLoader resourceLoader,
                                                                    final Jaxb2Marshaller jaxb2Marshaller) {
        if (instance == null) {
            instance = new LotjuLAMMetatiedotServiceEndpointMock(tmsMetadataServerAddress, resourceLoader, jaxb2Marshaller);
        }
        return instance;
    }

    private LotjuLAMMetatiedotServiceEndpointMock(final String tmsMetadataServerAddress, final ResourceLoader resourceLoader,
                                                  final Jaxb2Marshaller jaxb2Marshaller) {
        super(resourceLoader, tmsMetadataServerAddress, LAMMetatiedotEndpoint.class,
              LAMMetatiedotEndpointImplService.SERVICE, jaxb2Marshaller, LOTJU_LAM_RESOURCE_PATH);
    }

    @Override
    public void initStateAndService() {
        if (!isInited()) {
            initService();
        }
        setStateAfterChange(false);
    }

    @Override
    protected Class<?> getObjectFactoryClass() {
        return ObjectFactory.class;
    }

    /* LAMMetatiedot Service methods */

    @Override
    public LamAnturiVakioArvoVO haeAnturiVakioArvot(final Long anturiVakioId, final Integer paiva, final Integer kuukausi) {
        throw new NotImplementedException("haeAnturiVakioArvot");
    }

    @Override
    public LamAsemaVO haeLAMAsema(final Long id) {
        throw new NotImplementedException("haeLAMAsema");
    }

    @Override
    public List<LamAnturiVakioArvoVO> haeKaikkiAnturiVakioArvot(final Integer paiva, final Integer kuukausi) {
        HaeKaikkiAnturiVakioArvotResponse response = readLotjuSoapResponse(HaeKaikkiAnturiVakioArvotResponse.class);
        if (response != null) {
            return response.getLamanturivakiot();
        }
        return Collections.emptyList();
    }

    @Override
    public ArvoVastaavuusVO haeArvovastaavuus(final Long id) {
        throw new NotImplementedException("haeArvovastaavuus");
    }

    @Override
    public List<LamAnturiVakioArvoVO> haeAsemanAnturiVakioArvot(final Long asemaId, final Integer paiva, final Integer kuukausi) {
        throw new NotImplementedException("haeAsemanAnturiVakioArvot");
    }

    @Override
    public LamAnturiVO haeLAMAnturi(final Long id) {
        throw new NotImplementedException("haeLAMAnturi");
    }

    @Override
    public List<LamLaskennallinenAnturiVO> haeKaikkiLAMLaskennallisetAnturit() {
        HaeKaikkiLAMLaskennallisetAnturitResponse response = readLotjuSoapResponse(HaeKaikkiLAMLaskennallisetAnturitResponse.class);
        if (response != null) {
            return response.getLaskennallinenAnturi();
        }
        return Collections.emptyList();
    }

    @Override
    public List<ArvoVastaavuusVO> haeLaskennallisenAnturinArvovastaavuudet(final Long arg0) {
        throw new NotImplementedException("haeLaskennallisenAnturinArvovastaavuudet");
    }

    @Override
    public List<ArvoVastaavuusVO> haeKaikkiArvovastaavuudet() {
        throw new NotImplementedException("haeKaikkiArvovastaavuudet");
    }

    @Override
    public LamAnturiVakioVO haeAnturiVakio(final Long anturiVakioId) {
        throw new NotImplementedException("haeAnturiVakio");
    }

    @Override
    public List<LamAnturiVO> haeKaikkiLAMAnturit() {
        throw new NotImplementedException("haeKaikkiLAMAnturit");
    }

    @Override
    public List<LamLaskennallinenAnturiVO> haeLAMAsemanLaskennallisetAnturit(final Long id) {
        HaeLAMAsemanLaskennallisetAnturitResponse response = readLotjuSoapResponse(HaeLAMAsemanLaskennallisetAnturitResponse.class, id);
        if (response != null) {
            return response.getLamlaskennallisetanturit();
        }
        return Collections.emptyList();
    }

    @Override
    public List<LamAsemaLaskennallinenAnturiVO> haeLAMAsemanLaskennallistenAntureidenTilat(final Long asemaId) {
        throw new NotImplementedException("haeLAMAsemanLaskennallistenAntureidenTilat");
    }

    @Override
    public List<LamAnturiVO> haeLAMAsemanAnturit(final Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public void updateAsemaTilatieto(Long asemaId, String tieto) {
        throw new NotImplementedException("updateAsemaTilatieto");
    }

    @Override
    public List<LamAsemaVO> haeKaikkiLAMAsemat() {
        HaeKaikkiLAMAsematResponse response = readLotjuSoapResponse(HaeKaikkiLAMAsematResponse.class);
        if (response != null) {
            return response.getAsemat();
        }
        return Collections.emptyList();
    }

    @Override
    public LamLaskennallinenAnturiVO haeLAMLaskennallinenAnturi(final Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturiVakioVO> haeAsemanAnturiVakio(final Long asemaId) {
        HaeAsemanAnturiVakioResponse response = readLotjuSoapResponse( HaeAsemanAnturiVakioResponse.class, asemaId);
        if (response != null) {
            return response.getLamanturivakiot();
        }
        return Collections.emptyList();
    }
}
