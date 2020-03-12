package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinate;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinateList;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinateListPK;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinatePK;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.Coordinate;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesDto;

@Service
public class ForecastSectionV1MetadataUpdater {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionV1MetadataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionRepository forecastSectionRepository;
    private final DataStatusService dataStatusService;

    @Autowired
    public ForecastSectionV1MetadataUpdater(final ForecastSectionClient forecastSectionClient,
                                            final ForecastSectionRepository forecastSectionRepository,
                                            final DataStatusService dataStatusService) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionRepository = forecastSectionRepository;
        this.dataStatusService = dataStatusService;
    }

    /**
     * @return Returns true if one or more forecast sections were updated
     */
    @Transactional
    public boolean updateForecastSectionV1Metadata() {

        final List<ForecastSectionCoordinatesDto> forecastSectionCoordinates = forecastSectionClient.getForecastSectionV1Metadata();

        final List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsOrderByNaturalIdAsc(1);
        final Set<String> existingForecastSections = forecastSections.stream().map(ForecastSection::getNaturalId).collect(Collectors.toSet());

        printLogInfo(forecastSectionCoordinates, forecastSections);

        final Map<String, ForecastSection> naturalIdToForecastSections = forecastSections.stream().collect(Collectors.toMap(ForecastSection::getNaturalId, fs -> fs));

        final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToAdd = forecastSectionCoordinates.stream().filter(
                fs -> !existingForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSectionCoordinatesDto::getNaturalId, c -> c));

        final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToUpdate = forecastSectionCoordinates.stream().filter(
                fs -> existingForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSectionCoordinatesDto::getNaturalId, c -> c));

        final Set<String> receivedForecastSections = forecastSectionCoordinates.stream().map(ForecastSectionCoordinatesDto::getNaturalId).collect(Collectors.toSet());

        final Map<String, ForecastSection> forecastSectionsToDelete = forecastSections.stream().filter(
                fs -> !receivedForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSection::getNaturalId, c -> c));

        addForecastSections(naturalIdToForecastSections, forecastSectionsToAdd);
        boolean updated = updateForecastSections(naturalIdToForecastSections, forecastSectionsToUpdate);
        markForecastSectionsObsolete(naturalIdToForecastSections, forecastSectionsToDelete);

        forecastSectionRepository.saveAll(naturalIdToForecastSections.values());
        forecastSectionRepository.flush();

        final boolean metadataUpdated = forecastSectionCoordinates.size() != existingForecastSections.size() || forecastSectionsToDelete.size() > 0 || updated;

        if (metadataUpdated) {
            dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_METADATA_CHECK);

        return metadataUpdated;
    }

    private void addForecastSections(final Map<String, ForecastSection> forecastSections, final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToAdd) {

        for (final Map.Entry<String, ForecastSectionCoordinatesDto> fs : forecastSectionsToAdd.entrySet()) {

            final ForecastSectionCoordinatesDto forecastSection = fs.getValue();

            final ForecastSection newForecastSection = new ForecastSection(forecastSection.getNaturalId(), 1, forecastSection.getName());
            forecastSectionRepository.saveAndFlush(newForecastSection);
            newForecastSection.addCoordinates( forecastSection.getCoordinates());
            forecastSectionRepository.saveAndFlush(newForecastSection);
            forecastSections.put(fs.getValue().getNaturalId(), newForecastSection);
        }
    }

    private boolean updateForecastSections(final Map<String, ForecastSection> forecastSections, final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToUpdate) {

        boolean updated = false;
        for (final Map.Entry<String, ForecastSectionCoordinatesDto> fs : forecastSectionsToUpdate.entrySet()) {
            final ForecastSection forecastSection = forecastSections.get(fs.getValue().getNaturalId());

            if (!corresponds(forecastSection, fs.getValue())) {
                log.info("Updating forecastSection=" + forecastSection.toString() + " with forecastSectionData=" + fs.toString());
                updated = true;
            }
            forecastSection.setDescription(fs.getValue().getName());
            forecastSection.setObsoleteDate(null);

            forecastSection.removeCoordinateLists();
            forecastSectionRepository.saveAndFlush(forecastSection);

            addCoordinates(forecastSection, fs.getValue().getCoordinates());
            forecastSectionRepository.saveAndFlush(forecastSection);
        }
        return updated;
    }

    private void addCoordinates(final ForecastSection forecastSection, final List<Coordinate> coordinates) {
        final List<ForecastSectionCoordinate> coordinateList = new ArrayList<>();

        long orderNumber = 1;
        for (final Coordinate coordinate : coordinates) {
            if (!coordinate.isValid()) {
                log.info("Invalid coordinates for forecastSection=" + forecastSection.getNaturalId() + " . coordinates=" + coordinate.toString());
            } else {
                coordinateList.add(new ForecastSectionCoordinate(
                    new ForecastSectionCoordinatePK(forecastSection.getId(), 1L, orderNumber), coordinate.longitude, coordinate.latitude));
                orderNumber++;
            }
        }
        final ForecastSectionCoordinateList list =
            new ForecastSectionCoordinateList(new ForecastSectionCoordinateListPK(forecastSection.getId(), 1L), coordinateList);
        forecastSection.getForecastSectionCoordinateLists().add(list);
    }

    private void markForecastSectionsObsolete(final Map<String, ForecastSection> forecastSections, final Map<String, ForecastSection> forecastSectionsToDelete) {

        for (final Map.Entry<String, ForecastSection> fs : forecastSectionsToDelete.entrySet()) {
            final ForecastSection forecastSection = forecastSections.get(fs.getValue().getNaturalId());
            forecastSection.setObsoleteDate(Date.from(Instant.now()));
        }
    }

    public static boolean corresponds(final ForecastSection forecastSection, final ForecastSectionCoordinatesDto value) {
        if (value.getName().equals(forecastSection.getDescription()) && coordinatesCorrespond(forecastSection, value.getCoordinates())) {
            return true;
        }
        return false;
    }

    private static boolean coordinatesCorrespond(final ForecastSection forecastSection, final List<Coordinate> coordinates) {

        List<ForecastSectionCoordinate> coordinateList = new ArrayList<>();
        if (!forecastSection.getForecastSectionCoordinateLists().isEmpty()) {
            coordinateList = forecastSection.getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates();
        }

        if (coordinateList.size() != coordinates.size()) return false;

        final List<Coordinate> sorted1 = coordinateList.stream().sorted((a, b) -> {
            if (a.getLongitude().equals(b.getLongitude())) {
                return a.getLatitude().compareTo(b.getLatitude());
            }
            return a.getLongitude().compareTo(b.getLongitude());
        }).map(c -> new Coordinate(Arrays.asList(c.getLongitude(), c.getLatitude()))).collect(Collectors.toList());

        final List<Coordinate> sorted2 = coordinates.stream().sorted((a, b) -> {
            if (a.longitude.equals(b.longitude)) {
                return a.latitude.compareTo(b.latitude);
            }
            return a.longitude.compareTo(b.longitude);
        }).collect(Collectors.toList());

        for (int i = 0; i < coordinateList.size(); ++i) {
            if (sorted1.get(i).longitude.compareTo(sorted2.get(i).longitude) != 0 ||
                sorted1.get(i).latitude.compareTo(sorted2.get(i).latitude) != 0) {
                return false;
            }
        }
        return true;
    }

    private void printLogInfo(final List<ForecastSectionCoordinatesDto> roadSectionCoordinates, final List<ForecastSection> forecastSections) {

        log.info(String.format("Updating forecast sections. existingForecastSections=%s receivedForecastSections=%s",
                               forecastSections.size(), roadSectionCoordinates.size()));
        final List<String> externalNaturalIds = roadSectionCoordinates.stream().map(c -> c.getNaturalId()).collect(Collectors.toList());
        final List<String> existingNaturalIds = forecastSections.stream().map(f -> f.getNaturalId()).collect(Collectors.toList());
        final List<String> newForecastSectionNaturalIds = externalNaturalIds.stream().filter(n -> !existingNaturalIds.contains(n)).collect(Collectors.toList());
        final List<String> missingForecastSectionNaturalIds = existingNaturalIds.stream().filter(n -> !externalNaturalIds.contains(n)).collect(Collectors.toList());
        log.info(String.format("newForecastSections=%s with naturalIds=%s deletedForecastSections=%s with naturalIds=%s",
                               newForecastSectionNaturalIds.size(), StringUtils.join(newForecastSectionNaturalIds, ","),
                               missingForecastSectionNaturalIds.size(), StringUtils.join(missingForecastSectionNaturalIds, ",")));
    }
}
