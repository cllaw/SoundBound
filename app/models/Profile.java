package models;

import io.ebean.Finder;
import io.ebean.Model;
//import org.mindrot.jbcrypt.BCrypt;
import play.data.format.Formats;
import play.data.validation.Constraints;
import javax.persistence.Entity;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.persistence.Id;
import javax.validation.Constraint;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.reverseOrder;


@Entity
public class Profile extends Model {

    //TODO Ordered trip list to display on the profile page

    //private static final long serialVersionUID = 1L;

    private static final int WORKLOAD = 12;

    @Constraints.Required
    private String firstName;

    private String middleName;

    @Constraints.Required
    private String lastName;

    @Id
    @Constraints.Required
    private String email;

    @Constraints.Required
    private String password;

    @Formats.DateTime(pattern = "yyyy-MM-dd")
    private Date birthDate;



    @Constraints.Required
    private String gender;

    private String passports;

    @Constraints.Required
    private String nationalities;

    private String travellerTypes;



    //@Formats.DateTime(pattern="dd-MM-yyyy")
    private Date timeCreated;

    private ArrayList<Destination> destinations = new ArrayList<Destination>();
    private ArrayList<Trip> trips = new ArrayList<>();

    //these booleans are chosen by the checkboxes, functions then create destinations (list of enums) from the booleans



    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
    private static SimpleDateFormat dateFormatSort = new SimpleDateFormat("dd/MM/YYYY");
    private static SimpleDateFormat dateFormatEntry = new SimpleDateFormat("YYYY-MM-dd");



    public Profile(String firstName, String lastName, String email, String password, Date birthDate,
                   String passports, String gender, Date timeCreated, String nationalities, ArrayList<Destination> destinations,
                   String travellerTypes, ArrayList<Trip> trips) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.passports = passports;
        this.gender = gender;
        this.timeCreated = timeCreated;
        this.nationalities = nationalities;
        this.destinations = destinations;
        this.travellerTypes = travellerTypes;
        this.trips = trips;
    }

    // Finder for profile
    public static final Finder<String, Profile> find = new Finder<>(Profile.class);

    //Setters
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


    public void setDestinations(ArrayList<Destination> destinations) {
        this.destinations = destinations;
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
        ArrayList<String> passportsList = new ArrayList<>(Arrays.asList(passports.split(",")));
        return passportsList;
    }

    public ArrayList<Destination> getDestinations() {
        return destinations;
    }

    public ArrayList<String> getNationalityList() {
        ArrayList<String> nationalityList = new ArrayList<>(Arrays.asList(nationalities.split(",")));
        return nationalityList;
    }

    public ArrayList<String> getTravellerTypesList() {
        ArrayList<String> travelerTypesList = new ArrayList<>(Arrays.asList(travellerTypes.split(",")));
        return travelerTypesList;
    }

    public ArrayList<Trip> getTrips() {
        return trips;
    }

    public void setTrips(ArrayList<Trip> trips) {
        this.trips = trips;
    }

    public boolean checkPassword(String password) {
        // TODO FIX THIS
        return true;
        //BCrypt.checkpw(password, this.password);
    }


    /**
     * Searches the users destinations for the given search term.
     *
     * @param searchTerm The term that will be searched for.
     * @return A arraylists of the destinations that contain the search term.
     */
    public ArrayList<Destination> searchDestinations(String searchTerm) {
        ArrayList<Destination> resultDestinations = new ArrayList<Destination>();
        for (Destination dest : destinations) {
            if (dest.getName().contains(searchTerm) || dest.getType().contains(searchTerm) || dest.getCountry().contains(searchTerm) || dest.getDistrict().contains(searchTerm)) {
                resultDestinations.add(dest);
            }
        }
        return resultDestinations;
    }

    /**
     * Returns a single destination.
     *
     * @param destinationID The id of the required destination.
     * @return The destination required.
     */
    public Destination returnDestination(int destinationID) {
        Destination toReturn = null;
        for (Destination dest : destinations) {
            if (dest.getDestinationId() == destinationID) {
                toReturn = dest;
                break;
            }
        }
        return toReturn;
    }



    /**
     * Delete a destination from the profile
     * @param destinationID ID of destination to delete
     */
    public void deleteDestination(int destinationID) {
        for(Destination dest : destinations) {
            if (dest.getDestinationId() == destinationID) {
                destinations.remove(dest);
                return;
            }
        }
    }

    /**
     * This method sorts the users current list of trips by date
     * @return the sorted
     */
    public void sortedTrips() {
        Map<Long, Integer> tripsMap = new TreeMap<Long, Integer>();
        for (int index = 0; index < trips.size(); index++) {
            tripsMap.put(trips.get(index).getTimeVal(), index);
        }
        List<Map.Entry<Long, Integer>> sortedMap =
                tripsMap.entrySet()
                        .stream()
                        .sorted(reverseOrder(Map.Entry.comparingByKey()))
                        .collect(Collectors.toList());

        ArrayList<Trip> sortedTrips = new ArrayList<>();
        for (Map.Entry<Long, Integer> tripEntry : sortedMap) {
            sortedTrips.add(trips.get(tripEntry.getValue()));
        }
        trips = sortedTrips;
    }

    /**
     * This method creates a formatted date string of the profiles birth date
     * @return the formatted date string
     */
    public String getBirthString() {
        return dateFormatSort.format(birthDate);
    }

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