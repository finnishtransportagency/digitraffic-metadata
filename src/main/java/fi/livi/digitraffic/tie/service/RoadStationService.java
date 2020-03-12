package fi.livi.digitraffic.tie.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.RoadStationRepository;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;

@Service
public class RoadStationService {

    private static final Logger log = LoggerFactory.getLogger(RoadStationService.class);

    private final RoadStationRepository roadStationRepository;

    @Autowired
    public RoadStationService(final RoadStationRepository roadStationRepository) {
        this.roadStationRepository = roadStationRepository;
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findByType(final RoadStationType type) {
        return roadStationRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public RoadStation findByTypeAndLotjuId(final RoadStationType type, Long lotjuId) {
        return roadStationRepository.findByTypeAndLotjuId(type, lotjuId);
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findAll() {
        return roadStationRepository.findAll();
    }
}
