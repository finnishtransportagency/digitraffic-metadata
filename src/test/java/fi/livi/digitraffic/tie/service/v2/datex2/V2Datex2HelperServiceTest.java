package fi.livi.digitraffic.tie.service.v2.datex2;

import static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncement.Language.FI;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.datex2.Accident;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.GenericPublication;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.AlertCLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Contact;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.EstimatedDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.JsonMessage;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Location;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.LocationDetails;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.LocationToDisplay;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadAddress;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadAddressLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadPoint;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TimeAndDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncement;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

@Import({V2Datex2HelperService.class, JacksonAutoConfiguration.class })
public class V2Datex2HelperServiceTest extends AbstractServiceTest {

    @Autowired
    private V2Datex2HelperService v2Datex2HelperService;

    final static ZonedDateTime DATE_TIME = ZonedDateTime.parse("2020-01-02T14:43:18.388Z");

    final static Instant TIME_NOW = Instant.now().with(ChronoField.MILLI_OF_SECOND, 123);
    final static ZonedDateTime TIME_NOW_ZONED = TIME_NOW.atZone(ZoneOffset.UTC);
    final static Instant TIME_MILLIS_IN_FUTURE = TIME_NOW.with(ChronoField.MILLI_OF_SECOND, 321);
    final static Instant TIME_SECONDS_IN_FUTURE = Instant.ofEpochMilli(TIME_NOW.toEpochMilli()+1000); // +1s
    final static Instant TIME_SECONDS_IN_PAST = Instant.ofEpochMilli(TIME_NOW.toEpochMilli()-1000); // +1s

    @Test
    public void convertToJsonStringAndToJsonObject() {
        final ImsGeoJsonFeature jsonMessage = createJsonMessage();
        final String jsonText = v2Datex2HelperService.convertToJsonString(jsonMessage);
        final ImsGeoJsonFeature jsonMessageConverted = v2Datex2HelperService.convertToJsonObject(jsonText);
        final String jsonTextConverted = v2Datex2HelperService.convertToJsonString(jsonMessageConverted);
        Assert.assertTrue(jsonText.contains("GUID123456"));
        Assert.assertEquals(jsonText, jsonTextConverted);
    }

    @Test
    public void isUpdatedRecord() {

        // Millis don't matter
        final SituationRecord millisDiff = new Accident().withSituationRecordVersionTime(TIME_MILLIS_IN_FUTURE);
        Assert.assertFalse(V2Datex2HelperService.isUpdatedRecord(TIME_NOW, millisDiff));

        // Second in future -> is updated
        final SituationRecord secondsDiff = new Accident().withSituationRecordVersionTime(TIME_SECONDS_IN_FUTURE);
        Assert.assertTrue(V2Datex2HelperService.isUpdatedRecord(TIME_NOW, secondsDiff));

        // Second in past -> not updated
        final SituationRecord secondsPast = new Accident().withSituationRecordVersionTime(TIME_SECONDS_IN_PAST);
        Assert.assertFalse(V2Datex2HelperService.isUpdatedRecord(TIME_NOW, secondsPast));
    }

    @Test
    public void isNewOrUpdatedSituation() {
        final Situation sNow = creatSituationWithRecordsVersionTimes(TIME_MILLIS_IN_FUTURE);
        Assert.assertFalse(V2Datex2HelperService.isNewOrUpdatedSituation(TIME_NOW, sNow));
        Assert.assertFalse(V2Datex2HelperService.isNewOrUpdatedSituation(TIME_NOW_ZONED, sNow));

        final Situation sFuture = creatSituationWithRecordsVersionTimes(TIME_SECONDS_IN_FUTURE);
        Assert.assertTrue(V2Datex2HelperService.isNewOrUpdatedSituation(TIME_NOW, sFuture));
        Assert.assertTrue(V2Datex2HelperService.isNewOrUpdatedSituation(TIME_NOW_ZONED, sFuture));

        final Situation sPast = creatSituationWithRecordsVersionTimes(TIME_SECONDS_IN_PAST);
        Assert.assertFalse(V2Datex2HelperService.isNewOrUpdatedSituation(TIME_NOW, sPast));
        Assert.assertFalse(V2Datex2HelperService.isNewOrUpdatedSituation(TIME_NOW_ZONED, sPast));
    }

