package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter.TyokoneenseurannanKirjausToWorkMachineTrackingRecordConverter;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.service.v1.MaintenanceDataService;
import fi.livi.digitraffic.tie.service.v1.WorkMachineObservationService;

@Import({ TyokoneenseurannanKirjausToWorkMachineTrackingRecordConverter.class })
public abstract class AbstractWorkmachineDataServiceTest extends AbstractRestWebTest {
    @Autowired
    protected MaintenanceDataService maintenanceDataService;

    @Autowired
    protected WorkMachineObservationService workMachineObservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectReader reader;

    @Before
    public void initObjectReader() {
        reader = objectMapper.readerFor(TyokoneenseurannanKirjausRequestSchema.class);
    }

    protected int convertWorkMachineTrackingsToObservationsAndSave(final Map<Pair<Integer, Integer>, List<MaintenanceDataService.ObservationFeatureWrapper>> unhandledMap) {
        return unhandledMap.entrySet().stream().mapToInt(value -> workMachineObservationService.convertUnhandledWorkMachineTrackingsToObservations(value)).sum();
    }

    protected void readTrackingJsonAndSave(final String dir, final int filesCount) throws IOException {
        int fileNo = 0;
        while ( fileNo < filesCount) {
            TyokoneenseurannanKirjausRequestSchema kirjaus = readTrackingJsonFile(String.format(dir +"/linestring_tracking_%d.json", fileNo ));
            maintenanceDataService.saveWorkMachineTrackingData(kirjaus);
            fileNo++;
        }
        maintenanceDataService.updateWorkMachineTrackingTypes();
    }

    protected TyokoneenseurannanKirjausRequestSchema readTrackingJsonFile(final String fileName) throws IOException {
        final String jsonContent = readResourceContent("classpath:harja/service/" + fileName);
        final TyokoneenseurannanKirjausRequestSchema result = reader.readValue(jsonContent);
        return result;
    }

}