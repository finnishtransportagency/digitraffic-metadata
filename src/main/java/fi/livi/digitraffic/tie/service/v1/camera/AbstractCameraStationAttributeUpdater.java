package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.model.RoadStationType.CAMERA_STATION;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.Julkisuus;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.TieosoiteVO;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationState;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.service.AbstractRoadStationAttributeUpdater;

public abstract class AbstractCameraStationAttributeUpdater extends AbstractRoadStationAttributeUpdater {

    private static final Logger log = LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class);

    public static boolean updateRoadStationAttributes(final KameraVO from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(from.getKeruunTila())) {
            to.obsolete();
        } else {
            to.unobsolete();
        }
        to.setLotjuId(from.getId());

        final boolean isPublicOld = to.internalIsPublic();
        final boolean isPublicPreviousOld = to.isPublicPrevious();
        final ZonedDateTime publicityStartTimeOld = to.getPublicityStartTime();

        final ZonedDateTime publicityStartTimeNew = from.getJulkisuus() != null ? DateHelper.toZonedDateTimeWithoutMillis(from.getJulkisuus().getAlkaen()) : null;
        final boolean isPublicNew = from.getJulkisuus() != null && JulkisuusTaso.JULKINEN == from.getJulkisuus().getJulkisuusTaso();
        final boolean changed = to.updatePublicity(isPublicNew, publicityStartTimeNew);
        if ( changed ) {
            log.info("method=updateRoadStationAttributes {} roadStationPublicityChanged naturalId={} lotuId={} " +
                     "fromPublic={} toPublic={} fromPreviousPublic={} toPreviousPublic={} " +
                     "fromPublicityStartTime={} toPublicityStartTime={}",
                     to.getType(), CAMERA_STATION.equals(to.getType()) ? "C" + to.getNaturalId() : to.getNaturalId(), to.getLotjuId(),
                     isPublicOld, to.internalIsPublic(), isPublicPreviousOld, to.isPublicPrevious(),
                     publicityStartTimeOld, to.getPublicityStartTime());
        }

        to.setNaturalId(from.getVanhaId().longValue());
        to.setType(CAMERA_STATION);
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
        to.setStartDate(DateHelper.toZonedDateTimeWithoutMillis(from.getAlkamisPaiva()));
        to.setRepairMaintenanceDate(DateHelper.toZonedDateTimeWithoutMillis(from.getKorjaushuolto()));
        to.setAnnualMaintenanceDate(DateHelper.toZonedDateTimeWithoutMillis(from.getVuosihuolto()));
        to.setState(RoadStationState.fromTilaTyyppi(from.getAsemanTila()));
        to.setLocation(from.getAsemanSijainti());
        to.setCountry(from.getMaa());
        to.setPurpose(from.getKayttotarkoitus());

        return updateRoadAddressAttributes(from.getTieosoite(), to.getRoadAddress()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    public static boolean updateRoadAddressAttributes(final TieosoiteVO from, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setRoadNumber(from.getTienumero());
        to.setRoadSection(from.getTieosa());
        to.setDistanceFromRoadSectionStart(from.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(from.getAjorata());
        to.setSideCode(from.getPuoli());
        to.setRoadMaintenanceClass(from.getTienHoitoluokka());
        to.setContractArea(from.getUrakkaAlue());
        to.setContractAreaCode(from.getUrakkaAlueKoodi());
        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    protected static boolean isPublic(EsiasentoVO esiasento) {
        return Julkisuus.JULKINEN.equals(esiasento.getJulkisuus());
    }


}
