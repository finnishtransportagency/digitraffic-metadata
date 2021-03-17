package fi.livi.digitraffic.tie.service.v2.datex2;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.GenericApplicationContext;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.AreaType;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;

public class V3RegionGeometryDataServiceTest extends AbstractRestWebTest {
    private static final Logger log = getLogger(V3RegionGeometryDataServiceTest.class);

    @Autowired
    private RegionGeometryRepository regionGeometryRepository;
    @Autowired
    private V3RegionGeometryDataService v3RegionGeometryDataService;
    @Autowired
    private DataStatusService dataStatusService;
    @Autowired
    private GenericApplicationContext applicationContext;

    @MockBean
    private RegionGeometryGitClient regionGeometryGitClientMock;

    private V3RegionGeometryTestHelper v3RegionGeometryTestHelper;

    @Before
    public void init() {
        final V3RegionGeometryUpdateService v3RegionGeometryUpdateService =
            applicationContext.getAutowireCapableBeanFactory().createBean(V3RegionGeometryUpdateService.class);
        v3RegionGeometryTestHelper = new V3RegionGeometryTestHelper(regionGeometryGitClientMock, v3RegionGeometryUpdateService, dataStatusService);

        regionGeometryRepository.deleteAll();
    }

    @Test
    public void getAreaLocationRegionEffectiveOn_WhenToCommitsHaveSameffectiveDatLatestCommitReturned() {
        final Instant secondAndThirdCommiteffectiveDate = Instant.now();
        final Instant firstCommiteffectiveDate = secondAndThirdCommiteffectiveDate.minus(1, ChronoUnit.DAYS);
        final String commitId1 = RandomStringUtils.randomAlphanumeric(32);
        final String commitId2 = RandomStringUtils.randomAlphanumeric(32);
        final String commitId3 = RandomStringUtils.randomAlphanumeric(32);
        final List<RegionGeometry> commit1Changes = createCommit(commitId1, firstCommiteffectiveDate, 1,2,3);
        final List<RegionGeometry> commit2Changes = createCommit(commitId2, secondAndThirdCommiteffectiveDate, 1,2,3);
        final List<RegionGeometry> commit3Changes = createCommit(commitId3, secondAndThirdCommiteffectiveDate, 1,2,3);

        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(null))).thenReturn(commit1Changes);
        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(commitId1))).thenReturn(commit2Changes);
        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(commitId2))).thenReturn(commit3Changes);

        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit1
        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit2
        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit3

        v3RegionGeometryDataService.refreshCache();

        // Latest valid on time should be returned
        assertVersion(commit3Changes.get(0),
                      v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(1, secondAndThirdCommiteffectiveDate));

        // First commit should be returned
        assertVersion(commit1Changes.get(0),
                      v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(1, secondAndThirdCommiteffectiveDate.minusSeconds(1)));
    }

    @Test
    public void getAreaLocationRegionEffectiveOn_WhenThereIsInvalidTypeFirstValidShouldReturn() {
        final Instant secondCommiteffectiveDate = Instant.now();
        final Instant firstCommiteffectiveDate = secondCommiteffectiveDate.minus(1, ChronoUnit.DAYS);
        final String commitId1 = RandomStringUtils.randomAlphanumeric(32);
        final String commitId2 = RandomStringUtils.randomAlphanumeric(32);
        final List<RegionGeometry> commit1Changes = Collections.singletonList(
            RegionGeometryTestHelper.createNewRegionGeometry(1, firstCommiteffectiveDate, commitId1, AreaType.UNKNOWN));
        final List<RegionGeometry> commit2Changes = createCommit(commitId2, secondCommiteffectiveDate, 1);

        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(null))).thenReturn(commit1Changes);
        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(commitId1))).thenReturn(commit2Changes);

        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit1
        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit2
        v3RegionGeometryDataService.refreshCache();

        // Even when asking version valid from commit1, it should not be returned as it is not valid
        // Instead commit2 version should be returned although it's not effective but it's first effective that is valid
        assertVersion(commit2Changes.get(0),
            v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(1, firstCommiteffectiveDate));
    }

    private void assertVersion(final RegionGeometry expected, final RegionGeometry actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
    }

    /**
     * Creates commit contents
     */
    private List<RegionGeometry> createCommit(final String commitId1, final Instant effectiveDate, int...locationCode) {
        return Arrays.stream(locationCode)
            .mapToObj(i -> RegionGeometryTestHelper.createNewRegionGeometry(i, effectiveDate, commitId1))
            .collect(Collectors.toList());
    }
}
