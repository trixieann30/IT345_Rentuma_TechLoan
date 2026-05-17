package edu.cit.rentuma.techloan.features.penalty.dto;

import java.time.LocalDateTime;

public class AdminPenaltyDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String itemName;
    private Integer daysOverdue;
    private Integer penaltyPoints;
    private Boolean paid;
    private LocalDateTime calculatedAt;

    public AdminPenaltyDTO() {}

    public AdminPenaltyDTO(Long id, Long userId, String userName, String userEmail,
                           String itemName, Integer daysOverdue, Integer penaltyPoints,
                           Boolean paid, LocalDateTime calculatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.itemName = itemName;
        this.daysOverdue = daysOverdue;
        this.penaltyPoints = penaltyPoints;
        this.paid = paid;
        this.calculatedAt = calculatedAt;
    }

    public Long getId()                                    { return id; }
    public void setId(Long id)                             { this.id = id; }
    public Long getUserId()                                { return userId; }
    public void setUserId(Long userId)                     { this.userId = userId; }
    public String getUserName()                            { return userName; }
    public void setUserName(String userName)               { this.userName = userName; }
    public String getUserEmail()                           { return userEmail; }
    public void setUserEmail(String userEmail)             { this.userEmail = userEmail; }
    public String getItemName()                            { return itemName; }
    public void setItemName(String itemName)               { this.itemName = itemName; }
    public Integer getDaysOverdue()                        { return daysOverdue; }
    public void setDaysOverdue(Integer daysOverdue)        { this.daysOverdue = daysOverdue; }
    public Integer getPenaltyPoints()                      { return penaltyPoints; }
    public void setPenaltyPoints(Integer penaltyPoints)    { this.penaltyPoints = penaltyPoints; }
    public Boolean getPaid()                               { return paid; }
    public void setPaid(Boolean paid)                      { this.paid = paid; }
    public LocalDateTime getCalculatedAt()                 { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt){ this.calculatedAt = calculatedAt; }
}
