package fi.livi.digitraffic.tie.data.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.LockingDao;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.LockingServiceInternal;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Import({LockingDao.class, LockingService.class, LockingServiceInternal.class})
public class LockingServiceTest extends AbstractJpaTest {
    private static final Logger log = LoggerFactory.getLogger(LockingServiceTest.class);

    private static final String LOCK = "lock";
    private static final int LOCK_EXPIRATION_S = 2;
    private static final int LOCK_EXPIRATION_DELTA_S = 2;
    private static final int LOCK_COUNT = 3;
    private static final int THREAD_COUNT = 2;

    private static final int LOCKING_TIME_EXTRA = 20; // how long it takes after obtaining lock to get timestamp?

    @Autowired
    private LockingService lockingService;

    @Test
    public void testGenerate() {
        Set<Long> ids = new HashSet<>();
        IntStream.range(0,20).forEach(i -> {
            long id = LockingService.generateInstanceId();
            Assert.assertFalse(ids.contains(id));
            ids.add(id);
        });
    }

    @Test
    public void multipleInstancesLockingNotOverlapping() {

        final List<Long> lockStarts = new CopyOnWriteArrayList<>();
        final List<Long> lockerInstanceIds  = new CopyOnWriteArrayList<>();

        final Collection<Future<?>> futures = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 1; i <= THREAD_COUNT; i++) {
            futures.add(executor.submit(new TryLocker(LOCK, lockingService, lockStarts, lockerInstanceIds)));
        }

        while (futures.stream().anyMatch(f -> !f.isDone())) {
            sleep(100);
        }

        Assert.assertEquals(lockStarts.size(), THREAD_COUNT * LOCK_COUNT);

        Long prevStart = null;
        for (Long start: lockStarts) {
            if (prevStart != null) {
                log.info("START={} DIFF={} s", start, (double)(start-prevStart)/1000.0);
                // Check that locks won't overlap
                assertNoOverlapWithExpiration(start, prevStart);
            } else {
                log.info("START={}", start);
            }
            prevStart = start;
        }

