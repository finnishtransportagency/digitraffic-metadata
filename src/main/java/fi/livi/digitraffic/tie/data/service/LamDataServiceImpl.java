package fi.livi.digitraffic.tie.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LamMeasurementRepository;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;

@Service
public class LamDataServiceImpl implements LamDataService {
    private final LamMeasurementRepository lamMeasurementRepository;

    @Autowired
    public LamDataServiceImpl(final LamMeasurementRepository lamMeasurementRepository) {
        this.lamMeasurementRepository = lamMeasurementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LamRootDataObjectDto listAllLamDataFromNonObsoleteStations() {
        return new LamRootDataObjectDto(lamMeasurementRepository.listAllLamDataFromNonObsoleteStations());
    }
}
