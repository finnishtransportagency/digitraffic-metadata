package fi.livi.digitraffic.tie.dto.v1.location;

import org.springframework.beans.factory.annotation.Value;

public interface LocationSubtypeJson {
    @Value("#{target.id.subtypeCode}")
    String getSubtypeCode();

    String getDescriptionEn();

    String getDescriptionFi();
}
