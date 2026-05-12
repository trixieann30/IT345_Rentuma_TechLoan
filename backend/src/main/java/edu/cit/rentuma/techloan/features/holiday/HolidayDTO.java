package edu.cit.rentuma.techloan.features.holiday;

public class HolidayDTO {

    private String date;
    private String localName;
    private String name;

    public HolidayDTO(String date, String localName, String name) {
        this.date = date;
        this.localName = localName;
        this.name = name;
    }

    public String getDate() { return date; }
    public String getLocalName() { return localName; }
    public String getName() { return name; }
}
