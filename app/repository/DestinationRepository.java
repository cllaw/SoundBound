package repository;

import io.ebean.*;
import models.*;
import play.db.ebean.EbeanConfig;
import play.db.ebean.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;


/**
 * A destination repository that executes database operations in a different
 * execution context handles all interactions with the destination table .
 */
public class DestinationRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;
    private final RolesRepository rolesRepository;
    private final TravellerTypeRepository travellerTypeRepository;


    /**
     * A Constructor which links to the ebeans database
     *
     * @param ebeanConfig
     * @param executionContext
     */
    @Inject
    public DestinationRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext, RolesRepository roleRepository, TravellerTypeRepository travellerTypeRepository) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
        this.rolesRepository = roleRepository;
        this.travellerTypeRepository = travellerTypeRepository;
    }

    /**
     * Returns a specific destination
     *
     * @param destID The ID of the destination to return
     * @return
     */
    public Destination lookup(int destID) {
        return ebeanServer.find(Destination.class).setId(destID).findOne();
    }

    /**
     * Get the users destination list
     *
     * @param id the id of the user profile
     * @return destinations, list of all user destinations
     */
    public ArrayList<Destination> getUserDestinations(int id) {
        return new ArrayList<>(Destination.find.query()
                .where()
                .eq("profile_id", id)
                .findList());
    }

    /**
     * Get the all of the public destinations
     *
     * @return destinations, list of all public destinations
     */
    public ArrayList<Destination> getPublicDestinations() {
        return new ArrayList<>(Destination.find.query()
                .where()
                .eq("visible", 1)
                .findList());
    }


    /**
     * Inserts a new destination to the database.
     *
     * @param dest The destination to insert
     * @return
     */
    public CompletionStage<Optional<Integer>> insert(Destination dest) {
        return supplyAsync(() -> {
            ebeanServer.insert(dest);
            return Optional.of(dest.getDestinationId());
        }, executionContext);
    }

    /**
     * Deletes a destination from the database
     *
     * @param destID The ID of the destination to delete
     * @return
     */
    public CompletionStage<Optional<String>> delete(int destID) {
        return supplyAsync(() -> {
            try {
                final Optional<Destination> destinationOptional = Optional.ofNullable(ebeanServer.find(Destination.class)
                        .setId(destID).findOne());
                destinationOptional.ifPresent(Model::delete);
                return Optional.of(String.format("Destination %s deleted", destinationOptional.map((Destination p) -> p.getName())));
            } catch (Exception e) {
                return Optional.empty();
            }
        }, executionContext);
    }

    /**
     * Updates a destination in the database
     *
     * @param newDestination The new info to change the destination to
     * @param Id             The ID of the destination to editDestinations
     * @return
     */
    public CompletionStage<Optional<Integer>> update(Destination newDestination, Integer Id) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Integer> value = Optional.empty();
            try {
                Destination targetDestination = ebeanServer.find(Destination.class).setId(Id).findOne();
                if (targetDestination != null) {
                    targetDestination.setName(newDestination.getName());
                    targetDestination.setType(newDestination.getType());
                    targetDestination.setCountry(newDestination.getCountry());
                    targetDestination.setDistrict(newDestination.getDistrict());
                    targetDestination.setLatitude(newDestination.getLatitude());
                    targetDestination.setLongitude(newDestination.getLongitude());
                    targetDestination.setVisible(newDestination.getVisible());
                    targetDestination.setTravellerTypes(newDestination.getTravellerTypes());
                    targetDestination.update();
                    txn.commit();
                    value = Optional.of(targetDestination.getDestinationId());
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }


    /**
     * Update function to change only the profileId of a destination since the other update cannot handle this
     * Preconditions: The newDestinations profileId is a valid profileId
     *
     * @param newDestination
     * @param destinationId
     * @return
     */
    public Optional<Integer> updateProfileId(Destination newDestination, Integer destinationId) {
        Transaction txn = ebeanServer.beginTransaction();
        Optional<Integer> value = Optional.empty();
        try {
            Destination targetDestination = ebeanServer.find(Destination.class).setId(destinationId).findOne();
            if (targetDestination != null) {
                targetDestination.setProfileId(newDestination.getProfileId());
                targetDestination.update();
                txn.commit();
                value = Optional.of(destinationId);
            }
        } finally {
            txn.end();
        }
        return value;
    }


    /**
     * class to check if destination is already available to user
     * return true if already in else false
     */
    public boolean checkValid(Destination destination, int id) {
        Destination destinations = (Destination.find.query()
                .where()
                .eq("name", destination.getName())
                .eq("type", destination.getType())
                .eq("country", destination.getCountry())
                .eq("profile_id", id)
                .findOne());
        Destination publicDestinations = (Destination.find.query()
                .where()
                .eq("name", destination.getName())
                .eq("type", destination.getType())
                .eq("country", destination.getCountry())
                .eq("visible", "1")
                .findOne());
        return publicDestinations != null || destinations != null;
    }

    /**
     * Checks to see if a user has any destinations that are the same as the destination1 passed in
     *
     * @param destination1 the destination
     * @return Optional destination list, if there is a destination the same as destination1 then that destination will be
     * returned
     */
    public Optional<List<Destination>> checkForSameDestination(Destination destination1) {
        List<Destination> destinations = (Destination.find.query()
                .where()
                .eq("name", destination1.getName())
                .eq("type", destination1.getType())
                .eq("country", destination1.getCountry())
                .findList());
        return Optional.of(destinations);
    }

    /**
     * Method to follow a destination for a user
     *
     * @param destId    Id of the entered destination
     * @param profileId Id of the entered profile
     * @return Optional array of integers of the followed users id
     */
    public Optional<ArrayList<Integer>> followDestination(int destId, int profileId) {
        String updateQuery = "INSERT into follow_destination(profile_id, destination_id) values (?, ?)";
        SqlUpdate query = Ebean.createSqlUpdate(updateQuery);
        query.setParameter(1, profileId);
        query.setParameter(2, destId);
        query.execute();
        setOwnerAsAdmin(destId);
        return getFollowedDestinationIds(profileId);
    }

    /**
     * Method to allow a user to unfollow a given destination
     *
     * @param destId    Id of the destination to be unfollowed
     * @param profileId Id of the user that wants to unfollow a destination
     * @return Optional list of integers for the followed destination ids
     */
    public Optional<ArrayList<Integer>> unfollowDestination(int destId, int profileId) {
        String updateQuery = "DELETE from follow_destination where profile_id = ? and destination_id =  ?";
        SqlUpdate query = Ebean.createSqlUpdate(updateQuery);
        query.setParameter(1, profileId);
        query.setParameter(2, destId);
        query.execute();
        return getFollowedDestinationIds(profileId);
    }

    /**
     * Checks to see if destination is owned by an admin, if true this is its first follower, will change
     * ownership to admins and set the previous owner to follow destination
     *
     * @param destId the id of the destination
     */
    private void setOwnerAsAdmin(int destId) {
        Destination destination = lookup(destId);
        int profileId = destination.getProfileId();
        Optional<Integer> optionalAdminId = rolesRepository.getIdFromRole("global_admin");
        if (optionalAdminId.isPresent()) {
            int adminId = optionalAdminId.get();
            if (destination.getProfileId() != adminId) {
                destination.setProfileId(adminId);
                updateProfileId(destination, destination.getDestinationId());
                followDestination(destination.getDestinationId(), profileId);
            }
        }
    }

    /**
     * Method returns all of the users followed destinations
     *
     * @param profileId User if of the followed destinations to return
     * @return Optional array list of destinations followed by the user
     */
    public Optional<ArrayList<Destination>> getFollowedDestinations(int profileId) {
        String updateQuery = "Select D.destination_id, D.profile_id, D.name, D.type, D.country, D.district, D.latitude, D.longitude, D.visible " +
                "from follow_destination JOIN destination D on follow_destination.destination_id = D.destination_id where follow_destination.profile_id = ?";
        List<SqlRow> rowList = ebeanServer.createSqlQuery(updateQuery).setParameter(1, profileId).findList();
        ArrayList<Destination> destList = new ArrayList<>();
        Destination destToAdd;
        for (SqlRow aRowList : rowList) {
            destToAdd = new Destination();
            destToAdd.setDestinationId(aRowList.getInteger("destination_id"));
            destToAdd.setProfileId(aRowList.getInteger("profile_id"));
            destToAdd.setName(aRowList.getString("name"));
            destToAdd.setType(aRowList.getString("type"));
            destToAdd.setCountry(aRowList.getString("country"));
            destToAdd.setDistrict(aRowList.getString("district"));
            destToAdd.setLatitude(aRowList.getDouble("latitude"));
            destToAdd.setLongitude(aRowList.getDouble("longitude"));
            destToAdd.setVisible(aRowList.getBoolean("visible") ? 1 : 0);
            destList.add(destToAdd);
        }
        return Optional.of(destList);
    }




    /**
     * Method returns all followed destinations ids from a user
     *
     * @param profileId User id for the user followed destinations
     * @return Optional array list of integers of the followed destination ids
     */
    public Optional<ArrayList<Integer>> getFollowedDestinationIds(int profileId) {
        String updateQuery = "Select destination_id from follow_destination where profile_id = ?";
        List<SqlRow> rowList = ebeanServer.createSqlQuery(updateQuery).setParameter(1, profileId).findList();
        ArrayList<Integer> destIdList = new ArrayList<>();
        for (SqlRow aRowList : rowList) {
            int id = aRowList.getInteger("destination_id");
            destIdList.add(id);
        }
        return Optional.of(destIdList);
    }

    /**
     * class to check if destination is already available to user
     * return true if already in else false
     */
    public boolean checkValidEdit(Destination destination, int profileId, Destination previousDestination) {
        List<Destination> destinations = (Destination.find.query()
                .where()
                .eq("name", destination.getName())
                .eq("type", destination.getType())
                .eq("country", destination.getCountry())
                .eq("profile_id", profileId)
                .findList());

        List<Destination> publicDestinations = (Destination.find.query()
                .where()
                .eq("name", destination.getName())
                .eq("type", destination.getType())
                .eq("country", destination.getCountry())
                .eq("visible", 1)
                .findList());

        if(previousDestination != null){
            if(!destinations.isEmpty()){
                if(destinations.get(0).getName().equals(previousDestination.getName()) && destinations.get(0).getType().equals(previousDestination.getType()) && destinations.get(0).getCountry().equals(previousDestination.getCountry())) {
                    return false;
                }
            }
        }

        return !destinations.isEmpty() || !publicDestinations.isEmpty();
    }

    /**
     * Method called from addRequest method to add the changes made in a request to the actions table
     * @param destinationChanges Object that holds the following attributes to be inserted into the database:
     *   travellerTypeId: Id of the traveller type the user wants to add or remove.
     *   action: tinyInt 1 if the user wants to add traveller type, 0 if user wants to remove traveller type.
     *   requestId: Integer id of the request the user is making, links the changes to a request.
     * @return Integer CompletionStage of the id from the new change after the change is inserted into the
     *  destination_changes table
     */
    private CompletionStage<Integer> addDestinationChange(DestinationChanges destinationChanges){
        return supplyAsync(() -> {
            ebeanServer.insert(destinationChanges);
            return destinationChanges.getId();
        }, executionContext);
    }


    /**
     * Method to remove the traveller type destination request from the destination changes database table
     * @param changeId the database id of the change to delete
     * @return completion stage
     */
    public CompletionStage<Integer> deleteDestinationChange(int changeId) {
    return supplyAsync(
        () -> {
          ebeanServer.find(DestinationChanges.class).where().eq("id", changeId).delete();
          return 1;
        });
    }

    /**
     * Accept destination change request
     * calls add traveller type method if the request is to add or calls remove traveller type method if the request is
     * to remove traveller type
     * @param destinationChanges the destination change to be performed
     */
    public CompletionStage<Integer> acceptDestinationChange(int changeId) {
        return getDestinationChange(changeId)
                .thenApplyAsync(changeOpt -> {
                    if (changeOpt.isPresent()) {
                        // TODO: 19/07/19 might need to have add/remove methods chain return
                        System.out.println(changeOpt.get().getDestination());
                        if (changeOpt.get().getAction() == 1){
                           addDestinationTravellerType(changeOpt.get().getTravellerTypeId(), changeOpt.get().getDestination().getDestinationId());
                        } else {
                           removeDestinationTravellerType(changeOpt.get().getTravellerTypeId(), changeOpt.get().getDestination().getDestinationId());
                        }
                    }
                    return 1;
                })
                .thenApplyAsync(x -> {
                    deleteDestinationChange(changeId);
                    return 1;
                });

    }

    /**
     * Helper function to wrap Destination changes in a transaction
     * @param requestId
     * @param toAdd Boolean true if the traveller type is to be added
     *              False if traveller type is to be removed
     * @param changes List of changes to be wrapped in a transaction
     */
    @Transactional
    public void travellerTypeChangesTransaction(Integer requestId, Integer toAdd, List<Integer> changes){
        try (Transaction transaction = ebeanServer.beginTransaction()) {
            for (Integer travellerTypeId : changes) {
                DestinationChanges destinationChanges = new DestinationChanges(travellerTypeId, toAdd, requestId);
                addDestinationChange(destinationChanges);
            }
            transaction.commit();
        }
    }

    /**
     * Method to lodge a traveller type change request
     * Inserts request into the destination_request Table
     *
     * @param  destinationRequest object holding destinationId and profileId required for inserting the change into the
     *                            changes table
     */
    public CompletionStage<Integer> createDestinationTravellerTypeChangeRequest(DestinationRequest destinationRequest){
        return supplyAsync(() -> {
            ebeanServer.insert(destinationRequest);
            return destinationRequest.getId();
        }, executionContext);
    }


    /**
     * Update method to add traveller types to a destination
     *
     * @param travellerTypeId id of the traveller type that will be added to the destination
     * @param destinationId id of the destination that the traveller type will be added to
     */
    private CompletionStage<Void> addDestinationTravellerType(int travellerTypeId, int destinationId){
        DestinationTravellerType destinationTravellerType = new DestinationTravellerType(destinationId, travellerTypeId);
        return supplyAsync(() -> {
            ebeanServer.insert(destinationTravellerType);
            return null;
        }, executionContext);
    }

    /**
     * Update method to remove traveller type on a destination
     *
     * @param travellerTypeId id of the traveller type that will be added to the destination
     * @param destinationId id of the destination that the traveller type will be added to
     */
    private CompletionStage<Void> removeDestinationTravellerType(int travellerTypeId, int destinationId){
        return supplyAsync(() -> {
            ebeanServer
                    .find(DestinationTravellerType.class)
                    .where()
                    .eq("destinationId", destinationId)
                    .eq("travellerTypeId", travellerTypeId)
                    .delete();
            return null;
        });
    }


    /**
     * Method to get all destinationChanges with content such as email, destination and travellerTypes
     * @return result, a list of destinationChanges
     */
    public List<DestinationChanges> getAllDestinationChanges() {

                //Getting Destinationchanges out of the database
                List<DestinationChanges > result = DestinationChanges.find.query().where()
                        .findList();

            for (DestinationChanges destinationchanges : result) {
                DestinationRequest destinationRequest = DestinationRequest.find.query().where()
                        .eq("id", destinationchanges.getRequestId())
                        .findOne();

                Profile profile = Profile.find.query().where()
                        .eq("profile_id", destinationRequest.getProfileId())
                        .findOne();
                destinationchanges.setEmail(profile.getEmail());

                Destination destination = lookup(destinationRequest.getDestinationId());
                destinationchanges.setDestination(destination);

                TravellerType travellerType = TravellerType.find.query().where()
                        .eq("traveller_type_id", destinationchanges.getTravellerTypeId())
                        .findOne();
                destinationchanges.setTravellerType(travellerType);
            }
            return result;
    }

    public List<TravellerType> getDestinationsTravellerTypes(int destinationId) {
        String sql = "select traveller_type_id from destination_traveller_type where destination_id = ?";
        List<SqlRow> rowList = ebeanServer.createSqlQuery(sql).setParameter(1, destinationId).findList();
        if (rowList.isEmpty()) {
            return new ArrayList<>();
        } else {
            ArrayList<TravellerType> travellerTypes = new ArrayList<>();
            for (SqlRow row : rowList) {
                int id = row.getInteger("traveller_type_id");
                TravellerType travellerType = travellerTypeRepository.getById(id);
                if (!travellerTypes.contains(travellerType)) {
                    travellerTypes.add(travellerType);
                }
            }
            return travellerTypes;
        }
    }

    /**
     * Method used to get a DestinationChanges object from the database using a passed id
     *
     * @param changeId the id of the change to retrieve
     * @return CompletionStage containing the found DestinationChanges
     */
    public CompletionStage<Optional<DestinationChanges>> getDestinationChange(int changeId) {
    // TODO: 19/07/19 need to get the Destination object out too
        return supplyAsync(
            () -> {
              return Optional.ofNullable(
                  ebeanServer.find(DestinationChanges.class).where().eq("id", changeId).findOne());
            },
            executionContext);
        }

}