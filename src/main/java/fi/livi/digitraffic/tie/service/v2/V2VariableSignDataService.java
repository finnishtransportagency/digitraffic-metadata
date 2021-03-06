package fi.livi.digitraffic.tie.service.v2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.SignTextRow;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.DeviceDataRow;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dao.v2.V2DeviceDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2DeviceRepository;
import fi.livi.digitraffic.tie.dto.v1.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.Device;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.DeviceData;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.dao.v2.V2CodeDescriptionRepository;
import fi.livi.digitraffic.tie.dto.v1.CodeDescription;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeature;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignProperties;

@Service
public class V2VariableSignDataService {
    private final V2DeviceRepository v2DeviceRepository;
    private final V2DeviceDataRepository v2DeviceDataRepository;
    private final V2CodeDescriptionRepository v2CodeDescriptionRepository;

    public V2VariableSignDataService(final V2DeviceRepository v2DeviceRepository, final V2DeviceDataRepository v2DeviceDataRepository,
                                     final V2CodeDescriptionRepository v2CodeDescriptionRepository) {
        this.v2DeviceRepository = v2DeviceRepository;
        this.v2DeviceDataRepository = v2DeviceDataRepository;
        this.v2CodeDescriptionRepository = v2CodeDescriptionRepository;
    }

    @PerformanceMonitor(maxInfoExcecutionTime = 100000, maxWarnExcecutionTime = 3000)
    @Transactional(readOnly = true)
    public VariableSignFeatureCollection listLatestValues() {
        final Stream<Device> devices = v2DeviceRepository.streamAll();
        final List<Long> dataIds = v2DeviceDataRepository.findLatestData();
        final List<DeviceData> data = v2DeviceDataRepository.findDistinctByIdIn(dataIds);

        final Map<String, DeviceData> dataMap = data.stream().collect(Collectors.toMap(DeviceData::getDeviceId, d -> d));
        final List<VariableSignFeature> features = devices
            .map(d -> convert(d, dataMap))
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());

        return new VariableSignFeatureCollection(features);
    }

    private VariableSignFeature convert(final Device device, final Map<String, DeviceData> dataMap) {
        final DeviceData data = dataMap.get(device.getId());

        return data == null ? null : convert(device, data);
    }

    private VariableSignFeature convert(final Device device, final DeviceData data) {
        final List<SignTextRow> textRows = CollectionUtils.isEmpty(data.getRows()) ? Collections.emptyList() : convert(data.getRows());

        final VariableSignProperties properties = new VariableSignProperties(
            device.getId(),
            VariableSignProperties.SignType.byValue(device.getType()),
            device.getRoadAddress(),
            VariableSignProperties.Direction.byValue(device.getDirection()),
            VariableSignProperties.Carriageway.byValue(device.getCarriageway()),
            data == null ? null : data.getDisplayValue(),
            data == null ? null : data.getAdditionalInformation(),
            data == null ? null : data.getEffectDate(),
            data == null ? null : data.getCause(),
            data == null ? null : VariableSignProperties.Reliability.byValue(data.getReliability()), textRows);
        final Point point = CoordinateConverter.convertFromETRS89ToWGS84(new Point(device.getEtrsTm35FinX(), device.getEtrsTm35FinY()));

        return new VariableSignFeature(point, properties);
    }

    private List<SignTextRow> convert(final List<DeviceDataRow> textRows) {
        return textRows.stream()
            .map(r -> new SignTextRow(r.getScreen(), r.getRowNumber(), r.getText()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrafficSignHistory> listVariableSignHistory(final String deviceId) {
        return v2DeviceDataRepository.getDeviceDataByDeviceIdOrderByEffectDateDesc(deviceId);
    }

    @Transactional(readOnly = true)
    public VariableSignFeatureCollection listLatestValue(final String deviceId) {
        final Optional<Device> device = v2DeviceRepository.findById(deviceId);

        if(device.isPresent()) {
            final List<Long> latest = v2DeviceDataRepository.findLatestData(deviceId);
            final List<DeviceData> data = v2DeviceDataRepository.findDistinctByIdIn(latest);

            return new VariableSignFeatureCollection(data.stream()
                .map(d -> convert(device.get(), d))
                .collect(Collectors.toList()));
        }

        throw new ObjectNotFoundException(Device.class, deviceId);
    }

    @Transactional(readOnly = true)
    public List<CodeDescription> listVariableSignTypes() {
        return v2CodeDescriptionRepository.listAllVariableSignTypes();
    }
}
