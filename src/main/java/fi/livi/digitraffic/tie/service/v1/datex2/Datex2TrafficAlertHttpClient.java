package fi.livi.digitraffic.tie.service.v1.datex2;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.helper.FileGetService;

@Component
public class Datex2TrafficAlertHttpClient {
    private static final Logger log = LoggerFactory.getLogger(Datex2TrafficAlertHttpClient.class);

    // Old ftp format  InfoXML_2017-04-26-07-50-58-913.xml
    // New http format  Datex2_2017-04-26-07-51-04-245.xml
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("'Datex2_'yyyy-MM-dd-HH-mm-ss-SSS'.xml'");

    private static final Pattern fileNamePattern = Pattern.compile("href=\"(Datex2_[0-9-]*\\.xml)\"");
    private static final String AUTO_INDEX_QUERY_ARGUMENTS = "?F=0&C=N&O=D";

    private final String url;
    private final FileGetService fileGetService;

    @Autowired
    public Datex2TrafficAlertHttpClient(@Value("${datex2.traffic.alerts.url}") final String url, final FileGetService
        fileGetService) {
        this.url = url;
        this.fileGetService = fileGetService;
    }

    public List<Pair<String, Instant>> getTrafficAlertMessages(final Instant from) {
        final String html = getContent(url + AUTO_INDEX_QUERY_ARGUMENTS);

        final List<Pair<String, Instant>> newFiles = getNewFiles(from, html);

        return getDatex2Messages(newFiles);
    }

    private String getContent(final String url) {
        return fileGetService.getFile(Datex2MessageType.TRAFFIC_INCIDENT.name(), url, String.class);
    }

    private List<Pair<String, Instant>> getNewFiles(final Instant from, final String html) {
        final Matcher m = fileNamePattern.matcher(html);
        final List<Pair<String, Instant>> filenames = new ArrayList<>();

        while (m.find()) {
            final String filename = m.group(1);
            final Instant fileTimestamp = parseDate(filename);
            // Links in html are ordered by filename which contains file date (from newest to oldest)
            if (!isNewFile(from, fileTimestamp)) {
                break;
            }
            if (filename != null && filenames.stream().noneMatch(f -> f.getLeft().equals(filename))) {
                filenames.add(Pair.of(filename, fileTimestamp));
            }
        }
        // Sort files from oldest to newest, and limit to 1000
        return filenames.stream().sorted(Comparator.comparing(Pair::getLeft)).limit(1000).collect(Collectors.toList());
    }

    private boolean isNewFile(final Instant from, final Instant fileDate) {
        return from == null || (fileDate != null && from.isBefore(fileDate));
    }

    private List<Pair<String, Instant>> getDatex2Messages(final List<Pair<String, Instant>> filenames) {
        final List<Pair<String, Instant>> messages = new ArrayList<>();

        for (final Pair<String, Instant> filename : filenames) {
            final String datex2Url = url + filename.getLeft();
            final String content = getContent(datex2Url);
            messages.add(Pair.of(content, filename.getRight()));
        }
        return messages;
    }


    private Instant parseDate(final String filename) {
        try {
            return TimeFromFilenameParser.parseDate(filename);
        } catch (final DateTimeParseException ex) {
            log.error("Unable to parse date: " + filename, ex);
        }
        return null;
    }
}
