package edu.illinois.cs.forward.types;

/**
 * Geographical information container.
 */
public class Location {
    public double longitude;
    public double latitude;

    public Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
