package fi.livi.digitraffic.tie.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.dao.v1.RoadAddressRepository;
import fi.livi.digitraffic.tie.dao.v1.RoadStationRepository;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.service.v1.camera.AbstractCameraStationAttributeUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.AbstractTmsStationAttributeUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.AbstractWeatherStationAttributeUpdater;

@ConditionalOnNotWebApplication
@Service
public class RoadStationUpdateService {

    private static final Logger log = LoggerFactory.getLogger(RoadStationUpdateService.class);

    private final RoadStationRepository roadStationRepository;

    private final RoadAddressRepository roadAddressRepository;
    private final EntityManager entityManager;

    @Autowired
    public RoadStationUpdateService(final RoadStationRepository roadStationRepository,
                                    final RoadAddressRepository roadAddressRepository,
                                    final EntityManager entityManager) {
        this.roadStationRepository = roadStationRepository;
        this.roadAddressRepository = roadAddressRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public RoadStation save(final RoadStation roadStation) {
        return roadStationRepository.save(roadStation);
    }

    @Transactional
    public RoadAddress save(final RoadAddress roadAddress) {
        return roadAddressRepository.save(roadAddress);
    }

    @Transactional
    public boolean updateRoadStation(final LamAsemaVO from) {
        final RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.TMS_STATION, from.getId());
        if (rs == null) { // Not found from db
            return false;
        }
        if (from.getVanhaId() == null) {
            log.warn("method=updateRoadStation incoming LamAsema vanhaId is null. fromId={} toNaturalId={} toId={}",
                     from.getId(), rs.getNaturalId(), rs.getId());
            return false;
        }
        return AbstractTmsStationAttributeUpdater.updateRoadStationAttributes(from, rs);
    }

    @Transactional
    public boolean updateRoadStation(final TiesaaAsemaVO from) {
        final RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.WEATHER_STATION, from.getId());
        if (rs == null) { // Ei löydy kannnasta
            return false;
        }
        if (from.getVanhaId() == null) {
            log.warn("method=updateRoadStation incoming TiesaaAsema vanhaId is null. fromId={} toNaturalId={} toId={}",
                     from.getId(), rs.getNaturalId(), rs.getId());
            return false;
        }
        return AbstractWeatherStationAttributeUpdater.updateRoadStationAttributes(from, rs);
    }

    @Transactional
    public boolean updateRoadStation(final KameraVO from) {
        final RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, from.getId());
        if (rs == null) { // Ei löydy kannnasta
            return false;
        }
        if (from.getVanhaId() == null) {
            log.warn("method=updateRoadStation incoming Kamera vanhaId is null. fromId={} toNaturalId={} toId={}",
                     from.getId(), rs.getNaturalId(), rs.getId());
            return false;
        }

        return AbstractCameraStationAttributeUpdater.updateRoadStationAttributes(from, rs);
    }

    @Transactional
    public int obsoleteRoadStationsExcludingLotjuIds(final RoadStationType roadStationType, final List<Long> roadStationsLotjuIdsNotToObsolete) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaUpdate<RoadStation> update = cb.createCriteriaUpdate(RoadStation.class);
        final Root<RoadStation> root = update.from(RoadStation.class);
        final EntityType<RoadStation> rootModel = root.getModel();
        update.set("obsoleteDate", LocalDate.now());

        final List<Predicate> predicates = new ArrayList<>();
        predicates.add( cb.equal(root.get(rootModel.getSingularAttribute("roadStationType", RoadStationType.class)), roadStationType));
        predicates.add( cb.isNull(root.get(rootModel.getSingularAttribute("obsoleteDate", LocalDate.class))) );
        for (final List<Long> ids : Iterables.partition(roadStationsLotjuIdsNotToObsolete, 1000)) {
            predicates.add(cb.not(root.get("lotjuId").in(ids)));
        }
        update.where(cb.and(predicates.toArray(new Predicate[0])));

        return this.entityManager.createQuery(update).executeUpdate();
    }
}
