package fi.livi.digitraffic.tie.service.v1.tms;

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

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.AbstractRoadStationSensorUpdater;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataService;

@ConditionalOnNotWebApplication
@Service
public class TmsStationSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorUpdater.class);

    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationSensorUpdater(final RoadStationSensorService roadStationSensorService,
                                   final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        super(roadStationSensorService);
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    /**
     * Updates all available tms road station sensors
     */
    public boolean updateRoadStationSensors() {
        log.info("Update TMS RoadStationSensors start");

        // Update available RoadStationSensors types to db
        List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis =
                lotjuTmsStationMetadataService.getAllLamLaskennallinenAnturis();

        boolean updated = updateAllRoadStationSensors(allLamLaskennallinenAnturis);
        log.info("Update TMS RoadStationSensors end");
        return updated;
    }

    private boolean updateAllRoadStationSensors(final List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis) {

        int updated = 0;
        int inserted = 0;

        final List<LamLaskennallinenAnturiVO> toUpdate =
            allLamLaskennallinenAnturis.stream().filter(TmsStationSensorUpdater::validate).collect(Collectors.toList());

        final List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(LamLaskennallinenAnturiVO::getId).collect(Collectors.toList());
        final int obsoleted = roadStationSensorService.obsoleteSensorsExcludingLotjuIds(RoadStationType.TMS_STATION, notToObsoleteLotjuIds);

        final Collection invalid = CollectionUtils.subtract(allLamLaskennallinenAnturis, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ReflectionToStringBuilder.toString(i)));

        for (LamLaskennallinenAnturiVO anturi : toUpdate) {
            UpdateStatus result = roadStationSensorService.updateOrInsert(anturi);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }

        log.info("method=updateAllRoadStationSensors roadStationSensors obsoletedCount={} roadStationType={}", obsoleted, RoadStationType.TMS_STATION);
        log.info("method=updateAllRoadStationSensors roadStationSensors updatedCount={} roadStationType={}", updated, RoadStationType.TMS_STATION);
        log.info("method=updateAllRoadStationSensors roadStationSensors insertedCount={} roadStationType={}", inserted, RoadStationType.TMS_STATION);

        if (!invalid.isEmpty()) {
            log.warn("method=updateAllRoadStationSensors roadStationSensors invalidCount={} roadStationType={}", invalid.size(), RoadStationType.TMS_STATION);
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private static boolean validate(LamLaskennallinenAnturiVO anturi) {
        return anturi.getId() != null && anturi.getVanhaId() != null;
    }
}
