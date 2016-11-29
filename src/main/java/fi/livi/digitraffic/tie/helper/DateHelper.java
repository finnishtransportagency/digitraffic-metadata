package fi.livi.digitraffic.tie.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

public final class DateHelper {
    private DateHelper() {}

    public static ZonedDateTime getNewest(final ZonedDateTime first, final ZonedDateTime second) {
        if (first == null) {
            return second;
        } else if(second == null || first.isAfter(second)) {
            return first;
        }

        return second;
    }

    public static LocalDateTime toLocalDateTime(XMLGregorianCalendar calendar) {
        if (calendar != null) {
            ZonedDateTime zonedDateTime = calendar.toGregorianCalendar().toZonedDateTime();
            return ZonedDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTime(XMLGregorianCalendar calendar) {
        if (calendar != null) {
            // This way Time is formed as 1995-01-01T00:00+02:00[Europe/Helsinki]
            //                 and not as 1995-01-01T00:00+02:00[GMT+02:00]
            // HashCodeBuilder handles them differently
            return ZonedDateTime.of(toLocalDateTime(calendar), ZoneId.systemDefault());
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return localDateTime.atZone(ZoneId.systemDefault());
        }
        return null;
    }

    public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            return zonedDateTime.toLocalDateTime();
        }
        return null;
    }

    public static Date toDate(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            return Date.from(zonedDateTime.toInstant());
        }
        return null;
    }
}
