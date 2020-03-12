package fi.livi.digitraffic.tie.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jcabi.manifests.Manifests;

@Service
public class BuildVersionService {

    private static final Logger log = LoggerFactory.getLogger(BuildVersionService.class);

    private static final String GIT_PROPERTIES  = "git.properties";
    private static final String GIT_COMMIT_ID_HASH = "git.commit.id.abbrev";
    private static final String GIT_BUILD_TIME  = "git.build.time";
    private static final String MANIFEST_APP_VERSION  = "RoadApplication-Version";


    private String getAppVersion() {
        if (Manifests.exists(MANIFEST_APP_VERSION)) {
            return Manifests.read(MANIFEST_APP_VERSION);
        }
        return "DEV-BUILD";
    }

    private String getAppBuildRevision() {
        final String tag = readProperty(GIT_COMMIT_ID_HASH);
        if (StringUtils.isNotBlank(tag)) {
            return tag;
        }
        return "?";
    }

    private String getAppBuildTime() {
        final String time = readProperty(GIT_BUILD_TIME);
        if (StringUtils.isNotBlank(time)) {
            return time;
        }
        return "?";
    }


    public String getAppFullVersion() {
        final String version = String.format("%s#%s@%s", getAppVersion(), getAppBuildRevision(), getAppBuildTime());
        log.info("Application version: {}", version);
        return version;
    }

    private String readProperty(final String property) {
        final Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(GIT_PROPERTIES));
            final String prop = (String) properties.get(property);
            if (StringUtils.isNotBlank(prop)) {
                return prop;
            }
            return null;
        } catch (IOException ioe) {
            log.error("Failed to load git properties from file: " + GIT_PROPERTIES, ioe);
            return null;
        }
    }

}
