package edu.illinois.cs.forward.types;

/**
 * Geographical information container.
 */
public class Location {
    // Based on that two points 1e-5 in longitude or latitude away are only 1 meter away.
    public static final double varianceFloatingError = 1e-10;

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
}
