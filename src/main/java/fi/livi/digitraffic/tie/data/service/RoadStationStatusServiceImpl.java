package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.RoadStationStatusRepository;
import fi.livi.digitraffic.tie.data.dto.RoadStationStatusesDataObjectDto;

@Service
public class RoadStationStatusServiceImpl implements RoadStationStatusService {
    private final RoadStationStatusRepository roadStationStatusRepository;

    @Autowired
    public RoadStationStatusServiceImpl(RoadStationStatusRepository roadStationStatusRepository) {
        this.roadStationStatusRepository = roadStationStatusRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RoadStationStatusesDataObjectDto findPublicRoadStationStatuses(boolean onlyUpdateInfo) {
        LocalDateTime updated = roadStationStatusRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            return new RoadStationStatusesDataObjectDto(updated);
        } else {
            return new RoadStationStatusesDataObjectDto(
                    roadStationStatusRepository.findAllPublicRoadStationStatuses(),
                    updated);
        }
    }
}
