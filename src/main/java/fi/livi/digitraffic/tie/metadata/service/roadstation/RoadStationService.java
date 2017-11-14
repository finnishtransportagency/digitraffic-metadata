package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.metadata.dao.RoadAddressRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.camera.AbstractCameraStationAttributeUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.AbstractTmsStationAttributeUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.AbstractWeatherStationAttributeUpdater;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;

@Service
public class RoadStationService {

    private static final Logger log = LoggerFactory.getLogger(RoadStationService.class);

    private final RoadStationRepository roadStationRepository;

    private final RoadAddressRepository roadAddressRepository;
    private final EntityManager entityManager;

    @Autowired
    public RoadStationService(final RoadStationRepository roadStationRepository,
                              final RoadAddressRepository roadAddressRepository,
                              final EntityManager entityManager) {
        this.roadStationRepository = roadStationRepository;
        this.roadAddressRepository = roadAddressRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findByType(final RoadStationType type) {
        return roadStationRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStation> findByTypeMappedByNaturalId(final RoadStationType type) {
        final List<RoadStation> all = findByType(type);

        final Map<Long, RoadStation> map = new HashMap<>();
        for (final RoadStation roadStation : all) {
            map.put(roadStation.getNaturalId(), roadStation);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public RoadStation findByTypeAndNaturalId(final RoadStationType type, Long naturalId) {
        return roadStationRepository.findByTypeAndNaturalId(type, naturalId);
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStation> findOrphansByTypeMappedByNaturalId(final RoadStationType type) {
        final List<RoadStation> orphans;
        if (RoadStationType.TMS_STATION == type) {
            orphans = roadStationRepository.findOrphanTmsRoadStations();
        } else if (RoadStationType.CAMERA_STATION == type) {
            orphans = roadStationRepository.findOrphanCameraRoadStations();
        } else if (RoadStationType.WEATHER_STATION == type) {
            orphans = roadStationRepository.findOrphanWeatherRoadStations();
        } else {
            throw new IllegalArgumentException("RoadStationType " + type + " is unknown");
        }

        final Map<Long, RoadStation> map = new HashMap<>();
        for (final RoadStation roadStation : orphans) {
            map.put(roadStation.getNaturalId(), roadStation);
        }
        return map;
    }

    @Transactional
    public RoadStation save(final RoadStation roadStation) {
        return roadStationRepository.save(roadStation);
    }

    @Transactional
    public RoadAddress save(final RoadAddress roadAddress) {
        return roadAddressRepository.save(roadAddress);
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findAll() {
        return roadStationRepository.findAll();
    }

    @Transactional
    public boolean updateRoadStation(LamAsemaVO from) {
        RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.TMS_STATION, from.getId());
        if (rs == null) { // Ei löydy kannnasta
            return false;
        }
        if (from.getVanhaId() == null) {
            log.warn("method=updateRoadStation incoming LamAsema vanhaId is null. fromId={} fromNimiFI={} toNaturalId={} toId={}",
                     from.getId(), from.getNimiFi().replaceAll("\\s", "_"), rs.getNaturalId(), rs.getId());
            return false;
        }
        return AbstractTmsStationAttributeUpdater.updateRoadStationAttributes(from, rs);
    }

    @Transactional
    public boolean updateRoadStation(TiesaaAsemaVO from) {
        RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.WEATHER_STATION, from.getId());
        if (rs == null) { // Ei löydy kannnasta
            return false;
        }
        if (from.getVanhaId() == null) {
            log.warn("method=updateRoadStation incoming TiesaaAsema vanhaId is null. fromId={} fromNimiFI={} toNaturalId={} toId={}",
                     from.getId(), from.getNimiFi().replaceAll("\\s", "_"), rs.getNaturalId(), rs.getId());
            return false;
        }
        return AbstractWeatherStationAttributeUpdater.updateRoadStationAttributes(from, rs);
    }

    @Transactional
    public boolean updateRoadStation(KameraVO from) {
        RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, from.getId());
        if (rs == null) { // Ei löydy kannnasta
            return false;
        }
        if (from.getVanhaId() == null) {
            log.warn("method=updateRoadStation incoming Kamera vanhaId is null. fromId={} fromNimiFI={} toNaturalId={} toId={}",
                     from.getId(), from.getNimiFi().replaceAll("\\s", "_"), rs.getNaturalId(), rs.getId());
            return false;
        }
        return AbstractCameraStationAttributeUpdater.updateRoadStationAttributes(from, rs);
    }

    @Transactional
    public int obsoleteRoadStationsExcludingLotjuIds(final RoadStationType roadStationType, final List<Long> roadStationsLotjuIdsNotToObsolete) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaUpdate<RoadStation> update = cb.createCriteriaUpdate(RoadStation.class);
        final Root<RoadStation> root = update.from(RoadStation.class);
        EntityType<RoadStation> rootModel = root.getModel();
        update.set("obsoleteDate", LocalDate.now());
        update.set("obsolete", true);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add( cb.equal(root.get(rootModel.getSingularAttribute("roadStationType", RoadStationType.class)), roadStationType));
        predicates.add( cb.or(cb.isNull(root.get(rootModel.getSingularAttribute("obsoleteDate", LocalDate.class))), cb.notEqual(root.get(rootModel.getSingularAttribute("obsolete", Boolean.class)), true)) );
        for (List<Long> ids : Iterables.partition(roadStationsLotjuIdsNotToObsolete, 1000)) {
            predicates.add(cb.not(root.get("lotjuId").in(ids)));
        }
        update.where(cb.and(predicates.toArray(new Predicate[0])));

        return this.entityManager.createQuery(update).executeUpdate();
    }
}
