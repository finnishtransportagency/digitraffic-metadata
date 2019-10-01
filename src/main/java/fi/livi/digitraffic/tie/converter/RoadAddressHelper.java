package fi.livi.digitraffic.tie.converter;

public final class RoadAddressHelper {
    private RoadAddressHelper() {}

    public static RoadAddress parseRoadAddress(final String roadAddress) {
        final String s[] = roadAddress.split(" ");

        return new RoadAddress(s[0], s[1], Long.parseLong(s[2]));
    }

    public static class RoadAddress {
        public final String roadNumber;
        public final String roadSection;
        public final long distance;

        public RoadAddress(final String roadNumber, final String roadSection, final long distance) {
            this.roadNumber = roadNumber;
            this.roadSection = roadSection;
            this.distance = distance;
        }
    }
}
