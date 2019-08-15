package models;

import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Events {

    @Id
    private int eventId;

    @Constraints.Required
    private String eventName;

    private String description;

    @Constraints.Required
    private int destinationId;

    @Formats.DateTime(pattern = "yyyy-MM-dd")
    private Date startDate;

    @Formats.DateTime(pattern = "yyyy-MM-dd")
    private Date endDate;

    private int ageRestriction;

    /**
     * Traditional constructor for events used when retrieving an Event from the data base
     * @param eventId id of the event (Primary Key)
     * @param eventName String: name of the event
     * @param description String: description of what the event is
     * @param destinationId int: id of the destination where the event is (FK)
     * @param startDate Date: start date of te event
     * @param endDate Date: end date of the event
     * @param ageRestriction int: minimum age restriction of the event
     */
    public Events(int eventId, String eventName, String description, int destinationId, Date startDate, Date endDate,
                  int ageRestriction){
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.destinationId = destinationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ageRestriction = ageRestriction;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }
}
