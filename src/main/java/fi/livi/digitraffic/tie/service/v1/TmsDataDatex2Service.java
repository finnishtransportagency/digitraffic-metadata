package fi.livi.digitraffic.tie.service.v1;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.TmsStationData2Datex2Converter;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationDatex2Service;

@ConditionalOnWebApplication
@Service
public class TmsDataDatex2Service {

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final RoadStationSensorService roadStationSensorService;
    private final TmsStationData2Datex2Converter tmsStationData2Datex2Converter;

    public TmsDataDatex2Service(final TmsStationDatex2Service tmsStationDatex2Service,
                                final RoadStationSensorService roadStationSensorService,
                                final TmsStationData2Datex2Converter tmsStationData2Datex2Converter) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.roadStationSensorService = roadStationSensorService;
        this.tmsStationData2Datex2Converter = tmsStationData2Datex2Converter;
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findPublishableTmsDataDatex2() {
        final ZonedDateTime updated = roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION);

        final List<TmsStation> tmsStations = tmsStationDatex2Service.findAllPublishableTmsStations();

        final Map<Long, List<SensorValueDto>> values =
            roadStationSensorService.findAllPublishableRoadStationSensorValuesMappedByNaturalId(RoadStationType.TMS_STATION);

        final Map<TmsStation, List<SensorValueDto>> stations =
            tmsStations.stream().collect(Collectors.toMap(s -> s, s -> values.getOrDefault(s.getRoadStationNaturalId(), Collections.emptyList())));

        return tmsStationData2Datex2Converter.convert(stations, updated);
    }
}
