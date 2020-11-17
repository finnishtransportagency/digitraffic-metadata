package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.ASFALTOINTI;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.AURAUS_JA_SOHJONPOISTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_JUOTOSTYOT;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.Point;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.CRACK_FILLING;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.PAVING;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.PLOUGHING_AND_SLUSH_REMOVAL;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_X_MAX;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_X_MIN;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_Y_MAX;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_Y_MIN;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithLineString;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithPoints;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createWorkMachines;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createWorkmachine;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.getEndTime;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.getTaskSetWithIndex;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.getTaskSetWithTasks;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.getTaskWithIndex;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingProperties;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;

@Import({ V2MaintenanceTrackingUpdateService.class, JacksonAutoConfiguration.class,
          V2MaintenanceTrackingServiceTestHelper.class, V2MaintenanceTrackingDataService.class })
public class V2MaintenanceTrackingDataServiceTest extends AbstractServiceTest {

    final static Pair<Double, Double> BOUNDING_BOX_X_RANGE = Pair.of(20.0, 30.0);
    final static Pair<Double, Double> BOUNDING_BOX_Y_RANGE = Pair.of(64.0, 66.0);
    final static Pair<Double, Double> BOUNDING_BOX_CENTER = Pair.of(25.0, 65.0);

    @Autowired
    private V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;

    @Autowired
    private V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;

    @Autowired
    private V2MaintenanceTrackingServiceTestHelper testHelper;

    @Before
    public void init() {
        testHelper.clearDb();
    }

    @Test
    public void findCombinedTrackingsWithMultipleWorkMachines() throws JsonProcessingException {
        final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final TyokoneenseurannanKirjausRequestSchema seuranta1 =
            createMaintenanceTrackingWithPoints(start, 10, 1, 1, workMachines, ASFALTOINTI);
        final TyokoneenseurannanKirjausRequestSchema seuranta2 =
            createMaintenanceTrackingWithPoints(start.plusMinutes(10), 10, 2,1, workMachines, ASFALTOINTI);
        final ZonedDateTime end = getEndTime(seuranta2);

        testHelper.saveTrackingData(seuranta1);
        testHelper.saveTrackingData(seuranta2);
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(start, end, PAVING).getFeatures();
        assertCollectionSize(machineCount, features);

        final Set<Long> workMachineUniqueIds = new HashSet<>();
        features.forEach(t -> {
            final MaintenanceTrackingProperties properties = t.getProperties();
            // As observations are simplified, we can't know how many points is left in geometry
            assertTrue(t.getGeometry().getCoordinates().size() > 1);
            assertEquals(getTaskSetWithTasks(PAVING), t.getProperties().tasks);
            assertEquals(start, properties.startTime);
            assertEquals(end, properties.endTime);
            // {machineCount} unique machines
            assertFalse(workMachineUniqueIds.contains(properties.workMachineId));
            workMachineUniqueIds.add(properties.workMachineId);
        });
    }

