package controllers;

import models.PaginationHelper;
import models.PartnerFormData;
import models.Photo;
import models.Profile;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import repository.PersonalPhotoRepository;
import repository.ProfileRepository;
import repository.UndoStackRepository;
import utility.Country;
import views.html.travellers;
import views.html.travellersPhotos;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;


/**
 * This class is the controller for the travellers.scala.html file, it provides the route to the
 * travellers page
 */
public class TravellersController extends Controller {


    private final Form<PartnerFormData> form;
    private MessagesApi messagesApi;
    private final PersonalPhotoRepository personalPhotoRepository;
    private final ProfileRepository profileRepository;
    private final UndoStackRepository undoStackRepository;
    private List<Photo> photoList = new ArrayList<>();

    @Inject
     public TravellersController(FormFactory formFactory, MessagesApi messagesApi,
                                 PersonalPhotoRepository personalPhotoRepository, ProfileRepository profileRepository,
                                 UndoStackRepository undoStackRepository) {
        this.form = formFactory.form(PartnerFormData.class);
        this.messagesApi = messagesApi;
        this.personalPhotoRepository = personalPhotoRepository;
        this.profileRepository = profileRepository;
        this.undoStackRepository = undoStackRepository;
    }


    /**
     * Function to search travellers n gender nationality, age and traveller type fields, calls search functions for
     * each field
     * @param request http request
     * @param offset, the index at which to get the first traveller (ie 0 = start of first page)
     * @return renders traveller view with queried list of travellers
     */
    @Security.Authenticated(SecureSession.class)
    public CompletionStage<Result> search(Http.Request request, Integer offset){
        Integer profId = SessionController.getCurrentUserId(request);
        return profileRepository.findById(profId).thenApplyAsync(profile -> {
            if (profile.isPresent()) {
                Form<PartnerFormData> searchForm = form.bindFromRequest(request);
                PartnerFormData formData = searchForm.get();
                Date lowerDate;
                Date upperDate;

                // Show error if no search filters are filled, prevent needless search operation
                if (formData.searchAgeRange == null && formData.searchGender.equals("") &&
                        formData.searchNationality.equals("") && formData.searchTravellerTypes.equals("")) {
                    return redirect("/travellers/0").flashing("error", "Please enter at least one search filter");
                }

                if (formData.searchAgeRange == null) {
                    formData.setSearchAgeRange(0);
                }
                switch (formData.searchAgeRange) {
                    case 1:
                        lowerDate = getDateFromAge(0);
                        upperDate = getDateFromAge(18);
                        break;
                    case 2:
                        lowerDate = getDateFromAge(18);
                        upperDate = getDateFromAge(25);
                        break;
                    case 3:
                        lowerDate = getDateFromAge(25);
                        upperDate = getDateFromAge(35);
                        break;
                    case 4:
                        lowerDate = getDateFromAge(35);
                        upperDate = getDateFromAge(50);
                        break;
                    case 5:
                        lowerDate = getDateFromAge(50);
                        upperDate = getDateFromAge(65);
                        break;
                    case 6:
                        lowerDate = getDateFromAge(65);
                        upperDate = getDateFromAge(500);
                        break;
                    default:
                        lowerDate = getDateFromAge(0);
                        upperDate = getDateFromAge(500);
                        break;
                }

                List<Profile> searchedProfiles = profileRepository.searchProfiles(formData.searchTravellerTypes, lowerDate, upperDate, formData.searchGender, formData.searchNationality, offset);
                int sizeOfSearchResults = profileRepository.getNumSearchProfiles(formData.searchTravellerTypes, lowerDate, upperDate, formData.searchGender, formData.searchNationality);

                PaginationHelper paginationHelper = new PaginationHelper(offset, offset, offset, true, true, sizeOfSearchResults);
                paginationHelper.alterNext(12);
                paginationHelper.alterPrevious(12);
                paginationHelper.checkButtonsEnabled();

                return ok(travellers.render(form, searchedProfiles, photoList, profile.get(), Country.getInstance().getAllCountries(), formData, paginationHelper, request, messagesApi.preferred(request)));
            } else {
                return redirect("/travellers");
            }
        });
    }

    /**
     * Calculates a given date that corresponds to an age
     * @param age the age to find a date for
     * @return the calculated date
     */
    private Date getDateFromAge(int age) {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.YEAR, -age);
        return calendar.getTime();
    }


    /**
     * Method to retrieve all uploaded profile images from the database for a particular traveller
     * @param profileId id for a particular user you want to view the photos of
     * @return
     */
    private List<Photo> getTravellersPhotos(int profileId) {
        personalPhotoRepository.getAllProfilePhotos(profileId).ifPresent(photos -> photoList = photos);
        return photoList;
    }


    /**
     * this method shows the travellers photos on a new page
     * @param profileId id for a particular user you want to displays the images for
     * @return render traveller photo page
     */
    @Security.Authenticated(SecureSession.class)
    public CompletionStage<Result> displayTravellersPhotos(Http.Request request, Integer profileId) {
        Integer profId = SessionController.getCurrentUserId(request);
        return profileRepository.findById(profId).thenApplyAsync(profile -> {
            if (profile.isPresent()) {
                List<Photo> displayPhotoList = getTravellersPhotos(profileId);
                return ok(travellersPhotos.render(displayPhotoList, profile.get(), profileRepository.getExistingProfileByProfileId(profileId), request, messagesApi.preferred(request)));
            } else {
                return redirect("/travellers");
            }
        });
    }


    /**
     * This method shows the travellers page on the screen
     * @return result of the rendering of the travellers page
     */
    @Security.Authenticated(SecureSession.class)
    public CompletionStage<Result> show(Http.Request request, Integer offset) {
        Integer profId = SessionController.getCurrentUserId(request);
        PaginationHelper paginationHelper = new PaginationHelper(offset, offset, offset, true, true, profileRepository.getNumProfiles());
        paginationHelper.alterNext(12);
        paginationHelper.alterPrevious(12);
        paginationHelper.checkButtonsEnabled();

        return profileRepository.findById(profId).thenApplyAsync(profile -> {
            if (profile.isPresent()) {
                undoStackRepository.clearStackOnAllowed(profile.get());

                List<Profile> profiles = profileRepository.getAllTravellersPaginate(offset);
                return ok(travellers.render(form, profiles, photoList, profile.get(), Country.getInstance().getAllCountries(), new PartnerFormData(), paginationHelper, request, messagesApi.preferred(request)));
            } else {
                return redirect("/profile");
            }
        });
    }
}