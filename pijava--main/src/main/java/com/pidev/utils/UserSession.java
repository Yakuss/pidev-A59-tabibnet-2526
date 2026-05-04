package com.pidev.utils;

import com.pidev.models.BaseUser;

/**
 * Singleton Session - stores the currently logged-in user.
 */
public class UserSession {
    private static UserSession instance;
    private BaseUser user;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public BaseUser getUser() { return user; }
    public void setUser(BaseUser user) { this.user = user; }

    private Integer selectedPatientId;
    private Integer selectedMedecinId;
    private Integer selectedAppointmentId;

    public Integer getSelectedPatientId() { return selectedPatientId; }
    public void setSelectedPatientId(Integer selectedPatientId) { this.selectedPatientId = selectedPatientId; }

    public Integer getSelectedMedecinId() { return selectedMedecinId; }
    public void setSelectedMedecinId(Integer selectedMedecinId) { this.selectedMedecinId = selectedMedecinId; }

    public Integer getSelectedAppointmentId() { return selectedAppointmentId; }
    public void setSelectedAppointmentId(Integer selectedAppointmentId) { this.selectedAppointmentId = selectedAppointmentId; }

    public void cleanUserSession() {
        user = null;
        selectedPatientId = null;
        selectedMedecinId = null;
        selectedAppointmentId = null;
    }
}
