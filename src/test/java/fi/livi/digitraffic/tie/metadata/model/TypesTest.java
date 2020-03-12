package fi.livi.digitraffic.tie.metadata.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaTyyppi;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.AjoneuvoluokkaTyyppi;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LaiteTyyppi;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaTyyppi;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraTyyppi;
import fi.livi.digitraffic.tie.model.CalculatorDeviceType;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationState;
import fi.livi.digitraffic.tie.model.TmsStationType;
import fi.livi.digitraffic.tie.model.VehicleClass;
import fi.livi.digitraffic.tie.model.WeatherStationType;
import fi.livi.digitraffic.tie.model.v1.camera.CameraType;

@RunWith(JUnit4.class)
public class TypesTest {

    @Test
    public void testAllCameraTypes() {
        for (final KameraTyyppi kt : KameraTyyppi.values()) {
            assertNotNull(CameraType.convertFromKameraTyyppi(kt));
        }
    }

    @Test
    public void testAllCameraStationCollectionStates() {
        for (final fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA kt : fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA.values()) {
            assertNotNull(CollectionStatus.convertKeruunTila(kt));
        }
    }

    @Test
    public void testAllWeatherStationCollectionStates() {
        for (final fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.KeruunTILA kt : fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.KeruunTILA.values()) {
            assertNotNull(CollectionStatus.convertKeruunTila(kt));
        }
    }

    @Test
    public void testAllTmsStationCollectionStates() {
        for (final fi.livi.digitraffic.tie.external.lotju.metadata.lam.KeruunTILA kt : fi.livi.digitraffic.tie.external.lotju.metadata.lam.KeruunTILA.values()) {
            assertNotNull(CollectionStatus.convertKeruunTila(kt));
        }
    }

    @Test
    public void testAllTmsStationTypes() {
        for (final LamAsemaTyyppi lat : LamAsemaTyyppi.values()) {
            assertNotNull(TmsStationType.convertFromLamasemaTyyppi(lat));
        }
    }

    @Test
    public void testAllCameraRoadStationStates() {
        for (final fi.livi.digitraffic.tie.external.lotju.metadata.kamera.TilaTyyppi tt : fi.livi.digitraffic.tie.external.lotju.metadata.kamera.TilaTyyppi.values()) {
            assertNotNull(RoadStationState.fromTilaTyyppi(tt));
        }
    }

    @Test
    public void testAllTmsRoadStationStates() {
        for (final fi.livi.digitraffic.tie.external.lotju.metadata.lam.TilaTyyppi tt : fi.livi.digitraffic.tie.external.lotju.metadata.lam.TilaTyyppi.values()) {
            assertNotNull(RoadStationState.fromTilaTyyppi(tt));
        }
    }

    @Test
    public void testAllWeatherRoadStationStates() {
        for (final fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TilaTyyppi tt : fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TilaTyyppi.values()) {
            assertNotNull(RoadStationState.fromTilaTyyppi(tt));
        }
    }

    @Test
    public void testAllWeatherStationTypes() {
        for (final TiesaaAsemaTyyppi tsat : TiesaaAsemaTyyppi.values()) {
            assertNotNull(WeatherStationType.fromTiesaaAsemaTyyppi(tsat));
        }
    }

    @Test
    public void testAllTmsCalculatorDeviceTypes() {
        for (final LaiteTyyppi lt : LaiteTyyppi.values()) {
            assertNotNull(CalculatorDeviceType.convertFromLaiteTyyppi(lt));
        }
    }

    @Test
    public void testAllVehiVehicleClasscleClassTypes() {
        for (final AjoneuvoluokkaTyyppi at : AjoneuvoluokkaTyyppi.values()) {
            assertNotNull(VehicleClass.fromAjoneuvoluokka(at));
        }
        Assert.assertEquals(VehicleClass.CAR, VehicleClass.fromAjoneuvoluokka(AjoneuvoluokkaTyyppi.HA));
        Assert.assertEquals(VehicleClass.CAR, VehicleClass.fromAjoneuvoluokka(AjoneuvoluokkaTyyppi.PA));
        Assert.assertEquals(VehicleClass.BUS, VehicleClass.fromAjoneuvoluokka(AjoneuvoluokkaTyyppi.LA));
    }


}
