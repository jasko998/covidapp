package com.turtleshelldevelopment.utils.db;

import java.util.Date;

public record Vaccine(String lotNumber, int site_id, int patient_id, Date administeredDate, String manufacturer, String dose,
                      int administeredBy, ProvidingUser givenBy, Site site) {
}
