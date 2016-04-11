package fi.livi.digitraffic.tie.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.RoadStationStatusRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationStatuses;

@Service
public class RoadStationStatusServiceImpl implements RoadStationStatusService {
    private final RoadStationStatusRepository roadStationStatusRepository;

    @Autowired
    public RoadStationStatusServiceImpl(RoadStationStatusRepository roadStationStatusRepository) {
        this.roadStationStatusRepository = roadStationStatusRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RoadStationStatuses findAllRoadStationStatuses() {
        return new RoadStationStatuses(roadStationStatusRepository.findAllRoadStationStatuses());
    }
}
