package fi.livi.digitraffic.tie.converter;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.feature.AbstractMetadataToFeatureConverter;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.datex2.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.datex2.CountryEnum;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Exchange;
import fi.livi.digitraffic.tie.datex2.HeaderInformation;
import fi.livi.digitraffic.tie.datex2.InformationStatusEnum;
import fi.livi.digitraffic.tie.datex2.InternationalIdentifier;
import fi.livi.digitraffic.tie.datex2.MeasurementSiteRecord;
import fi.livi.digitraffic.tie.datex2.MeasurementSiteRecordIndexMeasurementSpecificCharacteristics;
import fi.livi.digitraffic.tie.datex2.MeasurementSiteTable;
import fi.livi.digitraffic.tie.datex2.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.datex2.MeasurementSpecificCharacteristics;
import fi.livi.digitraffic.tie.datex2.MultilingualString;
import fi.livi.digitraffic.tie.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.datex2.Point;
import fi.livi.digitraffic.tie.datex2.PointByCoordinates;
import fi.livi.digitraffic.tie.datex2.PointCoordinates;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationMetadata2Datex2Converter {
    public static final String MEASUREMENT_SITE_TABLE_IDENTIFICATION = "DigitrafficFI";
    public static final String MEASUREMENT_SITE_TABLE_VERSION = "1";
    public static final String MEASUREMENT_SITE_RECORD_VERSION = "1";

    private final InformationStatusEnum informationStatus;

    public TmsStationMetadata2Datex2Converter(@Value("${dt.domain.url}") final String appUrl) {
        this.informationStatus = appUrl.toLowerCase().contains("test") ? InformationStatusEnum.TEST : InformationStatusEnum.REAL;
    }

    public D2LogicalModel convert(final List<TmsStation> stations, final ZonedDateTime metadataLastUpdated) {
        final MeasurementSiteTablePublication measurementSiteTablePublication =
            new MeasurementSiteTablePublication()
                .withPublicationTime(DateHelper.toInstant(metadataLastUpdated))
                .withPublicationCreator(new InternationalIdentifier()
                                            .withCountry(CountryEnum.FI)
                                            .withNationalIdentifier("FI"))
                .withLang("Finnish")
                .withHeaderInformation(new HeaderInformation()
                                           .withConfidentiality(ConfidentialityValueEnum.NO_RESTRICTION)
                                           .withInformationStatus(informationStatus));

        final D2LogicalModel model = new D2LogicalModel()
            .withExchange(new Exchange().withSupplierIdentification(new InternationalIdentifier().withCountry(CountryEnum.FI).withNationalIdentifier("FI")))
            .withPayloadPublication(measurementSiteTablePublication);

        final MeasurementSiteTable siteTable =
            new MeasurementSiteTable()
                .withId(MEASUREMENT_SITE_TABLE_IDENTIFICATION)
                .withMeasurementSiteTableIdentification(MEASUREMENT_SITE_TABLE_IDENTIFICATION)
                .withVersion(MEASUREMENT_SITE_TABLE_VERSION);

        stations.forEach(station -> station.getRoadStation().getRoadStationSensors().stream().sorted(Comparator.comparingLong(RoadStationSensor::getNaturalId))
        .forEach(sensor -> siteTable.getMeasurementSiteRecords().add(getMeasurementSiteRecord(station, sensor))));

        measurementSiteTablePublication.getMeasurementSiteTables().add(siteTable);

        return model;
    }

    private static MeasurementSiteRecord getMeasurementSiteRecord(final TmsStation station, final RoadStationSensor sensor) {
        final fi.livi.digitraffic.tie.metadata.geojson.Point point =
            AbstractMetadataToFeatureConverter.getETRS89CoordinatesPoint(station.getRoadStation());

        final MeasurementSiteRecord measurementSiteRecord =
            new MeasurementSiteRecord()
                .withId(getMeasurementSiteReference(station.getNaturalId(), sensor.getNaturalId()))
                .withMeasurementSiteIdentification(getMeasurementSiteReference(station.getNaturalId(), sensor.getNaturalId()))
                .withVersion(MEASUREMENT_SITE_RECORD_VERSION)
                .withMeasurementSiteName(getName(sensor))
                .withMeasurementSiteLocation(
                    new Point().withPointByCoordinates(
                        new PointByCoordinates().withPointCoordinates(
                            new PointCoordinates()
                                .withLongitude(point != null && point.getLongitude() != null ? point.getLongitude().floatValue() : 0)
                                .withLatitude(point != null && point.getLatitude() != null ? point.getLatitude().floatValue() : 0))));

        if (sensor.getAccuracy() != null) {
            measurementSiteRecord.withMeasurementSpecificCharacteristics(
                new MeasurementSiteRecordIndexMeasurementSpecificCharacteristics()
                    .withIndex(1)
                    .withMeasurementSpecificCharacteristics(
                        new MeasurementSpecificCharacteristics()
                            .withAccuracy(sensor.getAccuracy() != null ? sensor.getAccuracy().floatValue() : 0)));
        }
        return measurementSiteRecord;
    }

    public static String getMeasurementSiteReference(final Long stationNaturalId, final Long sensorNaturalId) {
        return String.format("%d-%d", stationNaturalId, sensorNaturalId);
    }

    private static MultilingualString getName(final RoadStationSensor sensor) {
        return new MultilingualString().withValues(new MultilingualString.Values()
                                                       .withValues(new MultilingualStringValue()
                                                                      .withLang("fi")
                                                                      .withValue(sensor.getNameFi())));
    }
}
