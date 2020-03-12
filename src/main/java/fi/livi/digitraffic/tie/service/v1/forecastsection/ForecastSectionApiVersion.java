package fi.livi.digitraffic.tie.service.v1.forecastsection;

public enum ForecastSectionApiVersion {
    V1(1),
    V2(2);

    private final int version;

    ForecastSectionApiVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
