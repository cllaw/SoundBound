package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Destination extends Model {

    @Id
    @Constraints.Required
    private int destinationId;
    private int userId;
    @Constraints.Required
    private String name;
    @Constraints.Required
    private String type;
    @Constraints.Required
    private String country;

    private String district;

    private double latitude;

    private double longitude;

    public Destination() {

    }

    /**
     * Create a destination

     * @param name The name of this destination
     * @param type The type this destination is
     * @param country The country this destination is from
     * @param district The district this destination is from
     * @param latitude The latitude of this destination
     * @param longitude the longitude of this destination
     */
    public Destination(int destinationId, int userId, String name, String type, String country, String district, double latitude, double longitude) {
        this.destinationId = destinationId;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.country = country;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Finder for destination
    public static final Finder<String, Destination> find = new Finder<>(Destination.class);

    /**
     * Returns the destination ID
     * @return
     */
    public int getDestinationId() {
        return destinationId;
    }


    /**
     * Returns the name of the destination
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the destination
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of the destination
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the destination
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the country of the destination
     * @return
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country of the destination
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns the district of the destination
     * @return
     */
    public String getDistrict() {
        return district;
    }

    /**
     * Sets the district of the destination
     * @param district
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * Returns the latitude of the destination
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude of the destination
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the longitude of the destination
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude of the destination
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
