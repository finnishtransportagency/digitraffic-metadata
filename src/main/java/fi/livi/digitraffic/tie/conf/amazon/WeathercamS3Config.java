package fi.livi.digitraffic.tie.conf.amazon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeathercamS3Config {

    @Bean
    public WeathercamS3Properties weathercamConfig(@Value("${dt.amazon.s3.weathercam.bucketName}") final String s3WeathercamBucketName,
                                                   @Value("${dt.amazon.s3.weathercam.region}") final String s3WeathercamRegion,
                                                   @Value("${dt.amazon.s3.weathercam.history.maxAgeHours}") final int historyMaxAgeHours,
                                                   @Value("${weathercam.baseUrl}") final String weathercamBaseUrl) {
        return new WeathercamS3Properties(s3WeathercamBucketName, s3WeathercamRegion, historyMaxAgeHours, weathercamBaseUrl);
    }
}
