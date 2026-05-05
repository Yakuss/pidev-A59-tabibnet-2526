package com.pidev.services;

import com.pidev.models.Medecin;
import com.pidev.models.Patient;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting data to PDF and CSV formats
 */
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export medecins list to CSV format
     */
    public void exportMedecinsToCSV(List<Medecin> medecins, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des médecins");
        fileChooser.setInitialFileName("medecins_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
                // CSV Header
                writer.println("ID,Prénom,Nom,Email,Téléphone,Spécialité,Gouvernorat,Âge,Genre,CIN,Adresse,Formation,Expérience,Vérifié,Actif,Score IA");
                
                // CSV Data
                for (Medecin medecin : medecins) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%.2f%n",
                        medecin.getId(),
                        escapeCsv(medecin.getFirstName()),
                        escapeCsv(medecin.getLastName()),
                        escapeCsv(medecin.getEmail()),
                        escapeCsv(medecin.getPhoneNumber()),
                        medecin.getSpecialty() != null ? medecin.getSpecialty().getDisplayName() : "",
                        medecin.getGovernorate() != null ? medecin.getGovernorate().getDisplayName() : "",
                        medecin.getAge(),
                        escapeCsv(medecin.getGender()),
                        escapeCsv(medecin.getCin()),
                        escapeCsv(medecin.getAddress()),
                        escapeCsv(medecin.getEducation()),
                        escapeCsv(medecin.getExperience()),
                        medecin.isVerified() ? "Oui" : "Non",
                        medecin.isActive() ? "Actif" : "Inactif",
                        medecin.getAiAverageScore() != null ? medecin.getAiAverageScore() : 0.0
                    );
                }
                
                System.out.println("✅ Médecins exportés vers: " + file.getAbsolutePath());
                
            } catch (IOException e) {
                System.err.println("❌ Erreur lors de l'export CSV: " + e.getMessage());
                throw new RuntimeException("Erreur lors de l'export CSV", e);
            }
        }
    }

    /**
     * Export patients list to CSV format
     */
    public void exportPatientsToCSV(List<Patient> patients, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des patients");
        fileChooser.setInitialFileName("patients_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
                // CSV Header
                writer.println("ID,Prénom,Nom,Email,Téléphone,Âge,Genre,Adresse,Date de naissance,Assurance,Numéro d'assurance,Actif");
                
                // CSV Data
                for (Patient patient : patients) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",%s,\"%s\",%s%n",
                        patient.getId(),
                        escapeCsv(patient.getFirstName()),
                        escapeCsv(patient.getLastName()),
                        escapeCsv(patient.getEmail()),
                        escapeCsv(patient.getPhoneNumber()),
                        patient.getAge(),
                        escapeCsv(patient.getGender()),
                        escapeCsv(patient.getAddress()),
                        patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(DATE_FORMATTER) : "",
                        patient.isHasInsurance() ? "Oui" : "Non",
                        escapeCsv(patient.getInsuranceNumber()),
                        patient.isActive() ? "Actif" : "Inactif"
                    );
                }
                
                System.out.println("✅ Patients exportés vers: " + file.getAbsolutePath());
                
            } catch (IOException e) {
                System.err.println("❌ Erreur lors de l'export CSV: " + e.getMessage());
                throw new RuntimeException("Erreur lors de l'export CSV", e);
            }
        }
    }

    /**
     * Export medecins list to PDF format (simple text-based PDF)
     */
    public void exportMedecinsToPDF(List<Medecin> medecins, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des médecins en PDF");
        fileChooser.setInitialFileName("medecins_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
                // PDF Header
                writer.println("=".repeat(80));
                writer.println("                    LISTE DES MÉDECINS");
                writer.println("                 Généré le: " + LocalDateTime.now().format(DATE_FORMATTER));
                writer.println("=".repeat(80));
                writer.println();
                
                // PDF Content
                for (int i = 0; i < medecins.size(); i++) {
                    Medecin medecin = medecins.get(i);
                    writer.println((i + 1) + ". Dr. " + medecin.getFullName());
                    writer.println("   Email: " + (medecin.getEmail() != null ? medecin.getEmail() : "Non renseigné"));
                    writer.println("   Téléphone: " + (medecin.getPhoneNumber() != null ? medecin.getPhoneNumber() : "Non renseigné"));
                    writer.println("   Spécialité: " + (medecin.getSpecialty() != null ? medecin.getSpecialty().getDisplayName() : "Non spécifiée"));
                    writer.println("   Gouvernorat: " + (medecin.getGovernorate() != null ? medecin.getGovernorate().getDisplayName() : "Non spécifié"));
                    writer.println("   Âge: " + medecin.getAge() + " ans");
                    writer.println("   Statut: " + (medecin.isActive() ? "Actif" : "Inactif"));
                    writer.println("   Vérifié: " + (medecin.isVerified() ? "Oui" : "Non"));
                    if (medecin.getAiAverageScore() != null) {
                        writer.printf("   Score IA: %.2f/5.0%n", medecin.getAiAverageScore());
                    }
                    writer.println("   " + "-".repeat(60));
                    writer.println();
                }
                
                writer.println("=".repeat(80));
                writer.println("Total: " + medecins.size() + " médecin(s)");
                writer.println("=".repeat(80));
                
                System.out.println("✅ Médecins exportés vers: " + file.getAbsolutePath());
                
            } catch (IOException e) {
                System.err.println("❌ Erreur lors de l'export PDF: " + e.getMessage());
                throw new RuntimeException("Erreur lors de l'export PDF", e);
            }
        }
    }

    /**
     * Export patients list to PDF format (simple text-based PDF)
     */
    public void exportPatientsToPDF(List<Patient> patients, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des patients en PDF");
        fileChooser.setInitialFileName("patients_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
                // PDF Header
                writer.println("=".repeat(80));
                writer.println("                    LISTE DES PATIENTS");
                writer.println("                 Généré le: " + LocalDateTime.now().format(DATE_FORMATTER));
                writer.println("=".repeat(80));
                writer.println();
                
                // PDF Content
                for (int i = 0; i < patients.size(); i++) {
                    Patient patient = patients.get(i);
                    writer.println((i + 1) + ". " + patient.getFullName());
                    writer.println("   Email: " + (patient.getEmail() != null ? patient.getEmail() : "Non renseigné"));
                    writer.println("   Téléphone: " + (patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "Non renseigné"));
                    writer.println("   Âge: " + patient.getAge() + " ans");
                    writer.println("   Genre: " + (patient.getGender() != null ? patient.getGender() : "Non spécifié"));
                    writer.println("   Adresse: " + (patient.getAddress() != null ? patient.getAddress() : "Non renseignée"));
                    if (patient.getDateOfBirth() != null) {
                        writer.println("   Date de naissance: " + patient.getDateOfBirth().format(DATE_FORMATTER));
                    }
                    writer.println("   Assurance: " + (patient.isHasInsurance() ? "Oui" : "Non"));
                    if (patient.isHasInsurance() && patient.getInsuranceNumber() != null) {
                        writer.println("   Numéro d'assurance: " + patient.getInsuranceNumber());
                    }
                    writer.println("   Statut: " + (patient.isActive() ? "Actif" : "Inactif"));
                    writer.println("   " + "-".repeat(60));
                    writer.println();
                }
                
                writer.println("=".repeat(80));
                writer.println("Total: " + patients.size() + " patient(s)");
                writer.println("=".repeat(80));
                
                System.out.println("✅ Patients exportés vers: " + file.getAbsolutePath());
                
            } catch (IOException e) {
                System.err.println("❌ Erreur lors de l'export PDF: " + e.getMessage());
                throw new RuntimeException("Erreur lors de l'export PDF", e);
            }
        }
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        // Replace quotes with double quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains("\"") || value.contains(",") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Generate export statistics
     */
    public String generateExportSummary(List<?> data, String type) {
        return String.format("Export %s terminé avec succès!\n" +
                           "Nombre d'enregistrements: %d\n" +
                           "Date d'export: %s\n" +
                           "Format: CSV/PDF",
                           type, data.size(), LocalDateTime.now().format(DATE_FORMATTER));
    }
}