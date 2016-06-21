package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.lotju.wsdl.metatiedot.TieosoiteVO;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationState;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;

public abstract class LamRoadStationAttributeUpdater {

    private static final Logger log = Logger.getLogger(LamRoadStationAttributeUpdater.class);

    protected RoadStationService roadStationService;

    public LamRoadStationAttributeUpdater(RoadStationService roadStationService) {
        this.roadStationService = roadStationService;
    }

    protected static boolean updateRoadStationAttributes(final LamAsemaVO from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(from.getKeruunTila()) ) {
            to.obsolete();
        } else {
            to.setObsolete(false);
            to.setObsoleteDate(null);
        }

        to.setNaturalId(from.getVanhaId().longValue());
        to.setType(RoadStationType.LAM_STATION);
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
        to.setStartDate(from.getAlkamisPaiva() != null ? from.getAlkamisPaiva().toGregorianCalendar().toZonedDateTime().toLocalDateTime() : null);
        to.setRepairMaintenanceDate(from.getKorjaushuolto() != null ? from.getKorjaushuolto().toGregorianCalendar().toZonedDateTime().toLocalDateTime() : null);
        to.setAnnualMaintenanceDate(from.getVuosihuolto() != null ? from.getVuosihuolto().toGregorianCalendar().toZonedDateTime().toLocalDateTime(): null);
        to.setState(RoadStationState.convertAsemanTila(from.getAsemanTila()));
        to.setLocation(from.getAsemanSijainti());
        to.setCountry(from.getMaa());

        return updateRoadAddressAttributes(from.getTieosoite(), to.getRoadAddress()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    protected static boolean updateRoadAddressAttributes(final TieosoiteVO from, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadNumber(from.getTienumero());
        to.setRoadSection(from.getTieosa());
        to.setDistanceFromRoadSectionStart(from.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(from.getAjorata());
        to.setSideCode(from.getPuoli());
        to.setRoadMaintenanceClass(from.getTienHoitoluokka());

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    protected RoadAddress resolveOrCreateRoadAddress(LamAsemaVO la, Map<Long, RoadAddress> roadAddressesMappedByLotjuId) {
        if (la.getTieosoiteId() == null) {
            log.info(ToStringHelpper.toString(la) + " had null tieosoiteId");
        }
        // Set road address only if it is set in lotju
        RoadAddress ra = roadAddressesMappedByLotjuId.get(la.getTieosoiteId());
        if (ra == null && la.getTieosoiteId() != null) {
            ra = new RoadAddress(la.getTieosoiteId());
            roadStationService.save(ra);
            roadAddressesMappedByLotjuId.put(ra.getLotjuId(), ra);
            log.info("Created new " + ra);
        }
        return ra;
    }
}
