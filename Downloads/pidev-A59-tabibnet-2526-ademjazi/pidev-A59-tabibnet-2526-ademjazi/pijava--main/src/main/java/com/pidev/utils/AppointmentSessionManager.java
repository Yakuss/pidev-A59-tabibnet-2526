package com.pidev.utils;

import com.pidev.models.Appointment;

/**
 * Global session manager to store selected appointment information
 * across different pages and menus.
 * Maintains persistent red highlighting when returning to the appointments page.
 */
public class AppointmentSessionManager {
    private static AppointmentSessionManager instance;
    private Appointment selectedAppointment;

    private AppointmentSessionManager() {
    }

    public static AppointmentSessionManager getInstance() {
        if (instance == null) {
            instance = new AppointmentSessionManager();
        }
        return instance;
    }

    public void setSelectedAppointment(Appointment appointment) {
        this.selectedAppointment = appointment;
        if (appointment != null) {
            System.out.println("✅ Rendez-vous stocké en session:");
            System.out.println("   ID: " + appointment.getId());
            System.out.println("   Date: " + appointment.getDate());
            System.out.println("   Patient: " + appointment.getPatientName());
            System.out.println("   Médecin: " + appointment.getDoctorName());
            System.out.println("   Département: " + appointment.getDepartment());
        }
    }

    public Appointment getSelectedAppointment() {
        return selectedAppointment;
    }

    public void clearSelectedAppointment() {
        this.selectedAppointment = null;
        System.out.println("🗑️ Rendez-vous supprimé de la session");
    }

    public boolean hasSelectedAppointment() {
        return selectedAppointment != null;
    }

    // Getters pour accès facile aux informations
    public Integer getSelectedAppointmentId() {
        return selectedAppointment != null ? selectedAppointment.getId() : null;
    }

    public String getSelectedPatientName() {
        return selectedAppointment != null ? selectedAppointment.getPatientName() : null;
    }

    public String getSelectedDoctorName() {
        return selectedAppointment != null ? selectedAppointment.getDoctorName() : null;
    }

    public java.time.LocalDateTime getSelectedDate() {
        return selectedAppointment != null ? selectedAppointment.getDate() : null;
    }

    public Integer getSelectedPatientId() {
        return selectedAppointment != null ? selectedAppointment.getPatientId() : null;
    }

    public Integer getSelectedDoctorId() {
        return selectedAppointment != null ? selectedAppointment.getDoctorId() : null;
    }

    public String getSelectedDepartment() {
        return selectedAppointment != null ? selectedAppointment.getDepartment() : null;
    }
}
