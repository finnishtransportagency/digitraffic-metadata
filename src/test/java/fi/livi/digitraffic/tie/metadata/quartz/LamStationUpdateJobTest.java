package fi.livi.digitraffic.tie.metadata.quartz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractMetadataTest;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationsSensorsUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LamMetatiedotLotjuServiceMock;

public class LamStationUpdateJobTest extends AbstractMetadataTest {

    @Autowired
    private LamStationSensorUpdater lamStationSensorUpdater;

    @Autowired
    private LamStationsSensorsUpdater lamStationsSensorsUpdater;

    @Autowired
    private LamStationUpdater lamStationUpdater;

    @Autowired
    private LamStationService lamStationService;

    @Autowired
    private LamMetatiedotLotjuServiceMock lamMetatiedotLotjuServiceMock;

    @Test
    public void testUpdateLamStations() {

        lamMetatiedotLotjuServiceMock.initDataAndService();

        // Update lamstations to initial state (3 non obsolete stations and 1 obsolete)
        lamStationSensorUpdater.updateRoadStationSensors();
        lamStationUpdater.updateLamStations();
        lamStationsSensorsUpdater.updateLamStationsSensors();
        final LamStationFeatureCollection allInitial =
                lamStationService.findAllNonObsoletePublicLamStationsAsFeatureCollection(false);
        assertEquals(3, allInitial.getFeatures().size());

        // Now change lotju metadata and update lam stations (2 non obsolete stations and 2 obsolete)
        lamMetatiedotLotjuServiceMock.setStateAfterChange(true);
        lamStationSensorUpdater.updateRoadStationSensors();
        lamStationUpdater.updateLamStations();
        lamStationsSensorsUpdater.updateLamStationsSensors();
        final LamStationFeatureCollection allAfterChange =
                lamStationService.findAllNonObsoletePublicLamStationsAsFeatureCollection(false);
        assertEquals(2, allAfterChange.getFeatures().size());

        assertNotNull(findWithLotjuId(allInitial, 1));
        assertNull(findWithLotjuId(allInitial, 2));
        assertNotNull(findWithLotjuId(allInitial, 310));
        assertNotNull(findWithLotjuId(allInitial, 581));

        assertNotNull(findWithLotjuId(allAfterChange, 1));
        assertNull(findWithLotjuId(allAfterChange, 2));
        assertNotNull(findWithLotjuId(allAfterChange, 310));
        assertNull(findWithLotjuId(allAfterChange, 581));

        assertEquals(CollectionStatus.GATHERING, findWithLotjuId(allInitial, 1).getProperties().getCollectionStatus());
        assertEquals(CollectionStatus.GATHERING, findWithLotjuId(allInitial, 310).getProperties().getCollectionStatus());
        assertEquals(CollectionStatus.REMOVED_TEMPORARILY, findWithLotjuId(allInitial, 581).getProperties().getCollectionStatus());

        assertEquals(CollectionStatus.GATHERING, findWithLotjuId(allAfterChange, 1).getProperties().getCollectionStatus());
        assertEquals(CollectionStatus.GATHERING, findWithLotjuId(allAfterChange, 310).getProperties().getCollectionStatus());

        /*
        <id>310</id>
            <kuvaus>Liikennemittausasema</kuvaus> -> Liikennemittausasema 1
            <nimi>L_vt5_Iisalmi</nimi> -> L_vt5_Idensalmi
            <korkeus>0</korkeus> -> 1
            <latitudi>7048448</latitudi> -> 7048449
            <longitudi>512364</longitudi> -> 512365
            <tieosoite>
            <etaisyysTieosanAlusta>4750</etaisyysTieosanAlusta> -> 3750
            <tienumero>5</tienumero>
            <tieosa>217</tieosa>
            </tieosoite>
            <tieosoiteId>24</tieosoiteId>
            <keruuVali>300</keruuVali> -> 200
            <keruunTila>KERUUSSA</keruunTila>
            <maakunta>Pohjois-Savo</maakunta> -> Pohjois-Savvoo
            <maakuntaKoodi>11</maakuntaKoodi>
            <nimiEn>Road 5 Iisalmi</nimiEn> -> Road 5 Idensalmi
            <nimiFi>Tie 5 Iisalmi</nimiFi> -> Tie 5 Idensalmi
            <nimiSe>Väg 5 Idensalmi</nimiSe>
            <vanhaId>23826</vanhaId>
            <suunta1Kunta>Kajaani</suunta1Kunta> -> Kajaaniin
            <suunta1KuntaKoodi>205</suunta1KuntaKoodi>
            <suunta2Kunta>Kuopio</suunta2Kunta> -> Kuopioon
            <suunta2KuntaKoodi>297</suunta2KuntaKoodi>
            <tyyppi>DSL</tyyppi>
        */
        final LamStationFeature before = findWithLotjuId(allInitial, 310);
        final LamStationFeature after = findWithLotjuId(allAfterChange, 310);

        assertEquals("L_vt5_Iisalmi", before.getProperties().getName());
        assertEquals("L_vt5_Idensalmi", after.getProperties().getName());

        assertEquals(512364.0, before.getProperties().getLongitudeETRS89(), 0.001);
        assertEquals(512365.0, after.getProperties().getLongitudeETRS89(), 0.001);
        assertEquals(7048448.0, before.getProperties().getLatitudeETRS89(), 0.001);
        assertEquals(7048449.0, after.getProperties().getLatitudeETRS89(), 0.001);
        assertEquals(0.0, before.getProperties().getAltitudeETRS89(), 0.001);
        assertEquals(1.0, after.getProperties().getAltitudeETRS89(), 0.001);
        assertEquals(0.0, before.getProperties().getAltitudeETRS89(), 0.001);
        assertEquals(1.0, after.getProperties().getAltitudeETRS89(), 0.001);

        assertEquals(27.24890844195089, before.getGeometry().getLongitude(), 0.00000000000001);
        assertEquals(27.248928651749267, after.getGeometry().getLongitude(), 0.00000000000001);

        assertEquals(63.56393711859063, before.getGeometry().getLatitude(), 0.00000000000001);
        assertEquals(63.56394605816233, after.getGeometry().getLatitude(), 0.00000000000001);

        assertEquals(0.0, before.getGeometry().getAltitude(), 0.00000000000001);
        assertEquals(1.0, after.getGeometry().getAltitude(), 0.00000000000001);

        assertEquals((Integer) 4750, before.getProperties().getRoadAddress().getDistanceFromRoadSectionStart());
        assertEquals((Integer) 3750, after.getProperties().getRoadAddress().getDistanceFromRoadSectionStart());

        assertEquals((Integer) 300, before.getProperties().getCollectionInterval());
        assertEquals((Integer) 200, after.getProperties().getCollectionInterval());

        assertEquals("Pohjois-Savo", before.getProperties().getProvince());
        assertEquals("Pohjois-Savvoo", after.getProperties().getProvince());

        assertEquals("Tie 5 Iisalmi", before.getProperties().getNames().get("fi"));
        assertEquals("Tie 5 Idensalmi", after.getProperties().getNames().get("fi"));

        assertEquals("Väg 5 Idensalmi", before.getProperties().getNames().get("sv"));
        assertEquals("Väg 5 Idensalmi", after.getProperties().getNames().get("sv"));

        assertEquals("Road 5 Iisalmi", before.getProperties().getNames().get("en"));
        assertEquals("Road 5 Idensalmi", after.getProperties().getNames().get("en"));

        assertEquals("Kajaani", before.getProperties().getDirection1Municipality());
        assertEquals("Kajaaniin", after.getProperties().getDirection1Municipality());

        assertEquals("Kuopio", before.getProperties().getDirection2Municipality());
        assertEquals("Kuopioon", after.getProperties().getDirection2Municipality());

    }

    private LamStationFeature findWithLotjuId(final LamStationFeatureCollection collection, final long lotjuId) {
        final Optional<LamStationFeature> initial =
                collection.getFeatures().stream()
                        .filter(x -> x.getProperties().getLotjuId() == lotjuId)
                        .findFirst();
        return initial.orElse(null);
    }
}
