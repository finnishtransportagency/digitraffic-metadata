package fi.livi.digitraffic.tie.service.v2.forecastsection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.v2.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionClient;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.ForecastSectionV2Dto;
import reactor.util.Logger;
import reactor.util.Loggers;

@Service
public class V2ForecastSectionMetadataUpdater {
    private final static Logger log = Loggers.getLogger(V2ForecastSectionMetadataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionRepository forecastSectionRepository;

    private final V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;

    private final DataStatusService dataStatusService;

    @Autowired
    public V2ForecastSectionMetadataUpdater(final ForecastSectionClient forecastSectionClient,
                                            final ForecastSectionRepository forecastSectionRepository,
                                            final V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao,
                                            final DataStatusService dataStatusService) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionRepository = forecastSectionRepository;
        this.v2ForecastSectionMetadataDao = v2ForecastSectionMetadataDao;
        this.dataStatusService = dataStatusService;
    }

    @Transactional
    public Instant updateForecastSectionsV2Metadata() {
        final ForecastSectionV2Dto metadata = forecastSectionClient.getForecastSectionV2Metadata();

        final List<String> naturalIds = metadata.getFeatures().stream().map(f -> f.getProperties().getId()).collect(Collectors.toList());

        forecastSectionRepository.deleteAllNotIn(naturalIds, ForecastSectionApiVersion.V2.getVersion());

        v2ForecastSectionMetadataDao.upsertForecastSections(metadata.getFeatures());

        forecastSectionRepository.deleteCoordinates(ForecastSectionApiVersion.V2.getVersion());
        v2ForecastSectionMetadataDao.insertCoordinates(metadata.getFeatures());

        forecastSectionRepository.deleteRoadSegments(ForecastSectionApiVersion.V2.getVersion());
        v2ForecastSectionMetadataDao.insertRoadSegments(metadata.getFeatures());

        forecastSectionRepository.deleteLinkIds(ForecastSectionApiVersion.V2.getVersion());
        v2ForecastSectionMetadataDao.insertLinkIds(metadata.getFeatures());

        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_V2_METADATA_CHECK);
        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_V2_METADATA, metadata.getDataUpdatedTime().toInstant());
        return metadata.getDataUpdatedTime().toInstant();
    }
}
