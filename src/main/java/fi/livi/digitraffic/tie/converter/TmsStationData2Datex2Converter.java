package fi.livi.digitraffic.tie.converter;

import static fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2Datex2Converter.MEASUREMENT_SITE_TABLE_IDENTIFICATION;
import static fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2Datex2Converter.MEASUREMENT_SITE_TABLE_VERSION;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.BasicData;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasuredDataPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasuredValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteRecordVersionedReference;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteTableVersionedReference;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SiteMeasurements;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SiteMeasurementsIndexMeasuredValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SpeedValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficData;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficFlow;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficSpeed;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VehicleFlowValue;
import fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2Datex2Converter;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationData2Datex2Converter {
    private static final Logger log = LoggerFactory.getLogger(TmsStationData2Datex2Converter.class);

    private final Datex2Util datex2Util;

    public TmsStationData2Datex2Converter(final Datex2Util datex2Util) {
        this.datex2Util = datex2Util;
    }

    public D2LogicalModel convert(final Map<TmsStation, List<SensorValueDto>> stations, final ZonedDateTime updated) {
        final HashMap<String, Long> skippedSensorValues = new HashMap<>();

        final MeasuredDataPublication publication = datex2Util.publication(new MeasuredDataPublication(), updated)
                .withHeaderInformation(datex2Util.headerInformation())
                .withMeasurementSiteTableReference(new MeasurementSiteTableVersionedReference()
                                                       .withId(MEASUREMENT_SITE_TABLE_IDENTIFICATION)
                                                       .withVersion(MEASUREMENT_SITE_TABLE_VERSION));

        stations.forEach((station, sensorValues) ->
                             sensorValues.forEach(value -> publication.withSiteMeasurements(getSiteMeasurement(station, value, skippedSensorValues))));

        skippedSensorValues.forEach((k, v) -> log.warn("Skipping unsupported sensor while building datex2 message. sensorName={}, " +
                                                       "skipped sensor value: sensorValue={}", k, v));

        return datex2Util.logicalModel(publication);
    }

    private static SiteMeasurements getSiteMeasurement(final TmsStation station, final SensorValueDto sensorValue, final HashMap<String, Long> skipped) {
        final BasicData data = getBasicData(sensorValue);

        if (data != null) {
            return new SiteMeasurements()
                .withMeasurementSiteReference(new MeasurementSiteRecordVersionedReference()
                                                  .withId(TmsStationMetadata2Datex2Converter.getMeasurementSiteReference(station.getNaturalId(), sensorValue.getSensorNaturalId()))
                                                  .withVersion(TmsStationMetadata2Datex2Converter.MEASUREMENT_SITE_RECORD_VERSION))
                .withMeasurementTimeDefault(DateHelper.toXMLGregorianCalendar(sensorValue.getStationLatestMeasuredTime()))
                .withMeasuredValue(new SiteMeasurementsIndexMeasuredValue()
                                       .withIndex(1) // Only one measurement per sensor
                                       .withMeasuredValue(new MeasuredValue().withBasicData(data)));
        } else {
            skipped.compute(sensorValue.getSensorNameFi(), (k, v) -> v == null ? 1 : v + 1);
            return null;
        }
    }

    private static BasicData getBasicData(final SensorValueDto sensorValue) {
        final String sensorName = sensorValue.getSensorNameFi();

        if (sensorName.contains("KESKINOPEUS")) {
            final TrafficSpeed trafficSpeed =
                new TrafficSpeed().withAverageVehicleSpeed(new SpeedValue().withSpeed((float) sensorValue.getSensorValue()));
            if (setMeasurementOrCalculationPeriod(sensorName, trafficSpeed)) {
                return trafficSpeed;
            }
        } else if (sensorName.contains("OHITUKSET")) {
            final BigInteger value = BigDecimal.valueOf(sensorValue.getSensorValue()).round(MathContext.UNLIMITED).toBigInteger();
            final TrafficFlow trafficFlow = new TrafficFlow().withVehicleFlow(new VehicleFlowValue().withVehicleFlowRate(value));
            if (setMeasurementOrCalculationPeriod(sensorName, trafficFlow)) {
                return trafficFlow;
            }
        }
        return null;
    }

    private static boolean setMeasurementOrCalculationPeriod(final String sensorName, final TrafficData trafficData) {
        if (sensorName.contains("_5MIN_")) {
            trafficData.withMeasurementOrCalculationPeriod(5 * 60F);
            return true;
        } else if (sensorName.contains("_60MIN_")) {
            trafficData.withMeasurementOrCalculationPeriod(60 * 60F);
            return true;
        }
        return false;
    }
}
