package fi.livi.digitraffic.tie.dao.v2;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.DeviceData;

@Repository
public interface V2DeviceDataRepository extends JpaRepository<DeviceData, Long> {
    @Query(value =
        "select id, created_date, device_id, display_value, additional_information, effect_date, cause, reliability\n" +
        "from device_data where id in(\n" +
        "select first_value(id) over (partition by device_id order by effect_date desc) from device_data\n" +
        ")",
        nativeQuery = true)
    Stream<DeviceData> streamLatestData();

    @Query(value =
        "select id, created_date, device_id, display_value, additional_information, effect_date, cause, reliability\n" +
            "from device_data where id in(\n" +
            "select first_value(id) over (order by effect_date desc) from device_data where device_id = :deviceId\n" +
            ")",
        nativeQuery = true)
    List<DeviceData> findLatestData(final String deviceId);

    List<TrafficSignHistory> getDeviceDataByDeviceIdOrderByEffectDateDesc(final String deviceId);
}
