package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.FlightStatus;

import java.util.Date;

public class ScheduledFlight {
    public Date departureDateTime;
    protected FlightStatus status;

    public Date getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(Date departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }
}
