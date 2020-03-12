package fi.livi.digitraffic.tie.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.LockingDao;

@Service
public class LockingServiceInternal {

    private final LockingDao lockingDao;

    public LockingServiceInternal(final LockingDao lockingDao) {
        this.lockingDao = lockingDao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean tryLock(final String lockName, final int expirationSeconds, final long instanceId) {
        return lockingDao.acquireLock(lockName, instanceId, expirationSeconds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void unlock(final String lockName, final long instanceId) {
        lockingDao.releaseLock(lockName, instanceId);
    }
}
