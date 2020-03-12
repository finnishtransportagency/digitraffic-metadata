package fi.livi.digitraffic.tie.service.v1.datex2;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertEmpty;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertTimesEqual;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.helper.FileGetService;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2HelperService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({Datex2UpdateService.class, Datex2RoadworksHttpClient.class, FileGetService.class, RestTemplate.class, RetryTemplate.class})
public class Datex2RoadworksIntegrationTest extends AbstractServiceTest {
    private Datex2SimpleMessageUpdater messageUpdater;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Datex2UpdateService datex2UpdateService;

    @Autowired
    private StringToObjectMarshaller stringToObjectMarshaller;

    @Autowired
    private V2Datex2HelperService v2Datex2HelperService;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @MockBean
    private Datex2RoadworksHttpClient datex2RoadworksHttpClient;

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
        .append(ISO_LOCAL_DATE_TIME)
        .optionalStart()
        .appendOffsetId()
        .optionalStart()
        .toFormatter();

    @Before
    public void before() {
        messageUpdater = new Datex2SimpleMessageUpdater(null, datex2RoadworksHttpClient, null,
                                                        datex2UpdateService, null, stringToObjectMarshaller, v2Datex2UpdateService, v2Datex2HelperService);
        datex2Repository.deleteAll();
    }

    private void assertCountAndVersionTime(final int count, final ZonedDateTime versionTime) {
        final List<Datex2> roadWorks = datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name(), 0);

        assertCollectionSize(count, roadWorks);
        assertVersionTime(roadWorks.get(0), versionTime);
    }

    private void assertVersionTime(final Datex2 datex2, final ZonedDateTime versionTime) {
        assertTimesEqual(versionTime.withNano(0), datex2.getSituations().get(0).getSituationRecords().get(0).getVersionTime());
    }

    private String getRoadworks(final ZonedDateTime versionTime) throws IOException {
        final String xml = readResourceContent("classpath:roadworks/roadworks_GUID50013753.xml");
        final ZonedDateTime endTime = ZonedDateTime.now().plusDays(1);

        return xml
            .replace("%ENDTIME%", endTime.format(FORMATTER))
            .replace("%VERSIONTIME%", versionTime.format(FORMATTER));
    }

    @Test
    @Ignore
    public void updateMessagesWithRealData() {
        //assertEmpty(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name()));

        messageUpdater.updateDatex2RoadworksMessages();

        Assert.assertTrue(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name(), 0).size() > 1);
    }

    @Test
    @Rollback
    public void updateWithChangedTimestamp() throws IOException, InterruptedException {
        assertEmpty(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name(), 0));

        ZonedDateTime versionTime = ZonedDateTime.now();
        when(datex2RoadworksHttpClient.getRoadWorksMessage()).thenReturn(getRoadworks(versionTime));
        messageUpdater.updateDatex2RoadworksMessages();
        assertCountAndVersionTime(1, versionTime);

        Thread.sleep(1000);

        // this one has newer situationRecordVersion time on one of the situations

        versionTime = versionTime.plusHours(1);
        when(datex2RoadworksHttpClient.getRoadWorksMessage()).thenReturn(getRoadworks(versionTime));
        messageUpdater.updateDatex2RoadworksMessages();
        assertCountAndVersionTime(1, versionTime);
    }
}
