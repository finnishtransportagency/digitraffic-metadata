package fi.livi.digitraffic.tie.service.v1.location;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;
import fi.livi.digitraffic.tie.model.v1.location.Location;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;

public class LocationReader extends AbstractReader<Location> {
    private final Map<String, LocationSubtype> subtypeMap;
    private final Map<Integer, Integer> areaRefMap = new HashMap<>();
    private final Map<Integer, Integer> linearRefMap = new HashMap<>();

    private static final String GEOCODE_FIN_CODE = "FinCode:";

    public LocationReader(final Map<String, LocationSubtype> subtypeMap, final String version) {
        super(Charsets.UTF_8, DELIMITER_COMMA, version);
        this.subtypeMap = subtypeMap;
    }

    @Override
    protected Location convert(final String[] components) {
        final Location location = new Location();

        location.setVersion(version);
        location.setLocationCode(parseInteger(components[2]));
        location.setRoadJunction(parseString(components[6]));
        location.setRoadName(parseString(components[7]));
        location.setFirstName(parseString(components[8]));
        location.setSecondName(parseString(components[9]));
        location.setNegOffset(parseInteger(components[12]));
        location.setPosOffset(parseInteger(components[13]));
        location.setUrban(parseBoolean(components[14]));
        location.setWgs84Lat(parseWgs84(components[16]));
        location.setWgs84Long(parseWgs84(components[17]));
        location.setEtrsTm35FinX(parseEtrsTm35(components[18]));
        location.setEtrsTm35FixY(parseEtrsTm35(components[19]));
        location.setPosDirection(parseString(components[21]));
        location.setNegDirection(parseString(components[22]));
        location.setGeocode(parseGeocode(components[24]));
        location.setOrderOfPoint(parseInteger(components[25]));
        location.setSubtypeCode(parseSubtype(components[3], components[4], components[5], subtypeMap));

        addAreaRef(location, components[10]);
        addLinearRef(location, components[11]);

        return location;
    }

    private static String parseString(final String component) {
        return StringUtils.defaultIfEmpty(component, null);
    }

    private String parseGeocode(final String component) {
        if(StringUtils.isEmpty(component)) {
            return null;
        }

         if(!component.startsWith(GEOCODE_FIN_CODE)) {
             log.error("invalid geocode={}", component);
             return null;
         }

        return component.substring(GEOCODE_FIN_CODE.length());
    }

    private static String parseSubtype(final String classValue, final String typeValue, final String subtypeValue, final Map<String, LocationSubtype> subtypeMap) {
        final String subtypeCode = String.format("%s%s.%s", classValue, typeValue, subtypeValue);

        final LocationSubtype subtype = subtypeMap.get(subtypeCode);

        if(subtype == null) {
            throw new IllegalArgumentException("Could not find subtype " + subtypeCode);
        }

        return subtype.getSubtypeCode();
    }

    private static Boolean parseBoolean(final String value) {
        final Integer i = parseInteger(value);

        return i == null ? null : (i == 0 ? false : true);
    }

    private static BigDecimal parseWgs84(final String value) {
        return parseDecimal(value, 5);
    }

    private static BigDecimal parseEtrsTm35(final String value) {
        return parseDecimal(value, 9);
    }

    private static BigDecimal parseDecimal(final String value, final int scale) {
        return StringUtils.isEmpty(value) ? null : new BigDecimal(value.replace(',', '.')).setScale(scale, RoundingMode.HALF_UP);
    }

    private void addLinearRef(final Location location, final String value) {
        final Integer refValue = parseInteger(value);

        // for some reason, there is no 0 present
        if(refValue != null && !refValue.equals(0)) {
            linearRefMap.put(location.getLocationCode(), refValue);
        }
    }

    private void addAreaRef(final Location location, final String value) {
        final Integer refValue = parseInteger(value);

        // for some reason, there is no 0 present
        if(refValue != null && !refValue.equals(0)) {
            areaRefMap.put(location.getLocationCode(), refValue);
        }
    }

    private static Integer parseInteger(final String value) {
        return StringUtils.isEmpty(value) ? null :
               Integer.parseInt(value);
    }

    public Map<Integer, Integer> getLinearRefMap() {
        return linearRefMap;
    }

    public Map<Integer, Integer> getAreaRefMap() {
        return areaRefMap;
    }
}
