package fi.livi.digitraffic.tie.service.v1.datex2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Timestamps in filename are in Finnish localtime, either UTC+2 or UTC+3.
 * This class tries to figure that out.
 */
public final class TimeFromFilenameParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("'Datex2_'yyyy-MM-dd-HH-mm-ss-SSS'.xml'");

    private TimeFromFilenameParser() {}

    public static Instant parseDate(final String filename) {
        final LocalDateTime localTime = LocalDateTime.parse(filename, DATE_FORMAT);
        final ZoneOffset zoneId = calculateZoneId(localTime);

        return localTime.toInstant(zoneId);
    }

    public static ZoneOffset calculateZoneId(final LocalDateTime date) {
        final ZonedDateTime finlandTime = ZonedDateTime.of(date, ZoneId.of("EET"));

        return finlandTime.getOffset();
    }
}