        checkSameInstanceNotConsecutively(lockerInstanceIds);
    }

    private Future<Boolean> tryLock(final String lockName, final int expirationSeconds,
                                    final ExecutorService executorService) {
        return executorService.submit(() -> lockingService.tryLock(lockName, expirationSeconds));
    }

    @Test
    public void lockingAfterExpiration() throws InterruptedException, ExecutionException {
        final String LOCK_NAME_1 = "Lock1";
        final String LOCK_NAME_2 = "Lock2";
        final int EXPIRATION_SECONDS = 5;

        final ExecutorService executor1 = Executors.newFixedThreadPool(1);
        final ExecutorService executor2 = Executors.newFixedThreadPool(1);

        // Acquire 1. lock @ instance 1
        Future<Boolean> futureLocked1 = tryLock(LOCK_NAME_1, EXPIRATION_SECONDS, executor1);
        waitCompletion(futureLocked1);
        long locked1Time = System.currentTimeMillis();
        Assert.assertTrue(futureLocked1.get());

        // 2. lock can be acquired @ instance 2
        Future<Boolean> locked2 = tryLock(LOCK_NAME_2, EXPIRATION_SECONDS, executor2);
        waitCompletion(locked2);
        Assert.assertTrue(locked2.get());

        // Try to acquire 1. lock again @ instance 2 -> fail
        Future<Boolean> locked1Second = tryLock(LOCK_NAME_1, EXPIRATION_SECONDS, executor2);
        waitCompletion(locked1Second);
        Assert.assertFalse(locked1Second.get());

        // Lock 2 can be acquired after 5 seconds
        while (!locked1Second.get()) {
            locked1Second = tryLock(LOCK_NAME_1, EXPIRATION_SECONDS, executor2);
            waitCompletion(locked1Second);

            long now = System.currentTimeMillis();
            final double timeFromLocking = (double) (now - locked1Time) / 1000.0;
            if (timeFromLocking > 4.95 ) {
                log.info("LOCK_NAME_1 acquired: {}, time from locking {} seconds", locked1Second.get(), timeFromLocking);
            }
            if (locked1Time > (now - (EXPIRATION_SECONDS -1)*1000) ) {
                Assert.assertFalse("Lock acquired before expiration", locked1Second.get());
            } else if (locked1Time < (now - (EXPIRATION_SECONDS+1) * 1000) ) {
                Assert.assertTrue("Failed to lock after expiration", locked1Second.get());
            }
        }
    }

    private void waitCompletion(final Future<Boolean> future) {
        while(!future.isDone()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void waitLockNotOverlapping() {
        final List<Long> lockStarts = new CopyOnWriteArrayList<>();
        final List<Long> lockerInstanceIds  = new CopyOnWriteArrayList<>();


        final Collection<Future<?>> futures = new ArrayList<>();
        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 1; i <= THREAD_COUNT; i++) {
            futures.add(executor.submit(new WaitLocker(LOCK, lockingService, lockStarts, lockerInstanceIds)));
        }

        while (futures.stream().anyMatch(f -> !f.isDone())) {
            sleep(100);
        }

        Assert.assertEquals(lockStarts.size(), THREAD_COUNT * LOCK_COUNT);

        Long prev = null;
        for (Long start: lockStarts) {
            if (prev != null) {
                log.info("START={} previous DIFF={} s", start, (double)(start-prev)/1000.0);
                Assert.assertTrue(String.format("start %s should be ge than previous %s",
                    DateHelper.toZonedDateTimeAtUtc(start),
                    DateHelper.toZonedDateTimeAtUtc(prev)),
                    start >= prev);
                // WaitLocker sleeps 200 ms before releasing the lock. LockService tries to lock every 100 ms, so max start time gap around 300 ms
                Assert.assertTrue(String.format("Between lock starts should be > 200 ms but was %d", start-prev), start-prev >= 200);
                Assert.assertTrue(String.format("Between lock starts should be < 650 ms but was %d", start-prev), start-prev <= 350);
            } else {
                log.info("START={}", start);
            }
            prev = start;
        }

        checkSameInstanceNotConsecutively(lockerInstanceIds);

    }

    private void checkSameInstanceNotConsecutively(final List<Long> lockerInstanceIds) {
        // Check that same instance won't get lock consecutively
        Long prevInstance = null;
        for (Long instance: lockerInstanceIds) {
            if (prevInstance != null) {
                Assert.assertTrue("Same instance got lock consecutively", !instance.equals(prevInstance));
            }
            prevInstance = instance;
        }
    }

    private void assertNoOverlapWithExpiration(final Long start, final Long prevStart) {
        ZonedDateTime startZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneOffset.UTC);
        final long startLimit = prevStart + LOCK_EXPIRATION_S * 1000 - LOCKING_TIME_EXTRA;
        Assert.assertTrue(String.format("start %s should be ge than %s", startZdt,  ZonedDateTime.ofInstant(Instant.ofEpochMilli(startLimit), ZoneOffset.UTC)),
            start >= startLimit);

        final long endLimit = prevStart + (LOCK_EXPIRATION_S + LOCK_EXPIRATION_DELTA_S) * 1000;
        Assert.assertTrue(String.format("Start %s should be le than %s", startZdt, ZonedDateTime.ofInstant(Instant.ofEpochMilli(endLimit), ZoneOffset.UTC)),
            start <= endLimit);
    }

    private class TryLocker implements Runnable {
        private final String lock;
        private final LockingService lockingService;
        private List<Long> lockStarts;
        private List<Long> lockInstanceIds;

        /**
         * Acquires given lock LOCK_COUNT times
         * @param lock
         * @param lockingService
         * @param lockStarts
         * @param lockInstanceIds
         */
        TryLocker(final String lock, final LockingService lockingService, final List<Long> lockStarts, final List<Long> lockInstanceIds) {
            this.lock = lock;
            this.lockingService = lockingService;
            this.lockStarts = lockStarts;
            this.lockInstanceIds = lockInstanceIds;
        }

        @Override
        public void run() {
            int counter = 0;
            while (counter < LOCK_COUNT) {
                final boolean locked = lockingService.tryLock(lock, LOCK_EXPIRATION_S);
                synchronized(LOCK) {
                    final long timestamp = System.currentTimeMillis();
                    if (locked) {
                        log.info("Acquired Lock=[{}] for instanceId=[{}] at {}", lock, lockingService.getThreadId(), ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC));
                        lockStarts.add(timestamp);
                        lockInstanceIds.add(lockingService.getThreadId());
                        counter++;
                    }
                }
                if (locked) {
                    // Sleep little more than expiration time so another thread should get the lock
                    sleep((LOCK_EXPIRATION_S + LOCK_EXPIRATION_DELTA_S) * 1000);
                }
            }
        }
    }

    private class WaitLocker implements Runnable {
        private final String lock;
        private final LockingService lockingService;
        private List<Long> lockStarts;
        private List<Long> lockInstanceIds;

        /**
         * Acquires given lock LOCK_COUNT times
         * @param lock
         * @param lockingService
         * @param lockStarts
         * @param lockInstanceIds
         */
        WaitLocker(final String lock, final LockingService lockingService,
                   final List<Long> lockStarts, final List<Long> lockInstanceIds) {
            this.lock = lock;
            this.lockingService = lockingService;
            this.lockStarts = lockStarts;
            this.lockInstanceIds = lockInstanceIds;
        }

        @Override
        public void run() {
            int counter = 0;
            while (counter < LOCK_COUNT) {
                lockingService.lock(lock, LOCK_EXPIRATION_S);

                final long timestamp = System.currentTimeMillis();
                log.info("Acquired Lock=[{}] for instanceId=[{}] at {}", lock, lockingService.getThreadId(), ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC));
                lockStarts.add(timestamp);
                lockInstanceIds.add(lockingService.getThreadId());
                counter++;

                // Sleep little and then release the lock for next thread
                sleep(200);
                lockingService.unlock(lock);
                // Sleep to make sure next thread will try the lock
                sleep(200);
            }
        }
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Sleep error" , e);
        }
    }

}