    @Test
    public void findCombinedMultipleTrackings() {
        final int machineCount = 1;//getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();


        // Generate 5 trackings for each machine
        final ZonedDateTime endTime = IntStream.range(0, 5).mapToObj(i -> {
            final ZonedDateTime start = startTime.plusMinutes(i*10);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithPoints(start, 10, i + 1, 1, workMachines, ASFALTOINTI, PAALLYSTEIDEN_JUOTOSTYOT);
            try {
                testHelper.saveTrackingData(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return getEndTime(seuranta);
        }).max(ChronoZonedDateTime::compareTo).orElseThrow();

        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        assertEquals(5, handled);

        final List<MaintenanceTrackingLatestFeature> latestFeatures = findLatestMaintenanceTrackings(startTime, endTime).getFeatures();
        assertCollectionSize(machineCount, latestFeatures);
        assertAllHasOnlyPointGeometries(latestFeatures);

        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, endTime).getFeatures();
        assertCollectionSize(machineCount, features);

        features.forEach(t -> {
            // 10 observations for each
            final MaintenanceTrackingProperties properties = t.getProperties();
            // As observations are simplified, we can't know how many points is left in geometry
            assertTrue(t.getGeometry().getCoordinates().size() > 1);
            assertEquals(getTaskSetWithTasks(PAVING, CRACK_FILLING), properties.tasks);
            assertEquals(startTime, properties.startTime);
            assertEquals(endTime, properties.endTime);
        });
    }

    @Test
    public void findTrackingWithDifferentTasks() {
        final int machineCount = getRandomId(2, 10);
        // Work machines with harja id 1,2,...(machineCount+1)
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();

        // Generate 5 different tracking with different tasks for each machine
        // Each machine successive trackings will bombine next tracking first point as previous tracking last point
        IntStream.range(0, 5).forEach(idx -> {
            final ZonedDateTime start = startTime.plusMinutes(idx*10);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithPoints(start, 10, 1, workMachines, SuoritettavatTehtavat.values()[idx]);
            try {
                testHelper.saveTrackingData(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        // Without task parameter all should be found
        List<MaintenanceTrackingFeature> allFeatures =
            findMaintenanceTrackings(startTime, startTime.plusMinutes(10 * 5 + 9)).getFeatures();
        assertCollectionSize(machineCount*5, allFeatures);

        // Latest should return latest one per machine
        List<MaintenanceTrackingLatestFeature> latestFeatures =
            findLatestMaintenanceTrackings(startTime, startTime.plusMinutes(10 * 5 + 9)).getFeatures();
        assertCollectionSize(machineCount, latestFeatures);
        assertAllHasOnlyPointGeometries(latestFeatures);

        // Find with tasks
        IntStream.range(0, 5).forEach(idx -> {
            final ZonedDateTime endTime = startTime.plusMinutes(idx*10+10); // 10 observations end time is 10 min after first
            final List<MaintenanceTrackingFeature> features =
                findMaintenanceTrackings(startTime, endTime, getTaskWithIndex(idx)).getFeatures();
            assertCollectionSize(machineCount, features);
            features.forEach(f -> assertEquals(getTaskSetWithIndex(idx), f.getProperties().tasks));
        });
    }

    @Test
    public void findTrackingWithDifferentJobsForSameMachine() throws JsonProcessingException {
        // Work machines with harja id 1
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();
        // tracking with job 1
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // tracking with job 2
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 2, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        // 2 tracking should be made as they have different jobs
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, startTime.plusMinutes(10+10)).getFeatures();
        assertCollectionSize(2, features);

        // 2 tracking should be found as latest as same machine with different job id is handled as different machine
        final List<MaintenanceTrackingLatestFeature> latestFeatures = findLatestMaintenanceTrackings(startTime, startTime.plusMinutes(10+10)).getFeatures();
        assertCollectionSize(2, latestFeatures);
        assertAllHasOnlyPointGeometries(latestFeatures);
    }

    @Test
    public void findSeparateTrackingsWhenTransitionInBetweenTasks() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();
        // tracking with job 1
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 1, workMachines));
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10+10), 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        // 2 tracking should be made as they have different jobs
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, startTime.plusMinutes(10+10+10)).getFeatures();
        assertCollectionSize(2, features);

