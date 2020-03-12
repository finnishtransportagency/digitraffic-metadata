package fi.livi.digitraffic.tie.conf.jaxb2;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;

import fi.livi.digitraffic.tie.helper.DateHelper;

/**
 * Converts xmlType="xs:dateTime" field as Instant (Z-time).
 */
public class DateTimeConverter {

    public static Instant parseDateTime(final String from) {
        try {
            if (StringUtils.isBlank(from)) {
                return null;
            }
            return ZonedDateTime.parse(from).toInstant();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String printDateTime(final Instant from) {
        if (from == null) {
            return null;
        }
        return DateHelper.toIsoDateTimeWithMillis(from);
    }
}
