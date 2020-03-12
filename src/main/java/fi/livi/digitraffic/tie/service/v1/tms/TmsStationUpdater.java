package fi.livi.digitraffic.tie.service.v1.tms;

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
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationUpdateService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataService;

@ConditionalOnNotWebApplication
@Service
public class TmsStationUpdater {

    private static final Logger log = LoggerFactory.getLogger(TmsStationUpdater.class);

    private final RoadStationUpdateService roadStationUpdateService;
    private final TmsStationService tmsStationService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationUpdater(final RoadStationUpdateService roadStationUpdateService,
                             final TmsStationService tmsStationService,
                             final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        this.roadStationUpdateService = roadStationUpdateService;
        this.tmsStationService = tmsStationService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 60000)
    public boolean updateTmsStations() {
        final List<LamAsemaVO> asemas = lotjuTmsStationMetadataService.getLamAsemas();
        return updateTmsStationsMetadata(asemas);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateTmsStationsStatuses() {
        final List<LamAsemaVO> allLams = lotjuTmsStationMetadataService.getLamAsemas();

        int updated = 0;
        for(LamAsemaVO from : allLams) {
            try {
                if (roadStationUpdateService.updateRoadStation(from)) {
                    updated++;
                }
            } catch (Exception e) {
                log.error("method=updateTmsStationsStatuses : Updating roadstation nimiFi=\"{}\" lotjuId={} naturalId={} keruunTila={} failed", from.getNimiFi(), from.getId(), from.getVanhaId(), from.getKeruunTila());
                throw e;
            }
        }
        return updated;
    }

    private boolean updateTmsStationsMetadata(final List<LamAsemaVO> lamAsemas) {

        int updated = 0;
        int inserted = 0;

        final List<LamAsemaVO> toUpdate =
            lamAsemas.stream().filter(this::validate).collect(Collectors.toList());

        final List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(LamAsemaVO::getId).collect(Collectors.toList());
        final int obsoleted = roadStationUpdateService.obsoleteRoadStationsExcludingLotjuIds(RoadStationType.TMS_STATION, notToObsoleteLotjuIds);
        log.info("Not to obsolete lotju ids {}", notToObsoleteLotjuIds);

        final Collection<LamAsemaVO> invalid = (Collection<LamAsemaVO>)CollectionUtils.subtract(lamAsemas, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ReflectionToStringBuilder.toString(i)));

        for (LamAsemaVO tsa : toUpdate) {
            UpdateStatus result = tmsStationService.updateOrInsertTmsStation(tsa);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }


        log.info("Obsoleted={} TmsStations", obsoleted);
        log.info("Updated={} TmsStations", updated);
        log.info("Inserted={} TmsStations", inserted);
        if (!invalid.isEmpty()) {
            log.warn("Invalid WeatherStations from lotju invalidCount={}", invalid.size());
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private boolean validate(final LamAsemaVO lamAsema) {
        final boolean valid = lamAsema.getVanhaId() != null;
        if(!valid && !isPermanentlyDeletedKeruunTila(lamAsema.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(lamAsema));
        }
        return valid;
    }
}
