package fi.livi.digitraffic.tie.data.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;

@Transactional
public class LockingDaoTest extends MetadataIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(LockingDao.class);

    private static final String LOCK_NAME_1 = "Lock1";
    private static final String INSTANCE_ID_1 = "1";
    private static final int EXPIRATION_SECONDS = 5;
    private static final String LOCK_NAME_2 = "Lock2";
    private static final String INSTANCE_ID_2 = "2";

    @Autowired
    private LockingDao lockingDao;

    @Before
    public void beforeTest() {
        lockingDao.relaseLock(LOCK_NAME_1, INSTANCE_ID_1);
        lockingDao.relaseLock(LOCK_NAME_2, INSTANCE_ID_1);
        lockingDao.relaseLock(LOCK_NAME_1, INSTANCE_ID_2);
        lockingDao.relaseLock(LOCK_NAME_2, INSTANCE_ID_2);
    }

    @After
    public void afterTest() {
        lockingDao.relaseLock(LOCK_NAME_1, INSTANCE_ID_1);
        lockingDao.relaseLock(LOCK_NAME_2, INSTANCE_ID_1);
        lockingDao.relaseLock(LOCK_NAME_1, INSTANCE_ID_2);
        lockingDao.relaseLock(LOCK_NAME_2, INSTANCE_ID_2);
    }

    @Test
    public void testLockingAfterExpiration() {

        // Aguire 1. lock
        boolean locked1 = lockingDao.aquireLock(LOCK_NAME_1, INSTANCE_ID_1, EXPIRATION_SECONDS);
        long locked1Time = System.currentTimeMillis();
        Assert.assertTrue(locked1);

        // Another lock can be aquired
        boolean locked2 = lockingDao.aquireLock(LOCK_NAME_2, INSTANCE_ID_2, EXPIRATION_SECONDS);
        Assert.assertTrue(locked2);

        // Try to aguire 1. lock again
        boolean locked1Second = lockingDao.aquireLock(LOCK_NAME_1, INSTANCE_ID_2, EXPIRATION_SECONDS);
        Assert.assertFalse(locked1Second);

        while (!locked1Second) {
            locked1Second = lockingDao.aquireLock(LOCK_NAME_1, INSTANCE_ID_2, EXPIRATION_SECONDS);
            long now = System.currentTimeMillis();
            log.info("LOCK_NAME_1 aquired: " + locked1Second + ", time from locking " +  (double)(now-locked1Time)/1000.0 + " seconds" );
            if (locked1Time > (now - (EXPIRATION_SECONDS -1)*1000) ) {
                Assert.assertFalse("Lock aquired before expiration", locked1Second);
            } else if (locked1Time < (now - (EXPIRATION_SECONDS+1) * 1000) ) {
                Assert.assertTrue("Failed to lock after expiration", locked1Second);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.debug("Interrupted", e);
            }
        }
    }

    @Test
    public void testLockingAndRelasing() {

        // Aguire 1. lock
        boolean locked1 = lockingDao.aquireLock(LOCK_NAME_1, INSTANCE_ID_1, EXPIRATION_SECONDS);
        Assert.assertTrue(locked1);

        // Try to aguire 1. lock again
        boolean locked1Second = lockingDao.aquireLock(LOCK_NAME_1, INSTANCE_ID_2, EXPIRATION_SECONDS);
        Assert.assertFalse(locked1Second);

        // relase lock
        lockingDao.relaseLock(LOCK_NAME_1, INSTANCE_ID_1);

        // Try to aguire 1. lock again
        boolean locked1Third = lockingDao.aquireLock(LOCK_NAME_1, INSTANCE_ID_2, EXPIRATION_SECONDS);
        Assert.assertTrue(locked1Third);
    }
}