        // Only the latest one should be found
        final List<MaintenanceTrackingLatestFeature> latestFeatures = findLatestMaintenanceTrackings(startTime, startTime.plusMinutes(10+10+10)).getFeatures();
        assertCollectionSize(1, latestFeatures);
        assertEquals(startTime.plusMinutes(10+10+9), latestFeatures.get(0).getProperties().getTime());
        assertAllHasOnlyPointGeometries(latestFeatures);
    }

    @Test
    public void findSeparateTrackingsWhenTrackingTimeOverlaps() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();
        // tracking with startime T1 and end time T1+9
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // tracking with startime T1+1 and end time T1+10
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(1), 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        // 2 tracking should be made as firs ones end time is after second one's start time.
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, startTime.plusMinutes(1+9)).getFeatures();
        assertCollectionSize(2, features);

        // Only the latest one should be found
        final List<MaintenanceTrackingLatestFeature> latestFeatures = findLatestMaintenanceTrackings(startTime, startTime.plusMinutes(1+9)).getFeatures();
        assertCollectionSize(1, latestFeatures);
        assertEquals(startTime.plusMinutes(1+9), latestFeatures.get(0).getProperties().getTime());
        assertAllHasOnlyPointGeometries(latestFeatures);
    }

    @Test
    public void findTrackingDataJsonsByTrackingId() throws IOException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();
        // Create 2 tracking data that are combined as one tracking
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1,1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 2, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, startTime.plusMinutes(20)).getFeatures();
        assertCollectionSize(1, features);

        final List<JsonNode> jsons = v2MaintenanceTrackingDataService.findTrackingDataJsonsByTrackingId(features.get(0).getProperties().id);
        assertCollectionSize(2, jsons);
    }

    @Test
    public void findWithMultipleTasks() throws JsonProcessingException {
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();
        final ZonedDateTime endTime = startTime.plusMinutes(9);
        // Generate 5 trackings for each machine
        testHelper.saveTrackingData(createMaintenanceTrackingWithLineString(startTime, 10, 1, workMachines,
            AURAUS_JA_SOHJONPOISTO, PAALLYSTEIDEN_JUOTOSTYOT)); // PLOUGHING_AND_SLUSH_REMOVAL, CRACK_FILLING
        testHelper.saveTrackingData(createMaintenanceTrackingWithLineString(startTime, 10, 2, workMachines,
            ASFALTOINTI)); // PAVING
        testHelper.saveTrackingData(createMaintenanceTrackingWithLineString(startTime, 10, 3, workMachines,
            AURAUS_JA_SOHJONPOISTO, PAALLYSTEIDEN_PAIKKAUS)); // PLOUGHING_AND_SLUSH_REMOVAL, PATCHING

        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        assertEquals(3, handled);

        // First two should be returned
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, endTime, CRACK_FILLING, PAVING).getFeatures();
        assertCollectionSize(machineCount*2, features);

        // Check that each features tasks are AURAUS_JA_SOHJONPOISTO and PAALLYSTEIDEN_JUOTOSTYOT or ASFALTOINTI
        features.forEach(f -> {
            final Set<MaintenanceTrackingTask> tasks = f.getProperties().tasks;
            if (tasks.size() == 2) {
                assertEquals(new HashSet<>(asList(PLOUGHING_AND_SLUSH_REMOVAL, CRACK_FILLING)), tasks);
            } else {
                assertEquals(new HashSet<>(singletonList(PAVING)), tasks);
            }
        });
    }

    /**
     *      | - linestring
     * –––––|––––-
     * |    |    | - bounding box
     * |    |    |
     * –––––|-----
     *      |
     */
    @Test
    public void findMaintenanceTrackingWithLinestringCrossingWithBoundingBox() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();

        List<List<Double>> fromWGS84 = createVerticalLineStringWGS84(BOUNDING_BOX_CENTER.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft()-0.5, BOUNDING_BOX_Y_RANGE.getRight() + 0.5);

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingData(
            V2MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        Assert.assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList());
        Assert.assertEquals(1, result.getFeatures().size());
        final MaintenanceTrackingProperties props = result.getFeatures().get(0).getProperties();

        Assert.assertEquals(startTime, props.startTime);
        Assert.assertEquals(startTime, props.endTime);
    }

    private List<List<Double>> createVerticalLineStringWGS84(final double x, final double minY, final double maxY) {
        final double increment = 0.01; // keeps distance between points < 2 km
        final double range = maxY - minY;
        final int points = (int) (range / increment);

        return IntStream.range(1, points+1)
            .mapToObj(i -> asList(x, minY + (i*increment)))
            .collect(Collectors.toList());
    }

    /**
     *                          | - linestring
     *                –––––––––-|
     * bounding box - |        ||
     *                |        ||
     *                –––––––––-|
     *                          |
     */
    @Test
    public void findMaintenanceTrackingWithLinestringNotCrossingWithBoundingBox() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();

        List<List<Double>> fromWGS84 = createVerticalLineStringWGS84(BOUNDING_BOX_X_RANGE.getRight() + 0.1,
                                                                     BOUNDING_BOX_Y_RANGE.getLeft() - 10,
                                                                     BOUNDING_BOX_Y_RANGE.getRight() + 10);
        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingData(
            V2MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        Assert.assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList());
        Assert.assertEquals(0, result.getFeatures().size());
    }

    /**
     * ––––––––––-
     * |  point  | - bounding box
     * |    `    |
     * ––––––-----
     */
    @Test
    public void findMaintenanceTrackingWithPointInsideBoundingBox() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();

        List<List<Double>> fromWGS84 = singletonList(
            asList(BOUNDING_BOX_CENTER.getLeft(), BOUNDING_BOX_CENTER.getRight())
        );

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingData(
            V2MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        Assert.assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList());
        Assert.assertEquals(1, result.getFeatures().size());
        final MaintenanceTrackingProperties props = result.getFeatures().get(0).getProperties();

        Assert.assertEquals(startTime, props.startTime);
        Assert.assertEquals(startTime, props.endTime);
    }

    /**
     * ––––––––––-
     * |         | - bounding box
     * |         |` - point
     * ––––––-----
     */
    @Test
    public void findMaintenanceTrackingWithPointOutsideBoundingBox() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc();

        List<List<Double>> fromWGS84 = singletonList(
            asList(BOUNDING_BOX_X_RANGE.getRight() + 0.1, BOUNDING_BOX_CENTER.getRight())
        );

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingData(
            V2MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        Assert.assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList());
        Assert.assertEquals(0, result.getFeatures().size());
    }

    private MaintenanceTrackingFeatureCollection findMaintenanceTrackings(final ZonedDateTime start, final ZonedDateTime end,
                                                                              final MaintenanceTrackingTask...tasks) {
        return v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            start.toInstant(), end.toInstant(),
            RANGE_X_MIN, RANGE_Y_MIN, RANGE_X_MAX, RANGE_Y_MAX,
            asList(tasks));
    }

    private MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(final ZonedDateTime start, final ZonedDateTime end,
                                                                                      final MaintenanceTrackingTask...tasks) {
        return v2MaintenanceTrackingDataService.findLatestMaintenanceTrackings(
            start.toInstant(), end.toInstant(),
            RANGE_X_MIN, RANGE_Y_MIN, RANGE_X_MAX, RANGE_Y_MAX,
            asList(tasks));
    }

    private void assertAllHasOnlyPointGeometries(final List<MaintenanceTrackingLatestFeature> features) {
        features.forEach(f -> assertEquals(Point, f.getGeometry().getType()));
    }
}
