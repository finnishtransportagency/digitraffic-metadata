package fi.livi.digitraffic.tie.service.v1.weather;

import static fi.livi.digitraffic.tie.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationUpdateService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuWeatherStationMetadataService;

@ConditionalOnNotWebApplication
@Service
public class WeatherStationUpdater  {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationUpdater.class);

    private final RoadStationUpdateService roadStationUpdateService;
    private final WeatherStationService weatherStationService;
    private final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService;

    @Autowired
    public WeatherStationUpdater(final RoadStationUpdateService roadStationUpdateService,
                                 final WeatherStationService weatherStationService,
                                 final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService) {
        this.roadStationUpdateService = roadStationUpdateService;
        this.weatherStationService = weatherStationService;
        this.lotjuWeatherStationMetadataService = lotjuWeatherStationMetadataService;
    }

    /**
     * Updates Weather Stations
     */
    @PerformanceMonitor(maxWarnExcecutionTime = 60000, maxErroExcecutionTime = 90000)
    public boolean updateWeatherStations() {
        final List<TiesaaAsemaVO> tiesaaAsemas = lotjuWeatherStationMetadataService.getTiesaaAsemas();
        return updateWeatherStationsMetadata(tiesaaAsemas);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateWeatherStationsStatuses() {
        final List<TiesaaAsemaVO> allTiesaaAsemas = lotjuWeatherStationMetadataService.getTiesaaAsemas();

        int updated = 0;
        for (TiesaaAsemaVO from : allTiesaaAsemas) {
            try {
                if (roadStationUpdateService.updateRoadStation(from)) {
                    updated++;
                }
            } catch (Exception e) {
                log.error("method=updateWeatherStationsStatuses : Updating roadstation nimiFi=\"{}\" lotjuId={} naturalId={} keruunTila={} failed", from.getNimiFi(), from.getId(), from.getVanhaId(), from.getKeruunTila());
                throw e;
            }
        }
        return updated;
    }

    private boolean updateWeatherStationsMetadata(final List<TiesaaAsemaVO> tiesaaAsemas) {

        int updated = 0;
        int inserted = 0;

        final List<TiesaaAsemaVO> toUpdate =
            tiesaaAsemas.stream().filter(this::validate).collect(Collectors.toList());

        final Collection invalid = CollectionUtils.subtract(tiesaaAsemas, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ReflectionToStringBuilder.toString(i)));

        List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(TiesaaAsemaVO::getId).collect(Collectors.toList());
        int obsoleted = roadStationUpdateService.obsoleteRoadStationsExcludingLotjuIds(RoadStationType.WEATHER_STATION, notToObsoleteLotjuIds);

        for (TiesaaAsemaVO tsa : toUpdate) {
            UpdateStatus result = weatherStationService.updateOrInsertWeatherStation(tsa);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }

        log.info("weatherStationsObsoletedCount={} WeatherStations", obsoleted);
        log.info("weatherStationsUpdatedCount={} WeatherStations", updated);
        log.info("weatherStationsInserted={} WeatherStations", inserted);
        if (!invalid.isEmpty()) {
            log.warn("invalidWeatherStationsFromLotjuCount={}", invalid.size());
        }
        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private boolean validate(final TiesaaAsemaVO tsa) {
        final boolean valid = tsa.getVanhaId() != null;
        if (!valid && !isPermanentlyDeletedKeruunTila(tsa.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(tsa));
        }
        return valid;
    }
}
