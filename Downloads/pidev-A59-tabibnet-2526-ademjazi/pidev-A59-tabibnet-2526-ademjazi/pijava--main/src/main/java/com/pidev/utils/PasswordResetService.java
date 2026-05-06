package com.pidev.utils;

import com.pidev.models.BaseUser;
import com.pidev.services.AuthService;
import com.pidev.services.MedecinService;
import com.pidev.services.PatientService;
import com.pidev.models.Patient;
import com.pidev.models.Medecin;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the forgot-password OTP flow:
 *  1. generateAndSendOTP(email)  — creates a 6-digit code, stores it, sends email
 *  2. verifyOTP(email, code)     — returns true if code matches and is not expired
 *  3. resetPassword(email, pwd)  — hashes and saves the new password
 */
public class PasswordResetService {

    private static PasswordResetService instance;

    // In-memory OTP store: email → [otp, expiry]
    private final Map<String, OTPEntry> otpStore = new HashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 10;

    private PasswordResetService() {}

    public static PasswordResetService getInstance() {
        if (instance == null) instance = new PasswordResetService();
        return instance;
    }

    // ── Step 1: Generate OTP and send email ─────────────────────────────────

    /**
     * Looks up the user by email, generates a 6-digit OTP, stores it,
     * and sends the reset email asynchronously.
     *
     * @param email     the user's email address
     * @param onSuccess called on the FX thread when email is sent
     * @param onError   called on the FX thread with an error message
     */
    public void generateAndSendOTP(String email,
                                   Runnable onSuccess,
                                   java.util.function.Consumer<String> onError) {
        // Look up user across all tables
        BaseUser user = findUserByEmail(email);
        if (user == null) {
            if (onError != null) onError.accept("Aucun compte trouvé avec cet email.");
            return;
        }

        String otp = generateOTP();
        otpStore.put(email.toLowerCase(), new OTPEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));

        String firstName = user.getFirstName() != null ? user.getFirstName() : "Utilisateur";
        EmailService.getInstance().sendPasswordResetOTP(email, firstName, otp, onSuccess, onError);
    }

    // ── Step 2: Verify OTP ───────────────────────────────────────────────────

    /**
     * Returns true if the OTP matches and has not expired.
     */
    public boolean verifyOTP(String email, String code) {
        OTPEntry entry = otpStore.get(email.toLowerCase());
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiry)) {
            otpStore.remove(email.toLowerCase());
            return false;
        }
        return entry.otp.equals(code.trim());
    }

    // ── Step 3: Reset password ───────────────────────────────────────────────

    /**
     * Hashes the new password and updates it in the database.
     * Clears the OTP entry on success.
     *
     * @return true on success, false if user not found
     */
    public boolean resetPassword(String email, String newPassword) {
        try {
            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            BaseUser user = findUserByEmail(email);
            if (user == null) {
                System.err.println("Password reset: user not found for " + email);
                return false;
            }

            if (user instanceof Patient p) {
                new PatientService().updatePassword(p.getId(), hashed);
            } else if (user instanceof Medecin m) {
                new MedecinService().updatePassword(m.getId(), hashed);
            } else {
                System.err.println("Password reset: unsupported user type");
                return false;
            }

            otpStore.remove(email.toLowerCase());
            System.out.println("✅ Password reset for: " + email);
            return true;

        } catch (Exception e) {
            System.err.println("Password reset error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateOTP() {
        SecureRandom rng = new SecureRandom();
        int code = 100000 + rng.nextInt(900000); // 6 digits
        return String.valueOf(code);
    }

    private BaseUser findUserByEmail(String email) {
        try {
            // Search patients
            for (Patient p : new PatientService().getAll()) {
                if (email.equalsIgnoreCase(p.getEmail())) return p;
            }
        } catch (Exception ignored) {}
        try {
            // Search medecins
            for (Medecin m : new MedecinService().getAll()) {
                if (email.equalsIgnoreCase(m.getEmail())) return m;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── Inner class ──────────────────────────────────────────────────────────

    private record OTPEntry(String otp, LocalDateTime expiry) {}
}
