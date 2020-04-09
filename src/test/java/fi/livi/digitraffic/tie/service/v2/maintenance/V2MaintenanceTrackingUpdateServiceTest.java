package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.ASFALTOINTI;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithLineString;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithPoints;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createWorkMachines;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.getTaskSetWithIndex;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;

@Import({ V2MaintenanceTrackingUpdateService.class, JacksonAutoConfiguration.class, V2MaintenanceTrackingServiceTestHelper.class })
public class V2MaintenanceTrackingUpdateServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingUpdateServiceTest.class);

    @Autowired
    private V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository;

    @Autowired
    private V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;

    @Autowired
    private V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;

    @Autowired
    private V2MaintenanceTrackingServiceTestHelper testHelper;

    @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
    private int maxGapInMinutes;

    @Before
    public void init() {
        testHelper.clearDb();
    }

    @Test
    public void saveWorkMachineTrackingDataWithMultipleWorkMachines() throws IOException {
        final ZonedDateTime now = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final TyokoneenseurannanKirjausRequestSchema seuranta =
            createMaintenanceTrackingWithPoints(now, 10, 1, workMachines, ASFALTOINTI);
        testHelper.saveTrackingData(seuranta);

        final List<MaintenanceTrackingData> unhandled =
            v2MaintenanceTrackingDataRepository.findUnhandled(100).collect(Collectors.toList());
        assertEquals(1, unhandled.size());

        final String json = unhandled.get(0).getJson();
        IntStream.range(1, machineCount+1).forEach(i -> assertThat(json, CoreMatchers.containsString("Tyokone_" + i)));

        final String formatedJson = testHelper.getFormatedTrackingJson(seuranta);
        assertEquals(formatedJson, json);
    }

    @Test
    public void handleTrackingWithMultipleWorkMachines() throws JsonProcessingException {
        final ZonedDateTime now = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final TyokoneenseurannanKirjausRequestSchema seuranta =
            createMaintenanceTrackingWithPoints(now, 10, 1, workMachines, ASFALTOINTI);
        testHelper.saveTrackingData(seuranta);

        final List<MaintenanceTrackingData> datas = v2MaintenanceTrackingDataRepository.findAll();
        assertCollectionSize(1, datas);
        assertEquals(MaintenanceTrackingData.Status.UNHANDLED, datas.get(0).getStatus());

        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        assertEquals(1, handled);

        final List<MaintenanceTrackingData> datasAfter = v2MaintenanceTrackingDataRepository.findAll();
        assertCollectionSize(1, datasAfter);
        assertEquals(MaintenanceTrackingData.Status.HANDLED, datasAfter.get(0).getStatus());

        final List<MaintenanceTracking> trackings = v2MaintenanceTrackingRepository.findAll(Sort.by("workMachine.harjaId"));
        assertCollectionSize(machineCount, trackings);
        trackings.stream().forEach(t -> {
            // 10 observations for each
            assertEquals(10, t.getLineString().getNumPoints());
            t.getTasks().equals(new HashSet<>(asList(ASFALTOINTI)));
            assertEquals(now, t.getStartTime());
            assertEquals(now.plusMinutes(9), t.getEndTime());
        });

        final List<MaintenanceTrackingWorkMachine> wms =
            trackings.stream().map(t -> t.getWorkMachine()).sorted(Comparator.comparing(MaintenanceTrackingWorkMachine::getHarjaId))
                .collect(Collectors.toList());
        // Check all work machines exists
        IntStream.range(0, machineCount).forEach(i -> assertEquals(i+1, wms.get(i).getHarjaId().intValue()));
    }

    @Test
    public void combineMultipleTrackings() {
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        final ZonedDateTime endTime = startTime.plusMinutes(4);
        IntStream.range(0,5).forEach(i -> {
            final ZonedDateTime start = startTime.plusMinutes(i);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithLineString(start, 10, 1, workMachines, ASFALTOINTI);
            try {
                testHelper.saveTrackingData(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });


        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        assertEquals(5, handled);

        final List<MaintenanceTracking> trackings =
            v2MaintenanceTrackingRepository.findAll(Sort.by("workMachine.harjaId"));
        assertCollectionSize(machineCount, trackings);
        trackings.stream().forEach(t -> {
            // 10 observations for each
            assertEquals(10*5, t.getLineString().getNumPoints());
            t.getTasks().equals(new HashSet<>(asList(ASFALTOINTI)));
            assertEquals(startTime, t.getStartTime());
            assertEquals(endTime, t.getEndTime());
        });

        final List<MaintenanceTrackingWorkMachine> wms =
            trackings.stream().map(t -> t.getWorkMachine()).sorted(Comparator.comparing(MaintenanceTrackingWorkMachine::getHarjaId))
                .collect(Collectors.toList());
        // Check all work machines exists
        IntStream.range(0, machineCount).forEach(i -> assertEquals(i+1, wms.get(i).getHarjaId().intValue()));
    }

    @Test
    public void splitTrackingWhenTaskChanges() {
        final int machineCount = getRandomId(2, 10);
        // Work machines with harja id 1,2,...(machineCount+1)
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();

        IntStream.range(0, 5).forEach(idx -> {
            final ZonedDateTime start = startTime.plusMinutes(idx);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithPoints(start, 10, 1, workMachines, SuoritettavatTehtavat.values()[idx]);
            try {
                testHelper.saveTrackingData(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        assertEquals(5, handled);

        Map<Long, List<MaintenanceTracking>> trackingsByHarjaId =
            v2MaintenanceTrackingRepository.findAll(Sort.by("workMachine.harjaId", "id"))
                .stream()
                .collect(Collectors.groupingBy(mt -> mt.getWorkMachine().getHarjaId()));

        LongStream.range(1, machineCount + 1).forEach(harjaId -> {
            final List<MaintenanceTracking> machineTrackings = trackingsByHarjaId.get(harjaId);
            IntStream.range(0, 5).forEach(idx -> {
                final MaintenanceTracking t = machineTrackings.get(idx);
                assertEquals(getTaskSetWithIndex(idx), t.getTasks());
            });
        });
    }

    @Test
    public void splitTrackingWhenJobChanges() throws JsonProcessingException {
        // Work machines with harja id 1
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(1), 10, 2, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        final int handled = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        assertEquals(2, handled);

        final List<MaintenanceTracking> trackings = v2MaintenanceTrackingRepository.findAll();
        trackings.sort(Comparator.comparing(o -> o.getWorkMachine().getHarjaUrakkaId()));
        assertCollectionSize(2, trackings);
        assertEquals(1L, trackings.get(0).getWorkMachine().getHarjaUrakkaId().longValue());
        assertEquals(2L, trackings.get(1).getWorkMachine().getHarjaUrakkaId().longValue());
    }

    @Test
    public void insideTimeLimitCombinesTrackingsAsOne() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        // Last point will be startTime + 9 min
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // First point will be just 5 min from previous tracking last point -> should combine as same tracking
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(9+maxGapInMinutes), 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        final List<MaintenanceTracking> trackings = v2MaintenanceTrackingRepository.findAll();
        assertCollectionSize(1, trackings);
        assertEquals(20, trackings.get(0).getLineString().getNumPoints());
    }

    @Test
    public void timeLimitBreaksTrackingInParts() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        // Last point will be startTime + 9 min
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // First point will be over 5 min (6 min) from previous tracking last point
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10+maxGapInMinutes), 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        final List<MaintenanceTracking> trackings = v2MaintenanceTrackingRepository.findAll();
        assertCollectionSize(2, trackings);
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));

        final MaintenanceTracking first = trackings.get(0);
        final MaintenanceTracking second = trackings.get(1);
        assertNotEquals(first.getEndTime(), second.getStartTime());
        assertNotEquals(first.getLineString().getEndPoint(), second.getLineString().getStartPoint());
    }

    @Test
    public void timeLimitAndTaskChangeBreaksTrackingInParts() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // Second tracking over time gap and task change
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10 + maxGapInMinutes), 10, 1, workMachines, SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS));
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        final List<MaintenanceTracking> trackings = v2MaintenanceTrackingRepository.findAll();
        assertCollectionSize(2, trackings);
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));

        final MaintenanceTracking first = trackings.get(0);
        final MaintenanceTracking second = trackings.get(1);
        assertNotEquals(first.getEndTime(), second.getStartTime());
        assertNotEquals(first.getLineString().getEndPoint(), second.getLineString().getStartPoint());
    }

    @Test
    public void taskChangeBreaksTrackingAndLastPointOfFirstTrackingIsSameAsFirstPointOfNextTracking() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // First point will be after previous last point -> 10 min after start
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 1, workMachines, SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS));
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        final List<MaintenanceTracking> trackings = v2MaintenanceTrackingRepository.findAll();
        assertCollectionSize(2, trackings);
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));
        final MaintenanceTracking first = trackings.get(0);
        final MaintenanceTracking second = trackings.get(1);

        assertEquals(first.getEndTime(), second.getStartTime());
        assertEquals(first.getLineString().getEndPoint(), second.getLineString().getStartPoint());
    }

    @Test
    public void taskChangeToTransitionBreaksTrackingAndLastPointOfFirstTrackingIsSameAsFirstPointOfTransitionTracking() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingData(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // First point will be after previous last point -> 10 min after start
        final TyokoneenseurannanKirjausRequestSchema transition = createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 1, workMachines);
        testHelper.saveTrackingData(transition);
        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);

        final List<MaintenanceTracking> trackings = v2MaintenanceTrackingRepository.findAll();
        assertCollectionSize(1, trackings);

        final MaintenanceTracking first = trackings.get(0);
        final Havainto transitionHavainto = transition.getHavainnot().get(0).getHavainto();
        final KoordinaattisijaintiSchema koordinaatit = transitionHavainto.getSijainti().getKoordinaatit();
        final Point transitionFirstPoint = CoordinateConverter.convertFromETRS89ToWGS84(new Point(koordinaatit.getX(), koordinaatit.getY()));

        assertEquals(first.getEndTime(), transitionHavainto.getHavaintoaika());
        assertEquals(first.getLineString().getEndPoint().getX(), transitionFirstPoint.getLongitude(), 0.01);
        assertEquals(first.getLineString().getEndPoint().getY(), transitionFirstPoint.getLatitude(), 0.01);
    }
}
