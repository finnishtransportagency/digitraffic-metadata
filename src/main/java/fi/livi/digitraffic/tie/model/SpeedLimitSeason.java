package fi.livi.digitraffic.tie.model;

public enum SpeedLimitSeason {

    SUMMER(1), WINTER(2);

    private final int code;

    SpeedLimitSeason(int code) {
        this.code = code;
    }

    public static SpeedLimitSeason getSpeedLimitSeasonFromCode(int code) {
        for (SpeedLimitSeason season : SpeedLimitSeason.values()) {
            if (season.code == code) {
                return season;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }
}
