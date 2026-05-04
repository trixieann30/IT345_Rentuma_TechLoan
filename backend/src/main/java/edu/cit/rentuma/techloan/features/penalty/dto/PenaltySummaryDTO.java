package edu.cit.rentuma.techloan.features.penalty.dto;

import java.util.List;

public class PenaltySummaryDTO {

    private Long userId;
    private String borrowerName;
    private Integer totalPoints;
    private List<PenaltyDTO> penalties;

    public PenaltySummaryDTO() {}

    public PenaltySummaryDTO(Long userId, String borrowerName,
                              Integer totalPoints, List<PenaltyDTO> penalties) {
        this.userId       = userId;
        this.borrowerName = borrowerName;
        this.totalPoints  = totalPoints;
        this.penalties    = penalties;
    }

    public Long getUserId()                                { return userId; }
    public void setUserId(Long userId)                     { this.userId = userId; }
    public String getBorrowerName()                        { return borrowerName; }
    public void setBorrowerName(String borrowerName)       { this.borrowerName = borrowerName; }
    public Integer getTotalPoints()                        { return totalPoints; }
    public void setTotalPoints(Integer totalPoints)        { this.totalPoints = totalPoints; }
    public List<PenaltyDTO> getPenalties()                 { return penalties; }
    public void setPenalties(List<PenaltyDTO> penalties)   { this.penalties = penalties; }
}
