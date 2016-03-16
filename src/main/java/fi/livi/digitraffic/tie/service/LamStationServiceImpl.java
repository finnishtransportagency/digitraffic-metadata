package fi.livi.digitraffic.tie.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.converter.LamStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.dao.LamStationMetadataRepository;
import fi.livi.digitraffic.tie.dao.LamStationRepository;
import fi.livi.digitraffic.tie.model.LamStation;
import org.apache.log4j.Logger;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LamStationServiceImpl implements LamStationService {

    private static final Logger LOG = Logger.getLogger(LamStationServiceImpl.class);

    private final LamStationRepository lamStationRepository;
    private final LamStationMetadataRepository lamStationMetadataRepository;

    @Autowired
    LamStationServiceImpl(final LamStationRepository lamStationRepository,
                          final LamStationMetadataRepository lamStationMetadataRepository) {
        this.lamStationRepository = lamStationRepository;
        this.lamStationMetadataRepository = lamStationMetadataRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public FeatureCollection findAllNonObsoleteLamStationsAsFeatureCollection() {
        return LamStationMetadata2FeatureConverter.convert(lamStationMetadataRepository.findAllNonObsolete());
    }

    @Transactional
    @Override
    public LamStation save(LamStation lamStation) {
        return lamStationRepository.save(lamStation);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LamStation> findAll() {
        return lamStationRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, LamStation> findAllLamStationsMappedByByNaturalId() {

        final List<LamStation> allStations = lamStationRepository.findAll();
        final Map<Long, LamStation> stationMap = new HashMap<>();

        for(final LamStation lam : allStations) {
            stationMap.put(lam.getNaturalId(), lam);
        }

        return stationMap;
    }
}
