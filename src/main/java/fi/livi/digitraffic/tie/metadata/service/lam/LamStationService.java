package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.LamStation;

public interface LamStationService {

    /**
     * @return current non obsolete lam stations metadata as LamStationFeatureCollection
     */
    LamStationFeatureCollection findAllNonObsoletePublicLamStationsAsFeatureCollection(boolean onlyUpdateInfo);

    LamStation save(final LamStation lamStation);

    Map<Long, LamStation> findAllLamStationsMappedByByLamNaturalId();

    @Transactional(readOnly = true)
    Map<Long, LamStation> findAllLamStationsMappedByByRoadStationNaturalId();

    LamStation findByLotjuId(long lamStationLotjuId);

    Map<Long, LamStation> findAllLamStationsByMappedByLotjuId();

    Map<Long, LamStation> findLamStationsMappedByLotjuId(List<Long> lamStationLotjuIds);

    LamStation findByRoadStationNaturalId(long roadStationNaturalId);

    boolean lamStationExistsWithRoadStationNaturalId(long roadStationNaturalId);

    boolean lamStationExistsWithNaturalId(long roadStationNaturalId);
}
