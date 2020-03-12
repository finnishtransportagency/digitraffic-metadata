package fi.livi.digitraffic.tie.service.v2.forecastsection;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v2.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class V2ForecastSectionMetadataService {

    private final DataStatusService dataStatusService;

    private final V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;

    @Autowired
    public V2ForecastSectionMetadataService(final DataStatusService dataStatusService,
                                            final V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao) {
        this.dataStatusService = dataStatusService;
        this.v2ForecastSectionMetadataDao = v2ForecastSectionMetadataDao;
    }

    // TODO Poor performance > 10 s
    @Transactional(readOnly = true)
    public ForecastSectionV2FeatureCollection getForecastSectionV2Metadata(final boolean onlyUpdateInfo, final Integer roadNumber,
                                                                           final Double minLongitude, final Double minLatitude,
                                                                           final Double maxLongitude, final Double maxLatitude,
                                                                           final List<String> naturalIds) {

        final ForecastSectionV2FeatureCollection featureCollection = getFeatureCollection();

        if (onlyUpdateInfo) {
            return featureCollection;
        }

        final List<ForecastSectionV2Feature> features = v2ForecastSectionMetadataDao.findForecastSectionV2Features(roadNumber, minLongitude, minLatitude,
                                                                                                                   maxLongitude, maxLatitude,
                                                                                                                   naturalIds);
        featureCollection.addAll(features);

        return featureCollection;
    }

    private ForecastSectionV2FeatureCollection getFeatureCollection() {
        final ZonedDateTime metadataUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_METADATA);
        final ZonedDateTime metadataChecked = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_METADATA_CHECK);

        return new ForecastSectionV2FeatureCollection(metadataUpdated, metadataChecked);
    }
}
