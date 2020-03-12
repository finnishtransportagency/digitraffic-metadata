package fi.livi.digitraffic.tie;

import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.conf.amazon.AmazonS3ClientTestConfiguration;
import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Config;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.converter.StationSensorConverter;
import fi.livi.digitraffic.tie.converter.feature.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.dao.v1.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.dao.v1.workmachine.WorkMachineObservationDao;
import fi.livi.digitraffic.tie.helper.FileGetService;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.FlywayService;
import fi.livi.digitraffic.tie.service.RoadDistrictService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.v1.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.service.v1.TmsDataService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageReader;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageS3Writer;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2TrafficAlertHttpClient;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v1.datex2.StringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.v1.location.LocationService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2HelperService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

@Import({ StringToObjectMarshaller.class, XmlMarshallerConfiguration.class, RestTemplate.class, RetryTemplate.class,
          // services
          LocationService.class, RoadDistrictService.class, CameraPresetService.class, TmsStationService.class, DataStatusService.class,
          RoadStationService.class, FreeFlowSpeedService.class, TmsStationSensorConstantService.class, RoadStationSensorService.class,
          TmsDataService.class, CameraImageUpdateService.class, CameraImageReader.class, CameraImageS3Writer.class,
          Datex2TrafficAlertHttpClient.class, Datex2UpdateService.class, FileGetService.class,
          V2Datex2UpdateService.class, V2Datex2HelperService.class,
          CameraPresetHistoryUpdateService.class, FlywayService.class,

          // converters
          TmsStationMetadata2FeatureConverter.class, CoordinateConverter.class, StationSensorConverter.class,
          ObjectMapper.class,

          // daos
          TmsSensorConstantDao.class, WorkMachineObservationDao.class,

          // configurations
          AmazonS3ClientTestConfiguration.class, WeathercamS3Config.class
        })
@TestPropertySource(properties = { "spring.localstack.enabled=false" })
public abstract class AbstractServiceTest extends AbstractJpaTest {
}
