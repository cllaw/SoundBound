package models;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import io.ebean.Finder;
import io.ebean.Model;
import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

//import org.mindrot.jbcrypt.BCrypt;

/**
 * This class holds the data for a profile
 */
@Entity
public class Profile extends Model {

    @Constraints.Required
    private String firstName;

    private String middleName;

    @Constraints.Required
    private String lastName;

    @Id
    @Constraints.Required
    private String email;

    private String password;

    @Formats.DateTime(pattern = "yyyy-MM-dd")
    private Date birthDate;

    @Constraints.Required
    private String gender;

    private String passports;

    @Constraints.Required
    private String nationalities;
    @Constraints.Required
    private String travellerTypes;

    private boolean admin;

    //@Formats.DateTime(pattern="dd-MM-yyyy")
    private Date timeCreated;

    private ArrayList<Destination> destinations = new ArrayList<>();
    private ArrayList<Trip> trips;
    TreeMultimap<Long, Integer> tripsMap = TreeMultimap.create();
    TreeMap <Integer, Trip> tripsTripMap = new TreeMap<>();
    //these booleans are chosen by the checkboxes, functions then create destinations (list of enums) from the booleans

    private static SimpleDateFormat dateFormatEntry = new SimpleDateFormat("YYYY-MM-dd");
    private static SimpleDateFormat dateFormatsort = new SimpleDateFormat("dd/MM/YYY");

    public Profile(String firstName, String lastName, String email, String password, Date birthDate,
                   String passports, String gender, Date timeCreated, String nationalities,
                   String travellerTypes, ArrayList<Trip> trips, boolean isAdmin) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.passports = passports;
        this.gender = gender;
        this.timeCreated = timeCreated;
        this.nationalities = nationalities;
        this.travellerTypes = travellerTypes;
        this.trips = trips;
        this.admin = isAdmin;
    }

    // Finder for profile
    public static final Finder<String, Profile> find = new Finder<>(Profile.class);

    //--------------Setters----------------------
    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public void setLastName(String name) {
        this.lastName = name;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public void setPassword(String password) {
        //Hash the password for added security
       // String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(WORKLOAD));
        this.password = password;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public void setAdmin(boolean isAdmin){
        this.admin = isAdmin;
    }
    public String getEntryDate() {
        return dateFormatEntry.format(birthDate);
    }

    //Getters
    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public String getPassword() {
        return password;
    }

    public String getGender() {
        return gender;
    }

    public String getTravellerTypes() {
        return travellerTypes;
    }


    public void setTravellerTypes(String travellerTypes) {
        this.travellerTypes = travellerTypes;
    }

    public ArrayList<String> getPassportsList() {
        return new ArrayList<>(Arrays.asList(passports.split(",")));
    }

    public ArrayList<String> getNationalityList() {
        return new ArrayList<>(Arrays.asList(nationalities.split(",")));
    }

    public ArrayList<String> getTravellerTypesList() {
        return new ArrayList<>(Arrays.asList(travellerTypes.split(",")));
    }

    public TreeMultimap<Long, Integer> getTrips() {
        return tripsMap;
    }

    public Trip getTripById(int tripId) {
        return tripsTripMap.get(tripId);
    }

    public void setTrips(TreeMultimap<Long, Integer> trips) {
        this.tripsMap = trips;
    }

    public void setTripMaps(TreeMap<Integer, Trip> trips) {
        this.tripsTripMap = trips;
    }

    public TreeMap<Integer, Trip> getTripsMap() {
        return tripsTripMap;
    }

    public ArrayList<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(ArrayList<Destination> destinations) {
        this.destinations = destinations;
    }


    /**
     * This method creates a formatted date string of the profiles birth date
     * @return the formatted date string
     */
    public String getBirthString() {
        return dateFormatsort.format(birthDate);
    }

    /**
     * format the traveller types
     * @return
     */
    public String getFormattedTravellerTypes() {
        ArrayList<String> types = getTravellerTypesList();
        if (types.size() <= 3) {
            return travellerTypes;
        } else {
            String typeString = types.get(0);
            for (int i = 1; i < types.size(); i++) {
                if (i % 3 == 0) {
                    typeString += "\n";
                }
                typeString += ", " + types.get(i);
            }

            return typeString;
        }
    }


    public boolean isAdmin() { return this.admin; }

    public void setPassports(String passports) {
        this.passports = passports;
    }

    public String getPassports() {
        return passports;
    }

    public String getNationalities() {
        return nationalities;
    }

    public void setNationalities(String nationalities) {
        this.nationalities = nationalities;
    }
}