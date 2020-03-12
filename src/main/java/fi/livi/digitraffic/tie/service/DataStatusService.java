package fi.livi.digitraffic.tie.service;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.dao.v1.DataUpdatedRepository;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.DataUpdated;

@Service
public class DataStatusService {
    private static final Logger log = LoggerFactory.getLogger(DataStatusService.class);

    private final DataUpdatedRepository dataUpdatedRepository;

    @Autowired
    public DataStatusService(final DataUpdatedRepository dataUpdatedRepository) {
        this.dataUpdatedRepository = dataUpdatedRepository;
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType) {
        updateDataUpdated(dataType, (String)null);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final String version) {
        final DataUpdated updated = dataUpdatedRepository.findByDataType(dataType);
        log.debug("method=updateDataUpdated dataType={}, dataVersion={}", dataType, version);
        if (updated == null) {
            dataUpdatedRepository.save(new DataUpdated(dataType, ZonedDateTime.now(), version));
        } else {
            updated.setUpdatedTime(ZonedDateTime.now());
            updated.setVersion(version);
        }
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final Instant updated) {
        final DataUpdated dataUpdated = dataUpdatedRepository.findByDataType(dataType);
        log.debug("method=updateDataUpdated dataType={}, updatedTime={}", dataType, updated);

        if(updated != null) {
            if (dataUpdated == null) {
                dataUpdatedRepository.save(new DataUpdated(dataType, DateHelper.toZonedDateTimeAtUtc(updated), null));
            } else {
                dataUpdated.setUpdatedTime(DateHelper.toZonedDateTimeAtUtc(updated));
            }
        }
    }

    @Transactional(readOnly = true)
    public ZonedDateTime findDataUpdatedTime(final DataType dataType) {
        return DateHelper.toZonedDateTimeAtUtc(dataUpdatedRepository.findUpdatedTime(dataType));
    }

    @Transactional(readOnly = true)
    public Instant getTransactionStartTime() {
        return dataUpdatedRepository.getTransactionStartTime();
    }
}
