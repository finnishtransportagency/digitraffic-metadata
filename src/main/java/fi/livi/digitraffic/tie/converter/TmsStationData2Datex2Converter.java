package fi.livi.digitraffic.tie.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.datex2.BasicData;
import fi.livi.digitraffic.tie.datex2.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.datex2.CountryEnum;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Exchange;
import fi.livi.digitraffic.tie.datex2.HeaderInformation;
import fi.livi.digitraffic.tie.datex2.InformationStatusEnum;
import fi.livi.digitraffic.tie.datex2.InternationalIdentifier;
import fi.livi.digitraffic.tie.datex2.MeasuredDataPublication;
import fi.livi.digitraffic.tie.datex2.MeasuredValue;
import fi.livi.digitraffic.tie.datex2.MeasurementSiteRecordVersionedReference;
import fi.livi.digitraffic.tie.datex2.MeasurementSiteTableVersionedReference;
import fi.livi.digitraffic.tie.datex2.SiteMeasurements;
import fi.livi.digitraffic.tie.datex2.SiteMeasurementsIndexMeasuredValue;
import fi.livi.digitraffic.tie.datex2.SpeedValue;
import fi.livi.digitraffic.tie.datex2.TrafficData;
import fi.livi.digitraffic.tie.datex2.TrafficFlow;
import fi.livi.digitraffic.tie.datex2.TrafficSpeed;
import fi.livi.digitraffic.tie.datex2.VehicleFlowValue;
import fi.livi.digitraffic.tie.converter.TmsStationMetadata2Datex2Converter;
import fi.livi.digitraffic.tie.model.v1.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationData2Datex2Converter {

    private static final Logger log = LoggerFactory.getLogger(TmsStationData2Datex2Converter.class);

    private final InformationStatusEnum informationStatus;

    public TmsStationData2Datex2Converter(@Value("${dt.domain.url}") final String camUrl) {
        this.informationStatus = camUrl.toLowerCase().contains("test") ? InformationStatusEnum.TEST : InformationStatusEnum.REAL;
    }

    public D2LogicalModel convert(final Map<TmsStation, List<SensorValueDto>> stations, final ZonedDateTime updated) {

        final HashMap<String, Long> skippedSensorValues = new HashMap<>();

        final MeasuredDataPublication publication =
            new MeasuredDataPublication()
                .withPublicationTime(DateHelper.toInstant(updated))
                .withPublicationCreator(new InternationalIdentifier()
                                            .withCountry(CountryEnum.FI)
                                            .withNationalIdentifier("FI"))
                .withLang("Finnish")
                .withHeaderInformation(new HeaderInformation()
                                           .withConfidentiality(ConfidentialityValueEnum.NO_RESTRICTION)
                                           .withInformationStatus(informationStatus))
                .withMeasurementSiteTableReference(new MeasurementSiteTableVersionedReference()
                                                       .withId(TmsStationMetadata2Datex2Converter.MEASUREMENT_SITE_TABLE_IDENTIFICATION)
                                                       .withVersion(TmsStationMetadata2Datex2Converter.MEASUREMENT_SITE_TABLE_VERSION));

        stations.forEach((station, sensorValues) ->
                             sensorValues.forEach(value -> publication.withSiteMeasurements(getSiteMeasurement(station, value, skippedSensorValues))));

        skippedSensorValues.forEach((k, v) -> log.warn("Skipping unsupported sensor while building datex2 message. sensorName={}, " +
                                                       "skipped sensor value: sensorValue={}", k, v));

        return new D2LogicalModel()
            .withPayloadPublication(publication)
            .withExchange(new Exchange().withSupplierIdentification(new InternationalIdentifier().withCountry(CountryEnum.FI).withNationalIdentifier("FI")));
    }

    private static SiteMeasurements getSiteMeasurement(final TmsStation station, final SensorValueDto sensorValue, final HashMap<String, Long> skipped) {
        final BasicData data = getBasicData(sensorValue);

        if (data != null) {
            return new SiteMeasurements()
                .withMeasurementSiteReference(new MeasurementSiteRecordVersionedReference()
                                                  .withId(TmsStationMetadata2Datex2Converter.getMeasurementSiteReference(station.getNaturalId(), sensorValue.getSensorNaturalId()))
                                                  .withVersion(TmsStationMetadata2Datex2Converter.MEASUREMENT_SITE_RECORD_VERSION))
                .withMeasurementTimeDefault(DateHelper.toInstant(sensorValue.getStationLatestMeasuredTime()))
                .withMeasuredValues(new SiteMeasurementsIndexMeasuredValue()
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
