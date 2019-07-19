package controllers;


import models.TreasureHunt;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;
import repository.DestinationRepository;
import repository.ProfileRepository;
import repository.TreasureHuntRepository;
import views.html.treasureHunts;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;


public class TreasureHuntController {

    private MessagesApi messagesApi;
    private final ProfileRepository profileRepository;
    private final DestinationRepository destinationRepository;
    private final Form<TreasureHunt> huntForm;
    private final TreasureHuntRepository treasureHuntRepository;

    /**
     * Constructor for the treasure hunt controller class
     *
     * @param messagesApi
     */
    @Inject
    public TreasureHuntController(FormFactory formFactory, MessagesApi messagesApi, ProfileRepository profileRepository, DestinationRepository destinationRepository, TreasureHuntRepository treasureHuntRepository) {
        this.messagesApi = messagesApi;
        this.profileRepository = profileRepository;
        this.destinationRepository = destinationRepository;
        this.huntForm = formFactory.form(TreasureHunt.class);
        this.treasureHuntRepository = treasureHuntRepository;
    }


    public CompletionStage<Result> show(Http.Request request) {
        Integer profId = SessionController.getCurrentUserId(request);
        return profileRepository.findById(profId).thenApplyAsync(profile -> {
            return profile.map(profile1 -> {
                return ok(treasureHunts.render(profile1, destinationRepository.getPublicDestinations(), huntForm, request, messagesApi.preferred(request)));
            }).orElseGet(() -> redirect("/login"));
        });
    }


    /**
     * Endpoint method to handle a users request to create a new treasure hunt
     *
     * @apiNote /hunts/create
     * @param request the users request holding the treasure hunt form
     * @return CompletionStage redirecting back to the treasure hunts page
     */
    public CompletionStage<Result> createHunt(Http.Request request) {
        return supplyAsync(() -> {
            return redirect("/treasure");
        });
    }



    /**
     * Called treasure hunt delete method in the treasureHuntRepository to delete the treasureHunt from the database
     *
     * @param id int Id of the treasureHunt the user wishes to delete
     */
    public CompletionStage<Result> deleteHunt(Http.Request request, Integer id){
        return treasureHuntRepository.deleteTreasureHunt(id, SessionController.getCurrentUserId(request))
                .thenApplyAsync(x -> redirect("/treasure").flashing("succsess", "Hunt: " + id + "was deleted"));
    }
}
