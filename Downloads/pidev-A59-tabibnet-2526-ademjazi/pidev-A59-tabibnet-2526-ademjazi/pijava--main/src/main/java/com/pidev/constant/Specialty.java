package com.pidev.constant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enum representing medical specialties - mirrors Symfony Specialty constants.
 */
public enum Specialty {
    ANATOMIE("Anatomie"),
    ANATOMIE_CYTOLOGIE_PATHOLOGIQUE("Anatomie et cytologie pathologique"),
    ANESTHESIE_REANIMATION("Anesthésie réanimation"),
    BIOLOGIE_MEDICALE("Biologie médicale"),
    BIOLOGIE_MEDICALE_BIOCHIMIE("Biologie médicale option biochimie"),
    BIOLOGIE_MEDICALE_HEMATOLOGIE("Biologie médicale option hématologie"),
    BIOLOGIE_MEDICALE_IMMUNOLOGIE("Biologie médicale option immunologie"),
    BIOLOGIE_MEDICALE_MICROBIOLOGIE("Biologie médicale option microbiologie"),
    BIOLOGIE_MEDICALE_PARASITOLOGIE("Biologie médicale option parasitologie"),
    BIOPHYSIQUE_MEDECINE_NUCLEAIRE("Biophysique et médecine nucléaire"),
    CARCINOLOGIE_MEDICALE("Carcinologie médicale"),
    CARDIOLOGIE("Cardiologie"),
    CHIRURGIE_CARDIO_VASCULAIRE("Chirurgie cardio vasculaire"),
    CHIRURGIE_CARCINOLOGIQUE("Chirurgie carcinologique"),
    CHIRURGIE_GENERALE("Chirurgie générale"),
    CHIRURGIE_NEUROLOGIQUE("Chirurgie neurologique"),
    CHIRURGIE_ORTHOPEDIQUE_TRAUMATOLOGIQUE("Chirurgie orthopédique et traumatologique"),
    CHIRURGIE_PEDIATRIQUE("Chirurgie pédiatrique"),
    CHIRURGIE_PLASTIQUE_REPARATRICE_ESTHETIQUE("Chirurgie plastique réparatrice et esthétique"),
    CHIRURGIE_THORACIQUE("Chirurgie thoracique"),
    CHIRURGIE_UROLOGIQUE("Chirurgie urologique"),
    CHIRURGIE_VASCULAIRE_PERIPHERIQUE("Chirurgie vasculaire périphérique"),
    DERMATOLOGIE("Dermatologie"),
    ENDOCRINOLOGIE("Endocrinologie"),
    GASTRO_ENTEROLOGIE("Gastro-entérologie"),
    GENETIQUE("Génétique"),
    GYNECOLOGIE_OBSTETRIQUE("Gynécologie obstétrique"),
    HEMATOLOGIE_CLINIQUE("Hématologie clinique"),
    HISTO_EMBRYOLOGIE("Histo-embryologie"),
    IMAGERIE_MEDICALE("Imagerie médicale"),
    MALADIES_INFECTIEUSES("Maladies infectieuses"),
    MEDECINE_AERONAUTIQUE_SPATIALE("Médecine aéronautique et spatiale"),
    MEDECINE_DE_FAMILLE("Médecine de Famille"),
    MEDECINE_URGENCE("Médecine d'urgence"),
    MEDECINE_DU_TRAVAIL("Médecine du travail"),
    MEDECINE_GENERALE("Médecine générale"),
    MEDECINE_INTERNE("Médecine interne"),
    MEDECINE_LEGALE("Médecine légale"),
    MEDECINE_PHYSIQUE_READAPTATION("Médecine physique, rééducation et réadaptation fonctionnelle"),
    MEDECINE_PREVENTIVE_COMMUNAUTAIRE("Médecine préventive et communautaire"),
    NEPHROLOGIE("Néphrologie"),
    NEUROLOGIE("Neurologie"),
    NUTRITION_MALADIES_NUTRITIONNELLES("Nutrition et maladies nutritionnelles"),
    OPHTALMOLOGIE("Ophtalmologie"),
    OTO_RHINO_LARYNGOLOGIE("Oto-rhino-laryngologie"),
    PEDIATRIE("Pédiatrie"),
    PEDO_PSYCHIATRIE("Pédo psychiatrie"),
    PHARMACOLOGIE("Pharmacologie"),
    PHYSIOLOGIE_EXPLORATION_FONCTIONNELLE("Physiologie et exploration fonctionnelle"),
    PNEUMOLOGIE("Pneumologie"),
    PSYCHIATRIE("Psychiatrie"),
    RADIOTHERAPIE_CARCINOLOGIQUE("Radiothérapie carcinologique"),
    REANIMATION_MEDICALE("Réanimation médicale"),
    RHUMATOLOGIE("Rhumatologie"),
    SANS_SPECIALITE("sans spécialité"),
    SPECIALISTE_MEDECINE_FAMILLE("Spécialiste en médecine de famille"),
    STOMATOLOGIE_CHIRURGIE_MAXILLO_FACIALE("Stomatologie et chirurgie maxillo-faciale"),
    UROLOGIE("Urologie"),
    DENTISTE("Dentiste");

    private final String displayName;

    Specialty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a map of display names to enum constants for use in ComboBoxes, etc.
     */
    public static Map<String, Specialty> getChoices() {
        Map<String, Specialty> choices = new LinkedHashMap<>();
        for (Specialty s : values()) {
            choices.put(s.displayName, s);
        }
        return choices;
    }

    /**
     * Gets enum constant from its display name (case-sensitive).
     */
    public static Specialty fromDisplayName(String displayName) {
        for (Specialty s : values()) {
            if (s.displayName.equals(displayName)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}