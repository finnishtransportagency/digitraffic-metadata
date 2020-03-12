package fi.livi.digitraffic.tie.service.v1;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v2.V2DeviceDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2DeviceRepository;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.Laite;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.LiikennemerkinTila;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.Metatiedot;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.Tilatiedot;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.Device;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.DeviceData;

@Service
public class VariableSignUpdateService {
    private static final Logger log = LoggerFactory.getLogger(VariableSignUpdateService.class);

    private final V2DeviceRepository v2DeviceRepository;
    private final V2DeviceDataRepository v2DeviceDataRepository;

    public VariableSignUpdateService(final V2DeviceRepository v2DeviceRepository, final V2DeviceDataRepository v2DeviceDataRepository) {
        this.v2DeviceRepository = v2DeviceRepository;
        this.v2DeviceDataRepository = v2DeviceDataRepository;
    }

    @Transactional
    public void saveMetadata(final Metatiedot metadata) {
        final Map<String, Laite> idMap = metadata.getLaitteet().stream()
            .collect(Collectors.toMap(l -> l.getTunnus(), l -> l));

        final List<Device> devices = v2DeviceRepository.findAllById(idMap.keySet());

        updateDevices(devices, idMap);
        // updateDevices removes updated from map
        insertDevices(idMap);

        log.debug(idMap.toString());
    }

    private void insertDevices(final Map<String, Laite> idMap) {
        final StopWatch sw = StopWatch.createStarted();

        try {
            v2DeviceRepository.saveAll(idMap.values().stream().map(this::convertDevice).collect(Collectors.toList()));
        } finally {
            log.info("insertDeviceMetadataCount={} tookMs={}", idMap.size(), sw.getTime());
        }
    }

    private void updateDeviceInformation(final Device d, final Laite l) {
        d.setType(l.getTyyppi());
        d.setRoadAddress(l.getSijainti().getTieosoite());
        d.setEtrsTm35FinX(l.getSijainti().getE());
        d.setEtrsTm35FinY(l.getSijainti().getN());
        d.setDirection(l.getSijainti().getAjosuunta());
        d.setCarriageway(l.getSijainti().getAjorata());
    }

    private Device convertDevice(final Laite laite) {
        final Device d = new Device();
        d.setId(laite.getTunnus());

        updateDeviceInformation(d, laite);

        return d;
    }

    private void updateDevices(final List<Device> devices, final Map<String, Laite> idMap) {
        final int updateCount = devices.size();
        final StopWatch sw = StopWatch.createStarted();

        try {
            devices.forEach(d -> {
                final Laite laite = idMap.get(d.getId());

                if (laite == null) {
                    log.error("Could not find device " + d.getId());
                } else {
                    updateDeviceInformation(d, laite);

                    idMap.remove(d.getId());
                }
            });
        } finally {
            log.info("updateDeviceMetadataCount={} tookMs={}", updateCount, sw.getTime());
        }
    }

    @Transactional
    public void saveData(final Tilatiedot data) {
        final StopWatch sw = StopWatch.createStarted();

        try {
            v2DeviceDataRepository.saveAll(data.getLiikennemerkit().stream()
                .map(this::convertData)
                .collect(Collectors.toList()));
        } finally {
            log.info("updateDeviceDataCount={} tookMs={}", data.getLiikennemerkit().size(), sw.getTime());
        }
    }

    private DeviceData convertData(final LiikennemerkinTila lt) {
        final DeviceData d = new DeviceData();

        d.setCreatedDate(ZonedDateTime.now());
        d.setAdditionalInformation(lt.getLisatieto());
        d.setCause(lt.getSyy());
        d.setDeviceId(lt.getTunnus());
        d.setDisplayValue(lt.getNayttama());
        d.setEffectDate(lt.getVoimaan());
        d.setReliability(lt.getLuotettavuus());

        return d;
    }
}
