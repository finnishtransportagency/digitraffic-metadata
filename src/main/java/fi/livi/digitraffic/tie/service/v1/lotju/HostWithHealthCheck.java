package fi.livi.digitraffic.tie.service.v1.lotju;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.conf.RestTemplateConfiguration;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;

public class HostWithHealthCheck {
    private static final Logger log = LoggerFactory.getLogger(HostWithHealthCheck.class);

    private final RestTemplate restTemplate;
    private final URI dataUrl;
    private final String baseUrl;
    private final String healthUrl;
    private final int healthTtlSeconds;
    private final String healtOkValue;

    private boolean healthy = true;
    private Instant nextHealthCheckTime = Instant.now();

    /**
     *
     * @param baseUrl ie. https://example.com
     * @param dataPath ie. /service/data1
     * @param healthPath ie. /healthcheck. If empty no healt check is performed
     * @param healthTtlSeconds Health check time to live
     */
    public HostWithHealthCheck(final String baseUrl, final String dataPath, final String healthPath, int healthTtlSeconds, final String healtOkValue) {
        this.baseUrl = baseUrl;
        this.healthTtlSeconds = healthTtlSeconds;
        this.healtOkValue = healtOkValue;
        this.healthUrl = StringUtils.isNotEmpty(healthPath) ? (baseUrl + healthPath) : null;
        this.dataUrl = URI.create(baseUrl + dataPath);
        restTemplate = RestTemplateConfiguration.createRestTemplate(10, 10);

        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException(String.format("Param baseUrl:\"%s\" can't be empty value", baseUrl));
        }
        if (StringUtils.isBlank(dataPath)) {
            throw new IllegalArgumentException(String.format("Param dataPath:\"%s\" can't be empty value", dataPath));
        }
        if (StringUtils.isNotBlank(healthUrl) && StringUtils.isBlank(healtOkValue)) {
            throw new IllegalArgumentException(String.format("Param healtOkValue:\"%s\" can't be empty value", healtOkValue));
        }

        log.info("Created HostWithHealthCheck healthCheckUrl={} dataUrl={} healthTtlSeconds={} healtOkValue={}", healthUrl, dataUrl.toString(), healthTtlSeconds, healtOkValue);
    }

    /**
     * Checks if health check is needed (ttl exceeded) and returns either health check status or cached value.
     * @return health status
     */
    public boolean doHealthCheck() {
        // If healthUrl is not set, then recover host if ttl timeout is passed
        // Host will be marked as not healthy externally by calling setHealthy(false);
        if (healthUrl == null && isHealthCheckNeeded()) {
            log.info("method=doHealthCheck healthCheckUrl={} dataUrl={} healthCheckValue={} healthCheckExpectedValue={} returnStatus=true not performed as there is no health url", healthUrl, dataUrl, "not_performed_no_health_check", null);
            healthy = true;
            return true;
        } else if (!isHealthCheckNeeded()) {
            log.info("method=doHealthCheck healthCheckUrl={} dataUrl={} healthCheckValue={} healthCheckExpectedValue={} returnStatus=true not performed as ttl not exceeded", healthUrl, dataUrl, "not_performed_ttl", null);
            return healthy;
        }

        final String healthString = doRequestHealthString();

        if ( StringUtils.trimToEmpty(healthString).equalsIgnoreCase(healtOkValue) ) {
            log.info("method=doHealthCheck healthCheckUrl={} dataUrl={} healthCheckValue={} healthCheckExpectedValue={} returnStatus=true", healthUrl, dataUrl, healthString, healtOkValue);
            setHealthy(true);
            return true;
        }
        log.info("method=doHealthCheck healthCheckUrl={} dataUrl={} healthCheckValue={} healthCheckExpectedValue={} returnStatus=false", healthUrl, dataUrl, healthString, healtOkValue);
        setHealthy(false);
        return false;
    }

    /**
     * Requests health from server.
     * @return Null if there is error.
     */
    private String doRequestHealthString() {
        try {
            final ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getBody();
        } catch (final Exception e) {
            log.warn(String.format("method=doRequestHealthStatus Health check for healthCheckUrl=%s failed", healthUrl), e);
            return null;
        }
    }

    private boolean isHealthCheckNeeded() {
        return !Instant.now().isBefore(nextHealthCheckTime);
    }

    public void setHealthy(final boolean healthy) {
        final boolean changed = this.healthy != healthy;
        this.healthy = healthy;
        final Instant now = Instant.now();
        nextHealthCheckTime = now.plusSeconds(healthTtlSeconds);
        if (changed) {
            log.info("method=setHealthy Change server baseUrl={} dataUrl={} fromHealthy={} toHealthy={} healthChecked={}", baseUrl, dataUrl.toString(), !this.healthy, this.healthy, now);
        }
    }
    public URI getDataUrl() {
        return dataUrl;
    }

    public static List<HostWithHealthCheck> createHostsWithHealthCheck(final String[] baseUrls, final String dataPath, final String healthPath, final int healthTtlSeconds, final String healtOkValue) {
        if ( baseUrls == null || baseUrls.length == 0 ) {
            throw new IllegalArgumentException(String.format("method=createHostsWithHealthCheck failed because no addresses in baseUrls=%s:", Arrays.toString(baseUrls)));
        }
        return Arrays.stream(baseUrls)
            .map(baseUrl -> new HostWithHealthCheck(baseUrl, dataPath, healthPath, healthTtlSeconds, healtOkValue))
            .collect(Collectors.toList());
    }
}