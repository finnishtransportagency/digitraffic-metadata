package fi.livi.digitraffic.tie.data.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.VmsDatex2Converter;
import fi.livi.digitraffic.tie.data.dao.DeviceDataRepository;
import fi.livi.digitraffic.tie.data.dao.DeviceRepository;
import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;
import fi.livi.digitraffic.tie.data.model.trafficsigns.DeviceData;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTableDatex2Response;

@Service
public class VariableSignDatex2Service {
    private final DeviceDataRepository deviceDataRepository;
    private final DeviceRepository deviceRepository;

    private final VmsDatex2Converter vmsDatex2Converter;

    public VariableSignDatex2Service(final DeviceDataRepository deviceDataRepository, final DeviceRepository deviceRepository, final VmsDatex2Converter vmsDatex2Converter) {
        this.deviceDataRepository = deviceDataRepository;
        this.deviceRepository = deviceRepository;
        this.vmsDatex2Converter = vmsDatex2Converter;
    }

    public VmsDataDatex2Response findVariableSignData() {
        final List<Device> devices = deviceRepository.findAll();
        final List<DeviceData> data = deviceDataRepository.findLatestData();
        final Map<String, Device> deviceMap = devices.stream().collect(Collectors.toMap(Device::getId, d -> d));

        return new VmsDataDatex2Response().withD2LogicalModel(vmsDatex2Converter.convertVmsData(data, deviceMap, null));
    }

    @Transactional(readOnly = true)
    public VmsTableDatex2Response findVariableSignMetadata() {
        final List<Device> devices = deviceRepository.findAll();

        return new VmsTableDatex2Response().withD2LogicalModel(vmsDatex2Converter.convertVmsTable(devices, null));
    }
}
