package fi.livi.digitraffic.tie.service.v1.datex2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;

public class TimeFromFilenameParserTest {
    private void test(final LocalDateTime fileTime, final ZonedDateTime expected) {
        final String filename = String.format("Datex2_%04d-%02d-%02d-%02d-%02d-%02d-000.xml", fileTime.getYear(), fileTime.getMonthValue(),
            fileTime.getDayOfMonth(), fileTime.getHour(), fileTime.getMinute(), fileTime.getSecond());

        final Instant i = TimeFromFilenameParser.parseDate(filename);

        Assert.assertEquals(i.toEpochMilli(), expected.toInstant().toEpochMilli());
    }

    @Test
    public void parseTimeSpringWinterTime() {
        final LocalDateTime time = LocalDateTime.of(2018, 3, 25, 2, 1, 10);
        test(time, ZonedDateTime.of(time, ZoneId.of("UTC+2")));
    }

    @Test
    public void parseTimeSpringWinterTimeGap() {
        final LocalDateTime time = LocalDateTime.of(2018, 3, 25, 3, 1, 10);
        test(time, ZonedDateTime.of(time, ZoneId.of("UTC+3")));
    }

    @Test
    public void parseTimeSpringSummerTime() {
        final LocalDateTime time = LocalDateTime.of(2018, 3, 25, 4, 1, 10);
        test(time, ZonedDateTime.of(time, ZoneId.of("UTC+3")));
    }

    @Test
    public void parseTimeAutumnSummerTime() {
        final LocalDateTime time = LocalDateTime.of(2018, 10, 28, 2, 1, 10);
        test(time, ZonedDateTime.of(time, ZoneId.of("UTC+3")));
    }

    @Test
    public void parseTimeAutumnWinterTime() {
        final LocalDateTime time = LocalDateTime.of(2018, 10, 28, 4, 1, 10);
        test(time, ZonedDateTime.of(time, ZoneId.of("UTC+2")));
    }

    @Test
    public void parseTimeAutumnWinterTimeGap() {
        final LocalDateTime time = LocalDateTime.of(2018, 10, 28, 3, 1, 10);
        test(time, ZonedDateTime.of(time, ZoneId.of("UTC+3")));
    }
}
