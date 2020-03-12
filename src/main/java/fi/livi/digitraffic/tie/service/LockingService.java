package fi.livi.digitraffic.tie.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LockingService {

    private static final Logger log = LoggerFactory.getLogger(LockingService.class);
    private LockingServiceInternal lockingServiceInternal;
    private final static Set<Long> generatedInstanceIds = new HashSet<>();
    public LockingService(final LockingServiceInternal lockingServiceInternal) {
        this.lockingServiceInternal = lockingServiceInternal;
    }

    /**
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread will sleep
     * until the lock has been acquired.
     *
     * @param lockName lock name to acquire
     * @param expirationSeconds How long will lock stay valid if it is not refreshed or released.
     */
    public void lock(final String lockName, final int expirationSeconds) {
        lock(lockName, expirationSeconds, getThreadId());
    }

    public void lock(final String lockName, final int expirationSeconds, final long overrideInstanceId) {
        final StopWatch start = StopWatch.createStarted();
        while (!tryLock(lockName, expirationSeconds, overrideInstanceId)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("method=lock Error sleep interrupted", e);
                throw new RuntimeException(e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("method=lock lockName={} tookMs={} forThreadId={}", lockName, start.getTime(), overrideInstanceId);
        }
    }

    /**
     * Acquires the lock only if it is free at the time of invocation
     * or the caller already have it.
     *
     * <p>Acquires the lock if it is available and returns immediately
     * with the value {@code true}.
     * If the lock is not available then this method will return
     * immediately with the value {@code false}.
     *
     * @param lockName lock name to acquire
     * @param expirationSeconds How long will lock stay valid if it is not refreshed or released.
     * @return {@code true} if the lock was acquired and
     *         {@code false} otherwise
     */
    public boolean tryLock(final String lockName, final int expirationSeconds) {
        return tryLock(lockName, expirationSeconds, getThreadId());
    }

    public boolean tryLock(String lockName, int expirationSeconds, final long overrideInstanceId) {
        final StopWatch start = StopWatch.createStarted();
        final boolean locked = lockingServiceInternal.tryLock(lockName, expirationSeconds, overrideInstanceId);
        if (log.isDebugEnabled()) {
            log.debug("method=tryLock lockName={} tookMs={} forThreadId={}", lockName, start.getTime(), overrideInstanceId);
        }
        return locked;
    }

    /**
     * Releases the lock.
     *
     * @param lockName lock name to acquire
     */
    public void unlock(final String lockName) {
        unlock(lockName, getThreadId());
    }

    public void unlock(final String lockName, final long overrideInstanceId) {
        final StopWatch start = StopWatch.createStarted();
        lockingServiceInternal.unlock(lockName, overrideInstanceId);
        if (log.isDebugEnabled()) {
            log.debug("method=unlock lockName={} tookMs={} forThreadId={}", lockName, start.getTime(), overrideInstanceId);
        }
    }

    /**
     * Unique instance id per thread.
     *
     * @return Thread id
     */
    public static long getThreadId() {
        return Thread.currentThread().getId();
    }

    /**
     * Generates unique id
     */
    public synchronized static long generateInstanceId() {
        return UUID.randomUUID().getMostSignificantBits();
    }
}
