package fi.livi.digitraffic.tie.data.dao;

import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2Situation;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecord;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecordValidyStatus;

public class Datex2RepositoryTest extends AbstractJpaTest {

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private EntityManager entityManager;

    @Before
    public void insertDatex() {
        datex2Repository.deleteAll();
        final Datex2 datex2 = new Datex2();

        datex2.setImportTime(ZonedDateTime.now());
        datex2.setMessageType(Datex2MessageType.ROADWORK);
        datex2.setMessage("Message of high importance");

        datex2Repository.save(datex2);
    }

    @Test
    public void testDelete() {
        final List<Datex2> all = datex2Repository.findAll();
        assertCollectionSize(1, all);

        datex2Repository.delete(all.get(0));

        final List<Datex2> after = datex2Repository.findAll();
        assertCollectionSize(0, after);
    }

    @Test
    public void activeInPastHours() {
        final String pastSituationId = "GUID12345678";
        final List<Datex2> beroreActive10Hours = datex2Repository.findAllActive(TRAFFIC_INCIDENT.name(), 10);
        // Situation should not exist
        Assert.assertFalse(beroreActive10Hours.stream()
            .filter(d -> d.getSituations() != null && d.getSituations().stream()
                         .filter(s -> s.getSituationId().equals(pastSituationId)).findFirst().isPresent())
            .findFirst().isPresent());

        // Create traffic disorder in past for 2 hours
        createDatex2InPast2h(pastSituationId, Datex2MessageType.TRAFFIC_INCIDENT);

        // Situation should be found >= 3 h
        final List<Datex2> active3Hours = datex2Repository.findAllActive(TRAFFIC_INCIDENT.name(), 3);
        Assert.assertTrue(active3Hours.stream()
            .filter(d -> d.getSituations() != null && d.getSituations().stream()
                .filter(s -> s.getSituationId().equals(pastSituationId)).findFirst().isPresent())
            .findFirst().isPresent());

        // 2 h in past wont find it as its little bit over 2 h old
        final List<Datex2> afterActive2Hours = datex2Repository.findAllActive(TRAFFIC_INCIDENT.name(), 2);
        Assert.assertFalse(afterActive2Hours.stream()
            .filter(d -> d.getSituations() != null && d.getSituations().stream()
                .filter(s -> s.getSituationId().equals(pastSituationId)).findFirst().isPresent())
            .findFirst().isPresent());
    }

    private Datex2 createDatex2InPast2h(final String situationId, final Datex2MessageType type) {
        final Datex2 datex2 = new Datex2();
        datex2.setImportTime(ZonedDateTime.now());
        datex2.setMessageType(type);
        datex2.setMessage("xml message");
        datex2.setPublicationTime(ZonedDateTime.now());

        Datex2Situation situation = new Datex2Situation();
        situation.setSituationId(situationId);
        situation.setDatex2(datex2);
        datex2.setSituations(Collections.singletonList(situation));

        Datex2SituationRecord record = new Datex2SituationRecord();
        record.setType(Datex2SituationRecordType.TRAFFIC_ELEMENT_ACCIDENT);
        record.setSituationRecordId(situationId + "01");
        record.setVersionTime(ZonedDateTime.now().minusHours(10));
        record.setCreationTime(ZonedDateTime.now().minusHours(10));
        record.setValidyStatus(Datex2SituationRecordValidyStatus.DEFINED_BY_VALIDITY_TIME_SPEC);
        record.setOverallStartTime(ZonedDateTime.now().minusHours(10));
        record.setOverallEndTime(ZonedDateTime.now().minusHours(2).minusSeconds(1));
        record.setSituation(situation);
        situation.setSituationRecords(Collections.singletonList(record));

        datex2Repository.save(datex2);
        datex2Repository.flush(); // native query used in findActive

        return datex2;
    }

}
