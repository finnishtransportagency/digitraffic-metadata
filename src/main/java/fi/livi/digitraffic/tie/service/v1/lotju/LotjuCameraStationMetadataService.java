package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.AbstractVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;

@ConditionalOnNotWebApplication
@Service
public class LotjuCameraStationMetadataService {
    private static final Logger log = LoggerFactory.getLogger(LotjuCameraStationMetadataService.class);
    private final LotjuCameraStationMetadataClient lotjuCameraStationClient;

    @Autowired
    public LotjuCameraStationMetadataService(final LotjuCameraStationMetadataClient lotjuCameraStationClient) {
        this.lotjuCameraStationClient = lotjuCameraStationClient;
    }

    public Set<Long> getKamerasLotjuids() {
        return lotjuCameraStationClient.getKameras().stream().map(AbstractVO::getId).collect(Collectors.toSet());
    }

    public KameraVO getKamera(final long kameraLotjuId) {
        return lotjuCameraStationClient.getKamera(kameraLotjuId);
    }

    public List<EsiasentoVO> getEsiasentos(final Long kameraId) {
        return lotjuCameraStationClient.getEsiasentos(kameraId);
    }

    public EsiasentoVO getEsiasento(final long esiasentoLotjuId) {
        return lotjuCameraStationClient.getEsiasento(esiasentoLotjuId);
    }
}
