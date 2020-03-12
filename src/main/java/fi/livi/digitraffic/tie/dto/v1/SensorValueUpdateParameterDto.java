package fi.livi.digitraffic.tie.dto.v1;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.service.v1.LotjuAnturiWrapper;
import fi.livi.digitraffic.tie.helper.NumberConverter;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.model.RoadStationType;

public class SensorValueUpdateParameterDto {

    private final BigDecimal value;
    private final OffsetDateTime measured;
    private final long roadStationId;
    private final long sensorLotjuId;
    private final String stationType;
    private final OffsetDateTime timeWindowStart;
    private final OffsetDateTime timeWindowEnd;

    public SensorValueUpdateParameterDto(final LotjuAnturiWrapper<LAMRealtimeProtos.Lam.Anturi> wrapper, final Long roadStationId, final TimestampCache timestampCache) {
        final LAMRealtimeProtos.Lam.Anturi anturi = wrapper.getAnturi();
        this.value = BigDecimal.valueOf(anturi.getArvo());
        this.measured = timestampCache.get(wrapper.getAika());
        this.roadStationId = roadStationId;
        this.sensorLotjuId = anturi.getLaskennallinenAnturiId();
        this.stationType = RoadStationType.TMS_STATION.name();
        this.timeWindowStart = anturi.hasAikaikkunaAlku() ? timestampCache.get(anturi.getAikaikkunaAlku()) : null;
        this.timeWindowEnd = anturi.hasAikaikkunaLoppu() ? timestampCache.get(anturi.getAikaikkunaLoppu()) : null;
    }

    public SensorValueUpdateParameterDto(final LotjuAnturiWrapper<TiesaaProtos.TiesaaMittatieto.Anturi> wrapper, final TimestampCache timestampCache, final Long roadStationId) {
        final TiesaaProtos.TiesaaMittatieto.Anturi anturi = wrapper.getAnturi();
        this.value = NumberConverter.convertAnturiValueToBigDecimal(anturi.getArvo());
        this.measured = timestampCache.get(wrapper.getAika());
        this.roadStationId = roadStationId;
        this.sensorLotjuId = anturi.getLaskennallinenAnturiId();
        this.stationType = RoadStationType.WEATHER_STATION.name();
        this.timeWindowStart = null;
        this.timeWindowEnd = null;
    }

    public BigDecimal getValue() {
        return value;
    }

    public OffsetDateTime getMeasured() {
        return measured;
    }

    public long getRoadStationId() {
        return roadStationId;
    }

    public long getSensorLotjuId() {
        return sensorLotjuId;
    }

    public String getStationType() {
        return stationType;
    }

    public OffsetDateTime getTimeWindowStart() {
        return timeWindowStart;
    }

    public OffsetDateTime getTimeWindowEnd() {
        return timeWindowEnd;
    }
}
