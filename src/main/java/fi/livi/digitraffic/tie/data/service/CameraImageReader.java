package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.ely.lotju.kamera.proto.KuvaProtos;

@Component
@ConditionalOnNotWebApplication
public class CameraImageReader {

    private static final Logger log = LoggerFactory.getLogger(CameraImageReader.class);

    private final int connectTimeout;
    private final int readTimeout;
    private final String cameraUrl;

    CameraImageReader(
        @Value("${camera-image-uploader.http.connectTimeout}")
        final int connectTimeout,
        @Value("${camera-image-uploader.http.readTimeout}")
        final int readTimeout,
        @Value("${camera-image-download.url}")
        final String cameraUrl
    ) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.cameraUrl = cameraUrl;
    }

    byte[] readImage(final KuvaProtos.Kuva kuva, final ImageUpdateInfo info) throws IOException {
        final String imageDownloadUrl = getCameraDownloadUrl(kuva);
        info.setDownloadUrl(imageDownloadUrl);

        final URL url = new URL(imageDownloadUrl);
        final URLConnection con = url.openConnection();
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        try (final InputStream is = con.getInputStream()) {
            return IOUtils.toByteArray(is);
        }
    }

    private String getCameraDownloadUrl(final KuvaProtos.Kuva kuva) {
        return StringUtils.appendIfMissing(cameraUrl, "/") + kuva.getKuvaId();
    }

}