package fi.livi.digitraffic.tie.data.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.response.TrafficDisordersDatex2Response;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2DataService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

@Import({Datex2DataService.class, Datex2UpdateService.class})
public abstract class AbstractDatex2DataServiceTest extends AbstractServiceTest {

    @Autowired
    protected Datex2DataService datex2DataService;

    @Autowired
    protected Datex2UpdateService datex2UpdateService;

    @Autowired
    protected V2Datex2UpdateService v2Datex2UpdateService;

    @Autowired
    @Qualifier("datex2Jaxb2Marshaller")
    protected Jaxb2Marshaller datex2Jaxb2Marshaller;

    @Autowired
    protected Datex2Repository datex2Repository;

    private static final String VERSION_TIME_REXP = "<situationRecordVersionTime>\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5][0-9]\\.\\d[0-9]\\d([+-]\\d[0-9]:\\d[09])<\\/situationRecordVersionTime>";

    private static Instant versionTime = Instant.now();

    protected static SituationPublication getSituationPublication(final TrafficDisordersDatex2Response response) {
        assertEquals(1, response.getDisorders().size());
        return ((SituationPublication) response.getDisorders().get(0).getD2LogicalModel().getPayloadPublication());
    }

    protected static String addEndTime(final String datex2Xml, final Instant endTime) {
        return StringUtils.replace(datex2Xml,
            "</overallStartTime>",
            "</overallStartTime>\n                        " +
                "<overallEndTime>" + endTime.toString() + "</overallEndTime>");
    }

    protected D2LogicalModel createModel(final String datex2Content) {
        final Object object = datex2Jaxb2Marshaller.unmarshal(new StringSource(datex2Content));

        if (object instanceof JAXBElement) {
            return ((JAXBElement<D2LogicalModel>) object).getValue();
        }

        return (D2LogicalModel)object;
    }

    protected List<Datex2MessageDto> createDtoList(final String datex2Content) {
//        final D2LogicalModel d2LogicalModel =  createModel(datex2Content);
//        return v2Datex2UpdateService.createModels(d2LogicalModel, null, DateHelper.getZonedDateTimeNowAtUtc());
        return null;
    }

    protected static String replaceVersionTimes(final String xml, final Instant replacement) {
        return RegExUtils.replacePattern(xml, VERSION_TIME_REXP,
            "<situationRecordVersionTime>" + replacement.toString() + "</situationRecordVersionTime>");
    }

    protected void findActiveTrafficAlertsAndAssert(final String situationId, final boolean found, final int inactiveHours) {
        final TrafficDisordersDatex2Response allActive = datex2DataService.findActiveTrafficDisorders(inactiveHours);
        assertEquals(found,
            allActive.getDisorders().stream()
                .anyMatch(d ->
                    ((SituationPublication) d.getD2LogicalModel().getPayloadPublication()).getSituations().stream().anyMatch(s -> s.getId().equals(situationId))
                ));
    }

    protected TrafficDisordersDatex2Response findTrafficAlertsAndAssert(final String situationId, final boolean found) {
        try {
            final TrafficDisordersDatex2Response response = datex2DataService.getAllTrafficDisordersBySituationId(situationId);
            assertTrue(found);

            final SituationPublication s = getSituationPublication(response);
            assertEquals(situationId, s.getSituations().get(0).getId());
            return response;
        } catch (final ObjectNotFoundException onfe) {
            // OK
            if (found) {
                Assert.fail("Situation " + situationId + " should have found");
            }
            return null;
        }
    }

    protected void updateTrafficIncidents(final String datex2Content) {
        final String updated = replaceVersionTimes(datex2Content, getNextVersionTime());
        datex2UpdateService.updateDatex2Data(createDtoList(updated));
    }

    protected void updateRoadworks(final String datex2Content) {
        datex2UpdateService.updateDatex2Data(createDtoList(datex2Content));
    }

    protected static Instant getNextVersionTime() {
        versionTime = versionTime.plusSeconds(1);
        return versionTime;
    }

    protected void updateWeightRestrictions(final String datex2Content) {
        datex2UpdateService.updateDatex2Data(createDtoList(datex2Content));
    }

    protected void deleteAllDatex2() {
        datex2Repository.deleteAll();
        assertTrue(datex2Repository.findAll().isEmpty());
    }


}
