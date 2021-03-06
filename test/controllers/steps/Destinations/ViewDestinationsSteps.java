package controllers.steps.Destinations;

import controllers.TestApplication;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.util.HashMap;
import java.util.Map;

public class ViewDestinationsSteps {
    private Map<String, String> loginForm = new HashMap<>();
    private Map<String, String> destForm = new HashMap<>();
    private Result redirectDestination;


    @When("User clicks on public destinations")
    public void userClicksOnPublicDestinations() {
        Http.RequestBuilder requestDest = Helpers.fakeRequest()
                .method("GET")
                .uri("/destinations/show/true/0")
                .session("connected", "1");
        Result destinationResult = Helpers.route(TestApplication.getApplication(), requestDest);
        Assert.assertEquals(200, destinationResult.status());
    }

    @When("Signs up to a public destination {string}")
    public void signsUpToAPublicDestination(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @When("User clicks on private destinations")
    public void userClicksOnPrivateDestinations() {
        Http.RequestBuilder requestDest = Helpers.fakeRequest()
                .method("GET")
                .uri("/destinations/show/false/0")
                .session("connected", "1");
        Result destinationResult = Helpers.route(TestApplication.getApplication(), requestDest);
        Assert.assertEquals(200, destinationResult.status());
    }

    @Then("Private destinations contains {string}")
    public void willBeShownInTheirDestinationsList(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }
}
