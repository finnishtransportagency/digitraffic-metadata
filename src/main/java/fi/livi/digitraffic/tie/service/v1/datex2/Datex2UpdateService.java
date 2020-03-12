package fi.livi.digitraffic.tie.service.v1.datex2;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

@Service
public class Datex2UpdateService {
    private final Datex2Repository datex2Repository;
    private final V2Datex2UpdateService v2Datex2UpdateService;

    private static final Logger log = LoggerFactory.getLogger(Datex2UpdateService.class);

    @Autowired
    public Datex2UpdateService(final Datex2Repository datex2Repository,
                               final V2Datex2UpdateService v2Datex2UpdateService) {
        this.datex2Repository = datex2Repository;
        this.v2Datex2UpdateService = v2Datex2UpdateService;
    }

    @Transactional
    public Map<String, ZonedDateTime> listSituationVersionTimes(final Datex2MessageType messageType) {
        final Map<String, ZonedDateTime> map = new HashMap<>();

        for (final Object[] o : datex2Repository.listDatex2SituationVersionTimes(messageType.name())) {
            final String situationId = (String) o[0];
            final ZonedDateTime versionTime = DateHelper.toZonedDateTimeAtUtc(((Timestamp)o[1]).toInstant());

            if (map.put(situationId, versionTime) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        return map;
    }

    @Transactional
    public int updateDatex2Data(final List<Datex2MessageDto> data) {
        return (int) data.stream().filter(d -> v2Datex2UpdateService.updateDatex2Data(d)).count();
    }

}
