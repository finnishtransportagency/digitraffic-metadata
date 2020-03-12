package fi.livi.digitraffic.tie.service.v1.forecastsection;

public final class ForecastSectionNaturalIdHelper {

    private ForecastSectionNaturalIdHelper() {
    }

    public static int getRoadNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(0, 5));
    }

    public static int getRoadSectionNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(6, 9));
    }

    public static int getRoadSectionVersionNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(10, 13));
    }
}