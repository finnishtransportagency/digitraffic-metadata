package fi.livi.digitraffic.tie.converter.feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;

public class AbstractMetadataToFeatureConverter {
    private static final Logger log = LoggerFactory.getLogger(AbstractMetadataToFeatureConverter.class);

    protected final CoordinateConverter coordinateConverter;

    protected AbstractMetadataToFeatureConverter(final CoordinateConverter coordinateConverter) {
        this.coordinateConverter = coordinateConverter;
    }

    protected static void setRoadStationProperties(final RoadStationProperties properties, final RoadStation roadStation) {
        properties.setNaturalId(roadStation.getNaturalId());
        properties.setCollectionInterval(roadStation.getCollectionInterval());
        properties.setCollectionStatus(roadStation.getCollectionStatus());
        properties.setMunicipality(roadStation.getMunicipality());
        properties.setMunicipalityCode(roadStation.getMunicipalityCode());

        properties.setProvince(roadStation.getProvince());
        properties.setProvinceCode(roadStation.getProvinceCode());

        properties.setName(roadStation.getName());
        properties.addName("fi", roadStation.getNameFi());
        properties.addName("sv", roadStation.getNameSv());
        properties.addName("en", roadStation.getNameEn());

        if (roadStation.getRoadAddress() != null) {
            try {
                properties.setRoadAddress((RoadAddress)roadStation.getRoadAddress().clone());
            } catch (CloneNotSupportedException e) {
                log.error("Failed to clone RoadAddress", e);
            }
        }

        properties.setLiviId(roadStation.getLiviId());
        properties.setCountry(roadStation.getCountry());
        properties.setStartTime(roadStation.getStartDate());
        properties.setRepairMaintenanceTime(roadStation.getRepairMaintenanceDate());
        properties.setAnnualMaintenanceTime(roadStation.getAnnualMaintenanceDate());
        properties.setLocation(roadStation.getLocation());
        properties.setState(roadStation.getState());
        properties.setCoordinatesETRS89(getETRS89CoordinatesPoint(roadStation));
        properties.setPurpose(roadStation.getPurpose());
    }

    public static Point getETRS89CoordinatesPoint(RoadStation rs) {
        if (rs.getLatitude() != null && rs.getLongitude() != null) {
            if (rs.getAltitude() != null) {
                return new Point(
                        rs.getLongitude().doubleValue(),
                        rs.getLatitude().doubleValue(),
                        rs.getAltitude().doubleValue());
            } else {
                return new Point(
                        rs.getLongitude().doubleValue(),
                        rs.getLatitude().doubleValue());
            }
        }
        return null;
    }

    protected Point getGeometry(final RoadStation rs) {
        final Point etrS89 = getETRS89CoordinatesPoint(rs);
        if (etrS89 != null) {
            return CoordinateConverter.convertFromETRS89ToWGS84(etrS89);
        }
        return null;
    }

}
