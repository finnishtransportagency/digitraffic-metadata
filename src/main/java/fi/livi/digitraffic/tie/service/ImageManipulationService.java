package fi.livi.digitraffic.tie.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;

@ConditionalOnNotWebApplication
public class ImageManipulationService {

    private static final ExifRewriter exifRewriter = new ExifRewriter();

    /**
     * Removes JPG image Exif metadata losslessly
     * @param in image file contents
     * @return image file contents without Exif metadata
     */
    public static byte[] removeJpgExifMetadata(final byte[] in) throws ImageWriteException, ImageReadException, IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        exifRewriter.removeExifMetadata(in, os);
        return os.toByteArray();
    }
}