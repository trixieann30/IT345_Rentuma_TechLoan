package edu.cit.rentuma.techloan.features.holiday;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NagerPublicHoliday {

    private String date;
    private String localName;
    private String name;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLocalName() { return localName; }
    public void setLocalName(String localName) { this.localName = localName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
