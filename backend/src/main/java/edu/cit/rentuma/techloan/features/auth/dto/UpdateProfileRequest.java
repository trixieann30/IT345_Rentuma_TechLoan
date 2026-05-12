package edu.cit.rentuma.techloan.features.auth.dto;

public class UpdateProfileRequest {
    private String fullName;
    private String studentId;
    private String personalEmail;
    private String currentPassword;
    private String newPassword;

    public String getFullName()                        { return fullName; }
    public void setFullName(String fullName)           { this.fullName = fullName; }
    public String getStudentId()                       { return studentId; }
    public void setStudentId(String studentId)         { this.studentId = studentId; }
    public String getPersonalEmail()                   { return personalEmail; }
    public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }
    public String getCurrentPassword()                 { return currentPassword; }
    public void setCurrentPassword(String p)           { this.currentPassword = p; }
    public String getNewPassword()                     { return newPassword; }
    public void setNewPassword(String p)               { this.newPassword = p; }
}
