package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.feature.ForecastSectionV1ToFeatureConverter;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dto.v1.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class ForecastSectionV1MetadataService {

    private final ForecastSectionRepository forecastSectionRepository;

    private final ForecastSectionV1ToFeatureConverter forecastSectionV1ToFeatureConverter;

    private final DataStatusService dataStatusService;

    @Autowired
    public ForecastSectionV1MetadataService(final ForecastSectionRepository forecastSectionRepository,
                                            final DataStatusService dataStatusService,
                                            final ForecastSectionV1ToFeatureConverter forecastSectionV1ToFeatureConverter) {
        this.forecastSectionRepository = forecastSectionRepository;
        this.dataStatusService = dataStatusService;
        this.forecastSectionV1ToFeatureConverter = forecastSectionV1ToFeatureConverter;
    }

    @Transactional(readOnly = true)
    public ForecastSectionFeatureCollection findForecastSectionsV1Metadata() {
        List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsOrderByNaturalIdAsc(1);
        return forecastSectionV1ToFeatureConverter.convert(forecastSections,
                                                           dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_METADATA),
                                                           dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_METADATA_CHECK));
    }

    public ForecastSectionsMetadata findForecastSectionsV1Metadata(final boolean onlyUpdateInfo) {

        final ForecastSectionFeatureCollection features = findForecastSectionsV1Metadata();

        return new ForecastSectionsMetadata(
                    onlyUpdateInfo ? null : features,
                    features.getDataUpdatedTime(),
                    features.getDataLastCheckedTime());
    }
}