    @Test
    public void getSituationPublication() {
        final SituationPublication sp = new SituationPublication();
        final D2LogicalModel d2 = new D2LogicalModel().withPayloadPublication(sp);
        final SituationPublication spResult = V2Datex2HelperService.getSituationPublication(d2);
        Assert.assertTrue(sp == spResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSituationPublicationUnsuportedExeption() {
        final D2LogicalModel d2 = new D2LogicalModel().withPayloadPublication(new GenericPublication());
        V2Datex2HelperService.getSituationPublication(d2);
        Assert.assertTrue("Should not go here", false);
    }

    @Test
    public void checkD2HasOnlyOneSituation() {
        final D2LogicalModel d2 = createD2LogicalModelWithSituationPublications(new Situation());
        V2Datex2HelperService.checkD2HasOnlyOneSituation(d2); // no exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkD2HasOnlyOneSituationFails() {
        final D2LogicalModel d2 = createD2LogicalModelWithSituationPublications(new Situation(), new Situation());
        V2Datex2HelperService.checkD2HasOnlyOneSituation(d2); // no exception
    }

    private static Situation creatSituationWithRecordsVersionTimes(Instant...versionTimes) {
        final List<SituationRecord> records = Arrays.stream(versionTimes).map(t -> createSituationRecord(t)).collect(Collectors.toList());
        return new Situation().withSituationRecords(records);
    }

    private static SituationRecord createSituationRecord(final Instant versionTime) {
        return new Accident().withSituationRecordVersionTime(versionTime);
    }

    private static D2LogicalModel createD2LogicalModelWithSituationPublications(Situation...situations) {
        final SituationPublication sp = new SituationPublication();
        sp.withSituations(situations);
        return new D2LogicalModel().withPayloadPublication(sp);
    }

    private ImsGeoJsonFeature createJsonMessage() {
        final JsonMessage properties = new JsonMessage()
            .withVersion(1)
            .withSituationId("GUID123456")
            .withReleaseTime(DATE_TIME)
            .withAnnouncements(Collections.singletonList(
                new TrafficAnnouncement()
                    .withLanguage(FI)
                    .withTitle("Title")
                    .withLocation(createLocation())
                    .withLocationDetails(createLocationDetails())
                    .withFeatures(Collections.singletonList("Huono ajokeli"))
                    .withComment("TEST")
                    .withTimeAndDuration(createTimeAndDuration())
                    .withAdditionalInformation("Liikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/")
                    .withSender("Tieliikennekeskus Helsinki")
            ))
            .withLocationToDisplay(new LocationToDisplay(1.0, 2.0))
            .withContact(new Contact("123456789", "987654321", "helsinki@liikennekeskus.fi"));
        return new ImsGeoJsonFeature()
            .withType(ImsGeoJsonFeature.Type.FEATURE)
            .withProperties(properties)
            .withGeometry(new Point(23.77474, 61.50221));
    }

    private TimeAndDuration createTimeAndDuration() {
        return new TimeAndDuration(DATE_TIME, DATE_TIME.plusHours(2), new EstimatedDuration().withInformal("Yli 6 tuntia").withMaximum("PT8H").withMinimum("PT6H"));
    }

    private LocationDetails createLocationDetails() {
        return new LocationDetails()
            .withRoadAddressLocation(
                new RoadAddressLocation(
                    createRoadPoint(1), createRoadPoint(2), RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
                );
    }

    private RoadPoint createRoadPoint(final int id) {
        return new RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new RoadAddress(130, 24, 4000),
            "Tie 123", new AlertCLocation(37128, "Marjamäki", 2000));
    }

    private Location createLocation() {
        return new Location(358, 10, "1.1.1", "Location description");
    }


}
