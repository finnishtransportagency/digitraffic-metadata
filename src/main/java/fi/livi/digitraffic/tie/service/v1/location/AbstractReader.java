package fi.livi.digitraffic.tie.service.v1.location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

public abstract class AbstractReader<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final char DELIMITER_COMMA = ',';
    protected static final char DELIMITER_SEMICOLON = ';';
    protected static final char DELIMITER_TAB = '\t';
    protected static final char QUOTE = '\"';

    private final Charset charset;
    private final char delimeterCharacter;
    private final char quoteCharacter;
    protected final String version;

    protected AbstractReader(final Charset charset, final char delimeterCharacter, final String version) {
        this.charset = charset;
        this.delimeterCharacter = delimeterCharacter;
        this.quoteCharacter = QUOTE;
        this.version = version;
    }

    protected AbstractReader(final String version) {
        this(StandardCharsets.ISO_8859_1, DELIMITER_TAB, version);
    }

    public List<T> read(final Path path) {
        return read(path.toFile());
    }

    public List<T> read(final URL url) throws IOException {
        return read(url.openStream());
    }

    public List<T> read(final File file) {
        try {
            return read(new FileInputStream(file));
        } catch (final FileNotFoundException e) {
            log.error("error reading file", e);
        }

        return Collections.emptyList();
    }

    public List<T> read(final InputStream inputStream) {
        try (final CSVReader reader = new CSVReader(new InputStreamReader(inputStream, charset), delimeterCharacter, quoteCharacter)) {
            return StreamSupport.stream(reader.spliterator(), false).skip(1)
                    .map(this::convert)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch(final IOException ioe) {
            log.error("error opening file", ioe);
        }

        return Collections.emptyList();
    }

    protected abstract T convert(final String[] line);
}
