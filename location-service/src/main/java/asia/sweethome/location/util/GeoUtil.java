package asia.sweethome.location.util;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/14/2026 2:57 PM
 */
public class GeoUtil {

    public static final double EARTH_RADIUS_METERS = 6371000.0;

    public static double distanceMeters(
            double lng1,
            double lat1,
            double lng2,
            double lat2
    ) {

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;

    }

}
