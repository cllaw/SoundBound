package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Artist;
import models.ArtistCountry;
import models.ArtistProfile;
import models.PassportCountry;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ArtistRepository {


    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;
    private final PassportCountryRepository passportCountryRepository;


    /**
     * Ebeans injector constructor method for Artist repository.
     *
     * @param ebeanConfig The ebeans config which the ebean server will be supplied from
     * @param executionContext the database execution context object for this instance.
     */
    @Inject
    public ArtistRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext, PassportCountryRepository passportCountryRepository) {

        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
        this.passportCountryRepository = passportCountryRepository;


    }

    /**
     * Inserts an Artist object into the ebean database server
     * and checks if the selected countries are in the database,
     * if they are not, the country is added to the database and added to
     * the artist country linking table
     *
     * @param artist Artist object to insert into the database
     * @return the new Artist id
     */
    public CompletionStage<Integer> insert(Artist artist) {
        return supplyAsync(() -> {

            ebeanServer.insert(artist);

            // Adding artist countries to artist_country in the database
            for (String countryName : artist.getCountryList()) {
                PassportCountry country = ebeanServer.find(PassportCountry.class)
                        .where().eq("passport_name", countryName)
                        .findOne();

                if (country == null) { passportCountryRepository.insert(new PassportCountry(countryName)); }
                Optional<Integer> passportCountryId = passportCountryRepository.getPassportCountryId(countryName);
                supplyAsync(() -> {
                    ebeanServer.insert(new ArtistCountry(artist.getArtistId(), passportCountryId.get()));
                    return null;
                });
            }

            return artist.getArtistId();
        }, executionContext);
    }

    /** Checks if the artist to add is a duplicate of an existing artist
     *
     */
    public CompletionStage<Boolean> checkDuplicate(String artistName) {
        return supplyAsync(() -> {
            Artist artist = ebeanServer.find(Artist.class)
                    .where().eq("artist_name", artistName)
                    .findOne();
            return artist != null;
        }, executionContext);
    }

    /**
     * Inserts ArtistProfile object into the ebean database server for link table.
     *
     * @param artistProfile ArtistProfile object to insert into the database
     * @return the new Artist id
     */
    public CompletionStage<Integer> insertProfileLink(ArtistProfile artistProfile) {
        return supplyAsync(() -> {
            ebeanServer.insert(artistProfile);
            return artistProfile.getAPArtistId();
        }, executionContext);
    }


    /**
     * Method to return all of a users artists
     * @param userId, the id of the user
     * @return Artists, an ArrayList of all artists that user is a part of.
     */
    public List<Artist> getAllUserArtists(int userId) {

        List<ArtistProfile> artistProfiles = new ArrayList<>(ebeanServer.find(ArtistProfile.class)
                .where()
                //.eq("soft_delete", 0)
                .eq("profile_id", userId)
                .findList());

        List<Artist> artists = new ArrayList<>();
        for(ArtistProfile artistProfile : artistProfiles) {
            artists.add(ebeanServer.find(Artist.class)
                    .where()
                    .eq("soft_delete", 0)
                    .eq("artist_id", artistProfile.getAPArtistId())
                    .findOne());
        }
        return artists;
    }


    /**
     * Sets the artists approved flag to 1, this allows the artist to fully access the application
     *
     * @param artistId Id of the artist to approve
     * @return Void completion stage
     */
    public CompletionStage<Void> setArtistAsVerified(int artistId) {
        return supplyAsync(() -> {
            ebeanServer.update(Artist.class).set("verified", 1).where().eq("artist_id", Integer.toString(artistId)).update();
            return null;
        });
    }


    /**
     * Removes an artist entry from the database using a passed artist id
     *
     * @param artistId Id of the artist to delete
     * @return Void completion stage
     */
    public CompletionStage<Void> deleteArtist(int artistId) {
        return supplyAsync(() -> {
            ebeanServer.find(Artist.class).where().eq("artist_id", Integer.toString(artistId)).delete();
            return null;
        });
    }


    /**
     * Inserts a new entry into the artist_profile table linking the profile to the artist
     *
     * @param artistId id of artist to link
     * @param profileId id of profile to link
     * @return Void CompletionStage
     */
    public CompletionStage<Void> addProfileToArtist(int artistId, int profileId) {
        return supplyAsync(() -> {
            ebeanServer.insert(new ArtistProfile(artistId, profileId));
            return null;
        });
    }


    /**
     * Removes an entry from the artist_profile table unlinking a profile from an artist
     *
     * @param artistId id of artist to link
     * @param profileId id of profile to link
     * @return Void CompletionStage
     */
    public CompletionStage<Void> removeProfileFromArtist(int artistId, int profileId) {
        return supplyAsync(() -> {
            ebeanServer.find(ArtistProfile.class)
                    .where()
                    .eq("artist_id", artistId)
                    .eq("profile_id", profileId)
                    .delete();
            return null;
        });
    }

    /**
     * Method to get all artist profiles that have not yet been verified so the admin can either verify or remove the
     * profile
     * @return
     */
    public List<Artist> getInvalidArtists(){
        return new ArrayList<>(ebeanServer.find(Artist.class)
                .where().eq("verified", 0).findList());
    }

    public List<PassportCountry> getArtistCounties(int artistId) {
        return new ArrayList<>(ebeanServer.find(PassportCountry.class)
            .where().eq("artist_id", artistId).findList());
    }
}
