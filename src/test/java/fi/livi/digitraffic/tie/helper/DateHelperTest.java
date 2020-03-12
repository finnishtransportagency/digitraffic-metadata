package fi.livi.digitraffic.tie.helper;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import fi.livi.digitraffic.tie.AbstractSpringJUnitTest;

@RunWith(JUnit4.class)
public class DateHelperTest extends AbstractSpringJUnitTest {

    private static final String DATE_STRING_OFFSET_2 = "2016-01-22T10:00:01+02:00";
    private static final String DATE_STRING_Z = "2016-01-22T08:00:01Z";
    private static final String DATE_STRING_MILLIS_OFFSET_2 = "2016-01-22T10:00:01.500+02:00";
    private static final String DATE_STRING_MILLIS_Z = "2016-01-22T08:00:01.500Z";
    private static final String XML_DATE_STRING_Z = "2016-01-22T08:00:01.000Z";

    @Test
    public void getNewest() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime older = now.minusNanos(1);
        final ZonedDateTime newest = DateHelper.getNewest(now, older);
        Assert.assertEquals(now, newest);
    }

    @Test
    public void xmlGregorianCalendarToZonedDateTimetoAtUtc() throws DatatypeConfigurationException {
        final String DATE_STRING_WINTER = "2016-01-22T10:00:00+02:00";
        final String DATE_STRING_WINTER_Z = "2016-01-22T08:00:00Z";
        final String DATE_STRING_SUMMER = "2016-06-22T10:10:01.102+03:00";
        final String DATE_STRING_SUMMER_Z = "2016-06-22T07:10:01.102Z";

        final GregorianCalendar wc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_WINTER)));
        final ZonedDateTime winterTime = DateHelper.toZonedDateTimeAtUtc(DatatypeFactory.newInstance().newXMLGregorianCalendar(wc));
        Assert.assertEquals(DATE_STRING_WINTER_Z, ISO_OFFSET_DATE_TIME.format(winterTime));

        final GregorianCalendar sc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_SUMMER)));
        final ZonedDateTime summerTime = DateHelper.toZonedDateTimeAtUtc(DatatypeFactory.newInstance().newXMLGregorianCalendar(sc));
        Assert.assertEquals(DATE_STRING_SUMMER_Z, ISO_OFFSET_DATE_TIME.format(summerTime));
    }

    @Test
    public void xmlGregorianCalendarToInstant() throws DatatypeConfigurationException {
        final GregorianCalendar wc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_OFFSET_2)));
        final Instant instant = DateHelper.toInstant(DatatypeFactory.newInstance().newXMLGregorianCalendar(wc));
        Assert.assertEquals(DATE_STRING_Z, instant.toString());
    }

    @Test
    public void zonedDateTimeToInstant() throws DatatypeConfigurationException {
        final ZonedDateTime zdt = ZonedDateTime.of(2019, 12, 1, 10, 15, 20, 500, ZoneOffset.UTC);
        final String DATE_STRING_NANOS = "2019-12-01T10:15:20.000000500Z";
        final Instant instant = DateHelper.toInstant(zdt);
        Assert.assertEquals(DATE_STRING_NANOS, instant.toString());
    }

    @Test
    public void xmlGregorianCalendarToZonedDateTimeWithoutMillis() throws DatatypeConfigurationException {
        final GregorianCalendar gc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_OFFSET_2).plusNanos(500000000)));
        final XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        final Instant instant = DateHelper.toInstant(xmlDate);
        Assert.assertEquals(DATE_STRING_MILLIS_Z, instant.toString());

        final ZonedDateTime zdtWithOutMillis = DateHelper.toZonedDateTimeWithoutMillis(xmlDate);
        Assert.assertEquals(DATE_STRING_Z, zdtWithOutMillis.toString());
    }

    @Test
    public void zdtToZonedDateTimeAtUtc() {
        final ZonedDateTime timeAtOffset2 = ZonedDateTime.parse(DATE_STRING_OFFSET_2);
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(timeAtOffset2);

        Assert.assertEquals(0, utc.getOffset().getTotalSeconds());
        Assert.assertEquals(timeAtOffset2.toEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void instantToZonedDateTimeAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(instant);
        Assert.assertEquals(0, utc.getOffset().getTotalSeconds());
        Assert.assertEquals(instant.getEpochSecond() , utc.toEpochSecond());
    }


    @Test
    public void epochMillisToZonedDateTimeAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(instant.toEpochMilli());
        Assert.assertEquals(0, utc.getOffset().getTotalSeconds());
        Assert.assertEquals(instant.getEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void dateToZonedDateTimeAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final java.util.Date date = Date.from(instant);
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(date);
        Assert.assertEquals(0, utc.getOffset().getTotalSeconds());
        Assert.assertEquals(instant.getEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void zdtToXMLGregorianCalendarAtUtc() {
        final ZonedDateTime timeAtOffset2 = ZonedDateTime.parse(DATE_STRING_OFFSET_2);
        final XMLGregorianCalendar xmlUtc = DateHelper.toXMLGregorianCalendarAtUtc(timeAtOffset2);
        Assert.assertEquals(XML_DATE_STRING_Z, xmlUtc.toString());
        ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(xmlUtc);
        Assert.assertEquals(timeAtOffset2.toEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void instantToXMLGregorianCalendarAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final XMLGregorianCalendar xmlUtc = DateHelper.toXMLGregorianCalendarAtUtc(instant);
        Assert.assertEquals(XML_DATE_STRING_Z, xmlUtc.toString());
        ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(xmlUtc);
        Assert.assertEquals(instant.getEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void zonedDateTimeNowAtUtc() {
        final long now = ZonedDateTime.now().toInstant().getEpochSecond();
        final ZonedDateTime utc = DateHelper.getZonedDateTimeNowAtUtc();

        Assert.assertEquals(0, utc.getOffset().getTotalSeconds());
        Assert.assertEquals(now , utc.toEpochSecond(), 1.0);
    }
}
