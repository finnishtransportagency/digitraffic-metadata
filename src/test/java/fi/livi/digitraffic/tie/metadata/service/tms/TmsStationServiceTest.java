package fi.livi.digitraffic.tie.metadata.service.tms;

import static fi.livi.digitraffic.tie.metadata.controller.TmsState.ACTIVE;
import static fi.livi.digitraffic.tie.metadata.controller.TmsState.ALL;
import static fi.livi.digitraffic.tie.metadata.controller.TmsState.REMOVED;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

public class TmsStationServiceTest extends AbstractTest {

    @Autowired
    private TmsStationService tmsStationService;

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollection() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, ACTIVE);

        assertCollectionSize(stations.getFeatures(), 493);
    }

    @Test
    public void findPermanentlyRemovedStations() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, REMOVED);

        assertCollectionSize(stations.getFeatures(), 45);
    }

    @Test
    public void findAllStations() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, ALL);

        assertCollectionSize(stations.getFeatures(), 538);
    }

    @Test
    public void findAllTmsStationsMappedByByTmsNaturalId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsMappedByByTmsNaturalId();

        assertCollectionSize(stations.entrySet(), 545);
    }

    @Test
    public void findAllTmsStationsByMappedByLotjuId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsByMappedByLotjuId();

        assertCollectionSize(stations.entrySet(), 545);
    }
}
