package fi.livi.digitraffic.tie.metadata.dto.location;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

public interface LocationJson {
    @Value("#{target.id.locationCode}")
    int getLocationCode();

    String getSubtypeCode();
    String getRoadJunction();

    String getRoadName();
    String getFirstName();
    String getSecondName();

    Integer getAreaRef();
    Integer getLinearRef();

    Integer getNegOffset();

    Integer getPosOffset();

    Boolean getUrban();

    BigDecimal getWgs84Lat();
    BigDecimal getWgs84Long();

    BigDecimal getEtrsTm35FinX();
    BigDecimal getEtrsTm35FixY();

    String getNegDirection();
    String getPosDirection();

    String getGeocode();
    String getOrderOfPoint();
}
