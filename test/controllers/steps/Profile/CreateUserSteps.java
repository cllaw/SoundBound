package controllers.steps.Profile;


import controllers.TestApplication;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import models.Profile;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * Implements steps for testing CreateUser
 */
public class CreateUserSteps {

    private Map<String, String> createForm = new HashMap<>();
    private Map<String, String> createFormSecond = new HashMap<>();
    private Map<String, String> createFormThird = new HashMap<>();
    private Result createResult;
    private Profile createdProfile;

    private final String USER_EMAIL = "james@johnston.com";
    private final String USER_EMAIL_ADMIN = "Sam@samson.com";

    @When("he enters the First Name {string}")
    public void enter_first_name(String firstName) {
        createForm.put("firstName", firstName);
    }

    @When("he enters the Middle Name {string}")
    public void enter_middle_name(String middleName) {
        createForm.put("middleName", middleName);
    }

    @When("he enters the Last Name {string}")
    public void enter_last_name(String lastName) {
        createForm.put("lastName", lastName);
    }

    @When("he enters the Email {string}")
    public void enter_email(String email) {
        createForm.put("email", email);
    }

    @When("he enters the Password {string}")
    public void enter_password(String password) {
        createForm.put("password", password);
    }

    @When("he enters the Gender {string}")
    public void enter_gender(String gender) {
        createForm.put("gender", gender);
    }

    @When("he enters the Birth date {string}")
    public void enter_DOB(String DOB) {
        createForm.put("birthDate", DOB);
    }

    @When("he enters the Nationalities {string}")
    public void enter_nationalities(String nat) {
        createForm.put("nationalitiesForm", nat);
    }

    @When("he enters the Passport {string}")
    public void enter_passports(String passport) {
        createForm.put("passportsForm", passport);
    }

    @When("he chooses {string} in Traveller Type")
    public void enter_traveller_type(String travellerType) {
        createForm.put("travellerTypesForm", travellerType);
    }


    @When("he submits")
    public void he_submits() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/user/create")
                .bodyForm(createForm)
                .session("connected", "1");
    }

    @Then("his account should be saved")
    public void saved_account() {
        TestApplication.getProfileRepository().lookupEmail("john.gherkin.doe@travelea.com").thenApplyAsync(profileOpt -> {
            profileOpt.ifPresent(profile -> {
                assertEquals("John", profile.getFirstName());
                assertEquals("Gherkin", profile.getMiddleName());
                assertEquals("Doe", profile.getLastName());
                assertEquals("john.gherkin.doe@travelea.com", profile.getEmail());
                assertEquals("password", profile.getPassword());
                assertEquals("Male", profile.getGender());
                assertEquals("01/04/2019", profile.getBirthString());
                assertEquals("New Zealand, China", profile.getNationalityString());
                assertEquals("New Zealand, China", profile.getPassportsString());
                assertEquals("Holidaymaker, Thrillseeker", profile.getTravellerTypesString());
            });
            return "done";
        });
    }

    @Given("^I am on the landing page$")
    public void iAmOnTheLandingPage() throws Throwable {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .uri("/login");
        Helpers.route(TestApplication.getApplication(), request);
    }

    @Given("^I am on the admin page$")
    public void iAmOnTheAdminPage() throws Throwable {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .uri("/admin");
        Helpers.route(TestApplication.getApplication(), request);
    }

    @When("^I press the create user button$")
    public void iPressTheCreateUserButton() throws Throwable {
        // Pass through as button opens modal
    }

    @And("^I enter \"([^\"]*)\" into the \"([^\"]*)\" field$")
    public void iEnterIntoTheField(String arg0, String arg1) throws Throwable {
        createFormSecond.put(arg1, arg0);
    }

    @And("^I enter \"([^\"]*)\" into the \"([^\"]*)\" admin field$")
    public void iEnterIntoTheAdminField(String arg0, String arg1) throws Throwable {
        createFormThird.put(arg1, arg0);
    }


    @And("^I enter \"([^\"]*)\", \"([^\"]*)\" into the \"([^\"]*)\" field$")
    public void iEnterIntoTheField(String arg0, String arg1, String arg2) throws Throwable {
        createFormSecond.put(arg2, arg0 + ","+ arg1);
    }

    @And("^I enter \"([^\"]*)\", \"([^\"]*)\" into the \"([^\"]*)\" admin field$")
    public void iEnterIntoTheAdminField(String arg0, String arg1, String arg2) throws Throwable {
        createFormThird.put(arg2, arg0 + ","+ arg1);
    }


    @Then("^I save my new profile$")
    public void iSaveMyNewProfile() throws Throwable {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/user/create")
                .bodyForm(createFormSecond);
        createResult = Helpers.route(TestApplication.getApplication(), request);
    }

    @Then("^admin saves the profile$")
    public void adminSavesTheProfile() throws Throwable {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/admin/profile/create")
                .bodyForm(createFormThird)
                .session("connected", "2");
        createResult = Helpers.route(TestApplication.getApplication(), request);
    }


    @And("^My user profile is saved in the database$")
    public void myUserProfileIsSavedInTheDatabase() throws Throwable {
        createdProfile = TestApplication.getProfileRepository().getProfileById(USER_EMAIL);
        assertNotNull(createdProfile);
    }

    @And("^The created profile is saved in the database$")
    public void theCreatedProfileIsSavedInTheDatabase() throws Throwable {
        createdProfile = TestApplication.getProfileRepository().getProfileById(USER_EMAIL_ADMIN);
        assertNotNull(createdProfile);
    }

    @And("^my passports are \"([^\"]*)\" or \"([^\"]*)\"$")
    public void myPassportsAre(String arg0, String arg1) throws Throwable {
        if(createdProfile != null) {
            if (createdProfile.getPassportsString() == arg0 || createdProfile.getPassportsString() == arg1) {
                assertTrue(true);
            }
        } else {
            fail();
        }
    }

    @And("^my nationalities are \"([^\"]*)\"$")
    public void myNationalitiesAre(String arg0) throws Throwable {
        if(createdProfile != null) {
            assertEquals(arg0, createdProfile.getNationalityString());
        } else {
            fail();
        }
    }
}
