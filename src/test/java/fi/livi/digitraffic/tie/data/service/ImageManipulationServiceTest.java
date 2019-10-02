package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.Resource;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;

public class ImageManipulationServiceTest extends AbstractDaemonTestWithoutS3 {

    @Test
    public void imageExifRemovalSuccess() throws IOException, ImagingException {
        final byte[] imageWithExif = FileUtils.readFileToByteArray(loadResource("classpath:/lotju/kuva/exif.jpg").getFile());
        final byte[] imageWithoutExif = FileUtils.readFileToByteArray(loadResource("classpath:/lotju/kuva/noExif.jpg").getFile());

        final byte[] imageExifRemoved = ImageManipulationService.removeJpgExif(imageWithExif);

        Assert.assertThat(imageWithExif, IsNot.not(IsEqual.equalTo(imageWithoutExif)));
        Assert.assertThat(imageWithExif, IsNot.not(IsEqual.equalTo(imageExifRemoved)));
        Assert.assertArrayEquals(imageWithoutExif, imageExifRemoved);
    }

    @Test
    public void imageWithoutExifShouldStaySame() throws IOException, ImagingException {
        final Resource imgResource = loadResource("classpath:/lotju/kuva/withoutExif.jpg");
        final byte[] withExif = FileUtils.readFileToByteArray(imgResource.getFile());
        final byte[] withoutExif = ImageManipulationService.removeJpgExif(withExif);
        Assert.assertArrayEquals(withExif, withoutExif);
    }
}
