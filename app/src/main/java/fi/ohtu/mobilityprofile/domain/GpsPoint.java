package fi.ohtu.mobilityprofile.domain;

import com.orm.SugarRecord;

/**
 * Class is used to save raw gps data.
 */
public class GpsPoint extends SugarRecord implements HasCoordinate, Comparable<GpsPoint> {
    long timestamp;
    float accuracy;
    Coordinate coordinate;

    /**
     *
     */
    public GpsPoint() {
        this.timestamp = 0;
        this.accuracy = 0;
        this.coordinate = new Coordinate(0f, 0f);
    }

    /**
     * Creates GpsPoint.
     * @param timestamp timestamp of the visit
     * @param accuracy accuracy of the gpsPoint location
     * @param latitude latitude of the location
     * @param longitude longitude of the location
     */
    public GpsPoint(long timestamp, float accuracy, Float latitude, Float longitude) {
        this.timestamp = timestamp;
        this.accuracy = accuracy;
        this.coordinate = new Coordinate(latitude, longitude);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getAccuracy() {
        return accuracy;
    }

    @Override
    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    public Float getLatitude() {
        return this.coordinate.getLatitude();
    }

    public Float getLongitude() {
        return this.coordinate.getLongitude();
    }

    @Override
    public String toString() {
        return "lat=" + " lon=";
    }

    @Override
    public double distanceTo(HasCoordinate hasCoordinate) {
        return this.coordinate.distanceTo(hasCoordinate.getCoordinate());
    }

    @Override
    public int compareTo(GpsPoint another) {
        long difference = timestamp - another.getTimestamp();
        if (difference < 0) {
            return -1;
        }
        if (difference > 0) {
            return 1;
        }
        return 0;
    }
}
