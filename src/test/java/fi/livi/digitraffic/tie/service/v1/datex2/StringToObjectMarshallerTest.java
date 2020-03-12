package fi.livi.digitraffic.tie.service.v1.datex2;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.data.service.AbstractDatex2DataServiceTest;

@Import({StringToObjectMarshaller.class})
public class StringToObjectMarshallerTest extends AbstractDatex2DataServiceTest {

    private static final Logger log = LoggerFactory.getLogger(StringToObjectMarshallerTest.class);

    @Autowired
    private StringToObjectMarshaller stringToObjectMarshaller;

    // match values tags ending with name Time
    private final static Pattern timesPattern = Pattern.compile("<[^>]*Time>(.+?)<\\/");
    // Match Zulu datetime
    private final static Pattern isoDateTimeZPattern = Pattern.compile("\\d{4}-([0]\\d|1[0-2])-([0-2]\\d|3[01])T([0-2]\\d):([0-6]\\d):([0-6]\\d)(.\\d{3})?Z");

    @Test
    public void allDatesInUtc() throws IOException {
        final String fromXml = readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-15-59-34-896.xml");
        Object object = stringToObjectMarshaller.convertToObject(fromXml);
        final String toXml = stringToObjectMarshaller.convertToString(object);
        log.info("Check source xml not having Zulu times");
        checkIsoDateFormats(fromXml, false, 13);
        log.info("Check created xml having Zulu times");
        checkIsoDateFormats(toXml, true, 13);

    }

    private void checkIsoDateFormats(final String xml, final boolean shouldMatchIsoFormat, final int timesCount) {
        // Now create matcher object to find times from the xml.
        Matcher timesMatcher = timesPattern.matcher(xml);

        int found = 0;
        while (timesMatcher.find()) {
            final String dateTime = timesMatcher.group(1); // group(0) = whole match
            log.info("Found dateTime: {}", dateTime);
            if (shouldMatchIsoFormat) {
                Assert.assertTrue("Illegal iso datetime at Zulu " + dateTime, isoDateTimeZPattern.matcher(dateTime).matches());
            } else {
                Assert.assertFalse("Should not be iso datetime Zulu: " + dateTime, isoDateTimeZPattern.matcher(dateTime).matches());
            }
            found++;
        }
        Assert.assertEquals(13, found);

    }
}