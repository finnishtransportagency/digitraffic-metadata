package fi.livi.digitraffic.tie.service.v1.datex2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.conf.RestTemplateConfiguration;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.response.TrafficDisordersDatex2Response;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = "datex2.traffic.alerts.url=Datex2Url")
@Import({ Datex2SimpleMessageUpdater.class, Datex2WeightRestrictionsHttpClient.class, Datex2RoadworksHttpClient.class, RestTemplateConfiguration.class,
          Datex2DataService.class })
public class Datex2MessageUpdaterTest extends AbstractServiceTest {

    private static final String DISORDER1_GUID = "GUID50365428";
    private static final String DISORDER2_GUID = "GUID50365429";
    private static final String DISORDER2_END_PLACEHOLDER = "DISORDER2_END_PLACEHOLDER";

    @Autowired
    private Datex2SimpleMessageUpdater datex2SimpleMessageUpdater;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Datex2DataService datex2DataService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${datex2.traffic.alerts.url}")
    private String datex2Url;

    @Before
    public void before() {
        datex2Repository.deleteAll();

        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateDatex2MessagesSucceeds() throws IOException {
        server.expect(MockRestRequestMatchers.requestTo("/" + datex2Url + "?F=0&C=N&O=D"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/datex2FileList1.html"), MediaType.TEXT_HTML));

        server.expect(MockRestRequestMatchers.requestTo("/" + datex2Url + "Datex2_2017-08-10-15-59-34-896.xml"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-15-59-34-896.xml"), MediaType.APPLICATION_XML));

        datex2SimpleMessageUpdater.updateDatex2TrafficAlertMessages();
        server.verify();

        List<Datex2> datex2s = datex2Repository.findAll();
        assertEquals(1, datex2s.size());
        assertEquals(1, datex2s.get(0).getSituations().size());
        assertEquals("GUID50013339", datex2s.get(0).getSituations().get(0).getSituationId());
        assertNull(datex2s.get(0).getSituations().get(0).getSituationRecords().get(0).getOverallEndTime());


        server = MockRestServiceServer.createServer(restTemplate);

        server.expect(MockRestRequestMatchers.requestTo("/" + datex2Url + "?F=0&C=N&O=D"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/datex2FileList2.html"), MediaType.TEXT_HTML));

        server.expect(MockRestRequestMatchers.requestTo("/" + datex2Url + "Datex2_2017-08-10-16-08-32-976.xml"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-16-08-32-976.xml"), MediaType.APPLICATION_XML));

        server.expect(MockRestRequestMatchers.requestTo("/" + datex2Url + "Datex2_2017-08-10-16-10-01-680.xml"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-16-10-01-680.xml"), MediaType.APPLICATION_XML));

        datex2SimpleMessageUpdater.updateDatex2TrafficAlertMessages();
        server.verify();

        datex2s = datex2Repository.findAll(Sort.by(Sort.Direction.ASC, "importTime"));
        assertEquals(3, datex2s.size());
        assertEquals("GUID50013339", datex2s.get(0).getSituations().get(0).getSituationId());
        assertEquals("GUID50013340", datex2s.get(1).getSituations().get(0).getSituationId());
        assertEquals("GUID50013339", datex2s.get(2).getSituations().get(0).getSituationId());
        assertEquals(ZonedDateTime.parse("2017-08-10T16:09:26.231+03:00").withNano(0).toInstant(), datex2s.get(2).getSituations().get(0).getSituationRecords().get(0).getOverallEndTime().toInstant());
    }

    @Test
    public void combinedTrafficAlerts() throws IOException {
        server.expect(MockRestRequestMatchers.requestTo("/" + datex2Url + "?F=0&C=N&O=D"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/trafficAlertDatex2FileList12.html"), MediaType.TEXT_HTML));

        final String taDatex2 = readResourceContent("classpath:lotju/datex2/Datex2_2019-11-26-14-35-08-487.xml")
            .replace(DISORDER2_END_PLACEHOLDER, DateHelper.toXMLGregorianCalendarAtUtc(ZonedDateTime.now().minusHours(2).minusMinutes(10)).toString());
        server.expect(MockRestRequestMatchers.requestTo("/" + datex2Url + "Datex2_2019-11-26-14-35-08-487.xml"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(taDatex2, MediaType.APPLICATION_XML));

        datex2SimpleMessageUpdater.updateDatex2TrafficAlertMessages();
        server.verify();

        findActiveTrafficAlertsAndAssert(DISORDER1_GUID, true, 0);
        // Disorder 5 is ended > 2h since. With parameter value > 3 it should found, but not with < 3
        findActiveTrafficAlertsAndAssert(DISORDER2_GUID, false, 0);
        findActiveTrafficAlertsAndAssert(DISORDER2_GUID, false, 2);
        findActiveTrafficAlertsAndAssert(DISORDER2_GUID, true, 3);
    }

    private void findActiveTrafficAlertsAndAssert(final String situationId, final boolean found, final int inactiveHours) {
        final TrafficDisordersDatex2Response allActive = datex2DataService.findActiveTrafficDisorders(inactiveHours);
        Assert.assertEquals(found,
            allActive.getDisorders().stream()
                .filter(d ->
                    ((SituationPublication) d.getD2LogicalModel().getPayloadPublication()).getSituations().stream().filter(s -> s.getId().equals(situationId)).findFirst().isPresent()
                ).findFirst().isPresent());
    }

}