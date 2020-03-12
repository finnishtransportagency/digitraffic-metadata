package fi.livi.digitraffic.tie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.RoadDistrictRepository;
import fi.livi.digitraffic.tie.model.v1.RoadDistrict;

@Service
public class RoadDistrictService {

    private final RoadDistrictRepository roadDistrictRepository;

    @Autowired
    public RoadDistrictService(final RoadDistrictRepository roadDistrictRepository) {
        this.roadDistrictRepository = roadDistrictRepository;
    }

    @Transactional(readOnly = true)
    public RoadDistrict findByNaturalId(final int roadDistrictNumber) {
        return roadDistrictRepository.findByNaturalId(roadDistrictNumber);
    }

    @Transactional(readOnly = true)
    public RoadDistrict findByRoadSectionAndRoadNaturalId(final int roadSectionNaturalId, final int roadNaturalId) {
        return roadDistrictRepository.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId);
    }

}
