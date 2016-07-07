package fi.livi.digitraffic.tie.metadata.service.camera;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import fi.livi.digitraffic.tie.lotju.wsdl.kamera.KameraVO;
import fi.livi.digitraffic.tie.lotju.wsdl.metatiedot.TieosoiteVO;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationState;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.AbstractRoadStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;


public abstract class AbstractCameraRoadStationUpdater extends AbstractRoadStationUpdater {

    private static final Logger log = Logger.getLogger(AbstractCameraRoadStationUpdater.class);

    protected RoadStationService roadStationService;

    public AbstractCameraRoadStationUpdater(
            RoadStationService roadStationService) {
        this.roadStationService = roadStationService;
    }

    public static boolean updateRoadStationAttributes(final KameraVO from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(from.getKeruunTila()) ) {
            to.obsolete();
        } else {
            to.setObsolete(false);
            to.setObsoleteDate(null);
        }
        to.setNaturalId(from.getVanhaId().longValue());
        to.setType(RoadStationType.CAMERA);
        to.setName(from.getNimi());
        to.setNameFi(from.getNimiFi());
        to.setNameSv(from.getNimiSe());
        to.setNameEn(from.getNimiEn());
        to.setLatitude(from.getLatitudi());
        to.setLongitude(from.getLongitudi());
        to.setAltitude(from.getKorkeus());
        to.setCollectionInterval(from.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(from.getKeruunTila()));
        to.setMunicipality(from.getKunta());
        to.setMunicipalityCode(from.getKuntaKoodi());
        to.setProvince(from.getMaakunta());
        to.setProvinceCode(from.getMaakuntaKoodi());
        to.setLiviId(from.getLiviId());
        to.setStartDate(from.getAlkamisPaiva() != null ? from.getAlkamisPaiva().toGregorianCalendar().toZonedDateTime().toLocalDateTime().withNano(0) : null);
        to.setRepairMaintenanceDate(from.getKorjaushuolto() != null ? from.getKorjaushuolto().toGregorianCalendar().toZonedDateTime().toLocalDateTime().withNano(0) : null);
        to.setAnnualMaintenanceDate(from.getVuosihuolto() != null ? from.getVuosihuolto().toGregorianCalendar().toZonedDateTime().toLocalDateTime().withNano(0) : null);
        to.setState(RoadStationState.convertAsemanTila(from.getAsemanTila()));
        to.setLocation(from.getAsemanSijainti());
        to.setCountry(from.getMaa());

        return updateRoadAddressAttributes(from.getTieosoite(), to.getRoadAddress()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    public static boolean updateRoadAddressAttributes(final TieosoiteVO from, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        String before = ReflectionToStringBuilder.toString(to);

        to.setRoadNumber(from.getTienumero());
        to.setRoadSection(from.getTieosa());
        to.setDistanceFromRoadSectionStart(from.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(from.getAjorata());
        to.setSideCode(from.getPuoli());
        to.setRoadMaintenanceClass(from.getTienHoitoluokka());
        // TODO should we use this for idenfying road address?
        // from.getId()
        if (HashCodeBuilder.reflectionHashCode(to) != hash) {
            log.info("Updated:\n" + before + " ->\n" + ReflectionToStringBuilder.toString(to));
        }
        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
