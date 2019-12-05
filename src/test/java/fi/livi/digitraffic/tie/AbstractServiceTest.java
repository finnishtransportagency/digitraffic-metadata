package fi.livi.digitraffic.tie;

import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.conf.amazon.AmazonS3ClientTestConfiguration;
import fi.livi.digitraffic.tie.conf.jaxb2.MetadataMarshallerConfiguration;
import fi.livi.digitraffic.tie.dao.v1.WorkMachineObservationDao;
import fi.livi.digitraffic.tie.data.service.CameraImageReader;
import fi.livi.digitraffic.tie.data.service.CameraImageS3Writer;
import fi.livi.digitraffic.tie.data.service.CameraImageUpdateService;
import fi.livi.digitraffic.tie.data.service.Datex2UpdateService;
import fi.livi.digitraffic.tie.data.service.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.data.service.TmsDataService;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2TrafficAlertHttpClient;
import fi.livi.digitraffic.tie.data.service.datex2.StringToObjectMarshaller;
import fi.livi.digitraffic.tie.helper.FileGetService;
import fi.livi.digitraffic.tie.metadata.converter.StationSensorConverter;
import fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.FlywayService;
import fi.livi.digitraffic.tie.metadata.service.RoadDistrictService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.location.LocationService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

@Import({ StringToObjectMarshaller.class, MetadataMarshallerConfiguration.class, RestTemplate.class, RetryTemplate.class,
          // services
          LocationService.class, RoadDistrictService.class, CameraPresetService.class, TmsStationService.class, DataStatusService.class,
          RoadStationService.class, FreeFlowSpeedService.class, TmsStationSensorConstantService.class, RoadStationSensorService.class,
          TmsDataService.class, CameraImageUpdateService.class, CameraImageReader.class, CameraImageS3Writer.class,
          Datex2TrafficAlertHttpClient.class, Datex2UpdateService.class, FileGetService.class,
          CameraPresetHistoryService.class, FlywayService.class,

          // converters
          TmsStationMetadata2FeatureConverter.class, CoordinateConverter.class, StationSensorConverter.class,

          // daos
          TmsSensorConstantDao.class, WorkMachineObservationDao.class,

          // configurations
          AmazonS3ClientTestConfiguration.class
        })
@TestPropertySource(properties = { "spring.localstack.enabled=false" })
public abstract class AbstractServiceTest extends AbstractJpaTest {
}
