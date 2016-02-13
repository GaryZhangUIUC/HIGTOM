package edu.illinois.cs.forward.types;

/**
 * Geographical information container.
 */
public class Location {
    public double longitude;
    public double latitude;

    public double longitudeVariance;
    public double latitudeVariance;
    public double longitudeLatitudeCovariance;

    public Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;

        this.longitudeVariance = 0.0;
        this.latitudeVariance = 0.0;
        this.longitudeLatitudeCovariance = 0.0;
    }

    public double locationProbability(Location target) {
        return 0.0;
    }
}
