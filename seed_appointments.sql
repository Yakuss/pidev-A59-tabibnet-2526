-- =============================================================================
-- seed_appointments.sql
-- Inserts 4 realistic test appointments into the `appointment` table.
-- Safe to run multiple times (uses INSERT IGNORE / duplicate-safe logic).
--
-- Prerequisites:
--   • The Symfony app must have run its migrations so `appointment`, `patients`,
--     and `medecins` tables exist.
--   • This script picks the FIRST available patient/doctor by ID automatically.
--
-- Run with:
--   mysql -u root pidev_1 < seed_appointments.sql
-- =============================================================================

-- Disable FK checks temporarily so order doesn't matter
SET FOREIGN_KEY_CHECKS = 0;

-- ─── Capture real IDs from existing rows ─────────────────────────────────────
-- We store them in user-defined variables so the INSERT block is portable.

-- First 4 distinct patients (or fewer if the table has less)
SET @p1 = (SELECT id FROM patients ORDER BY id LIMIT 1 OFFSET 0);
SET @p2 = (SELECT id FROM patients ORDER BY id LIMIT 1 OFFSET 1);
SET @p3 = (SELECT id FROM patients ORDER BY id LIMIT 1 OFFSET 2);
SET @p4 = (SELECT id FROM patients ORDER BY id LIMIT 1 OFFSET 3);

-- Fall back to p1 if not enough distinct patients
SET @p2 = IFNULL(@p2, @p1);
SET @p3 = IFNULL(@p3, @p1);
SET @p4 = IFNULL(@p4, @p1);

-- First available doctor
SET @doc = (SELECT id FROM medecins ORDER BY id LIMIT 1);

-- ─── Insert 4 appointments (skip silently if a duplicate PK somehow exists) ──
-- Appointment 1: Patient A — morning preference 09:00, next Monday
INSERT INTO appointment
    (date, start_time, duration, status, message, department, patient_id, doctor_id, created_at, reminder_sent)
VALUES
    (
        DATE_ADD(CURDATE(), INTERVAL (9 - WEEKDAY(CURDATE())) % 7 DAY),   -- next Monday
        '09:00:00',
        30,
        'scheduled',
        'Consultation matinale — préférence matin confirmée.',
        'Médecine générale',
        @p1,
        @doc,
        NOW(),
        0
    );

-- Appointment 2: Patient B — evening preference 18:00, next Tuesday
INSERT INTO appointment
    (date, start_time, duration, status, message, department, patient_id, doctor_id, created_at, reminder_sent)
VALUES
    (
        DATE_ADD(CURDATE(), INTERVAL (9 - WEEKDAY(CURDATE()) + 1) % 7 + 1 DAY), -- next Tuesday
        '18:00:00',
        30,
        'pending',
        'Consultation en soirée — préférence soir.',
        'Cardiologie',
        @p2,
        @doc,
        NOW(),
        0
    );

-- Appointment 3: Patient C — midday preference 14:00, next Wednesday
INSERT INTO appointment
    (date, start_time, duration, status, message, department, patient_id, doctor_id, created_at, reminder_sent)
VALUES
    (
        DATE_ADD(CURDATE(), INTERVAL (9 - WEEKDAY(CURDATE()) + 2) % 7 + 2 DAY), -- next Wednesday
        '14:00:00',
        30,
        'scheduled',
        'Suivi post-opératoire.',
        'Chirurgie',
        @p3,
        @doc,
        NOW(),
        0
    );

-- Appointment 4: Patient D — morning preference 10:00, next Thursday
INSERT INTO appointment
    (date, start_time, duration, status, message, department, patient_id, doctor_id, created_at, reminder_sent)
VALUES
    (
        DATE_ADD(CURDATE(), INTERVAL (9 - WEEKDAY(CURDATE()) + 3) % 7 + 3 DAY), -- next Thursday
        '10:00:00',
        30,
        'scheduled',
        'Bilan annuel — préférence matinale.',
        'Médecine interne',
        @p4,
        @doc,
        NOW(),
        0
    );

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Seed complete — 4 appointments inserted.' AS result;
