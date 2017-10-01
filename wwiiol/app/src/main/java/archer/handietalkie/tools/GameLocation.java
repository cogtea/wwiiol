package archer.handietalkie.tools;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ramy on 9/29/17.
 * <p>
 * http://wiretap.wwiionline.com/wwiiol-latlon.js
 * DESCRIPTION:
 * Provides two functions that return LatLon objects:
 * getLatLonFromMeterXY(x,y)
 * getLatLonFromOctetXY(ox, oy)
 * <p>
 * Most strat-related coordinates are provided in octets; octets are
 * 800x800m (game meters). Since the game uses half x/y scale,
 * these represent 1600x1600m real-world.
 */

public class GameLocation {


    // Get a LatLon from octet coordinates
    public static LatLng getLatLonFromOctetXY(double ox, double oy) {
        // Octets are squares 800x800m
        double x = ox * 800;
        double y = oy * 800;
        // Use the center of the octet ;
        if (x >= 0) x = x + 400;
        else x = x - 400;
        if (y >= 0) y = y + 400;
        else y = y - 400;
        return new LatLon(x, y).getLatLng();
    }

    public static class LatLon {
        private double glat, glon;

        LatLon(double x, double y) {
            // Get the lat
            double WORLD_Y = 4211200;
            double ARC_Y = 70.1875;
            this.glat = y / WORLD_Y * ARC_Y;

            // Get the lng
            double WORLD_X = 8640000;
            double ARC_X = 180.0;
            this.glon = x / WORLD_X * ARC_X;
        }

        public double getGlat() {
            return glat;
        }

        public double getGlon() {
            return glon;
        }

        public LatLng getLatLng() {
            return new LatLng(getGlat(), getGlon());
        }

        @Override
        public String toString() {
            return "Lat: " + getGlat() + ", Lon: " + getGlon();
        }
    }
}
