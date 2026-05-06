package com.pidev.utils;

import com.pidev.models.RendezVous;

/**
 * Global session manager to store selected rendezvous information
 * across different pages and menus.
 */
public class AppointmentSessionManager {
    private static AppointmentSessionManager instance;
    private RendezVous selectedAppointment;

    private AppointmentSessionManager() {
    }

    public static AppointmentSessionManager getInstance() {
        if (instance == null) {
            instance = new AppointmentSessionManager();
        }
        return instance;
    }

    public void setSelectedAppointment(RendezVous appointment) {
        this.selectedAppointment = appointment;
        if (appointment != null) {
            System.out.println("✅ Rendez-vous stocké en session:");
            System.out.println("   ID: " + appointment.getId());
            System.out.println("   Date: " + appointment.getDate());
            System.out.println("   Patient: " + appointment.getNomPatient());
            System.out.println("   Médecin: " + appointment.getMedecin());
            System.out.println("   Département: " + appointment.getDepartment());
        }
    }

    public RendezVous getSelectedAppointment() {
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
        return selectedAppointment != null ? selectedAppointment.getNomPatient() : null;
    }

    public String getSelectedDoctorName() {
        return selectedAppointment != null ? selectedAppointment.getMedecin() : null;
    }

    public String getSelectedDate() {
        return selectedAppointment != null ? selectedAppointment.getDate() : null;
    }

    public Integer getSelectedPatientId() {
        return selectedAppointment != null ? selectedAppointment.getPatientId() : null;
    }

    public Integer getSelectedDoctorId() {
        return selectedAppointment != null ? selectedAppointment.getMedecinId() : null;
    }

    public String getSelectedDepartment() {
        return selectedAppointment != null ? selectedAppointment.getDepartment() : null;
    }
}
