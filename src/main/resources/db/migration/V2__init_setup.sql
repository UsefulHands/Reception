-- =====================================================
-- HOTEL PMS DATABASE IMPROVEMENTS - FIXED VERSION
-- This file contains necessary additions and fixes
-- WITHOUT the problematic test queries
-- =====================================================

-- =====================================================
-- 1. ADD MISSING CONSTRAINTS
-- =====================================================

ALTER TABLE reservations
    ADD CONSTRAINT chk_checkout_after_checkin
        CHECK (check_out_date > check_in_date);

ALTER TABLE reservations
    ADD CONSTRAINT chk_total_price_positive
        CHECK (total_price >= 0);

ALTER TABLE reservations
    ADD CONSTRAINT chk_amount_paid_valid
        CHECK (amount_paid >= 0 AND amount_paid <= total_price);

ALTER TABLE rooms
    ADD CONSTRAINT chk_room_price_positive
        CHECK (price > 0);

ALTER TABLE rooms
    ADD CONSTRAINT chk_beds_positive
        CHECK (beds > 0);

ALTER TABLE rooms
    ADD CONSTRAINT chk_max_guests_positive
        CHECK (max_guests > 0);

-- =====================================================
-- 2. ADD USEFUL INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX idx_reservations_availability
    ON reservations(room_id, check_in_date, check_out_date, status, is_deleted)
    WHERE is_deleted = false AND status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT');

CREATE INDEX idx_reservations_date_range
    ON reservations(check_in_date, check_out_date, is_deleted, status)
    WHERE is_deleted = false;

CREATE INDEX idx_reservations_guest
    ON reservations(guest_id, status, is_deleted)
    WHERE is_deleted = false;

CREATE INDEX idx_rooms_available
    ON rooms(available, status, is_deleted)
    WHERE is_deleted = false;

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs(created_at DESC);

CREATE INDEX idx_audit_logs_action
    ON audit_logs(action, created_at DESC);

-- =====================================================
-- 3. ADD MISSING COLUMNS
-- =====================================================

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) DEFAULT 'UNPAID';

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS special_requests TEXT;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS early_check_in BOOLEAN DEFAULT FALSE;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS late_check_out BOOLEAN DEFAULT FALSE;

-- =====================================================
-- 4. UPDATE EXISTING DATA TO BE CONSISTENT
-- =====================================================

UPDATE reservations
SET payment_status = CASE
                         WHEN amount_paid = 0 THEN 'UNPAID'
                         WHEN amount_paid >= total_price THEN 'FULLY_PAID'
                         ELSE 'PARTIALLY_PAID'
    END
WHERE payment_status IS NULL OR payment_status = 'UNPAID';

UPDATE reservations
SET check_out_date = check_in_date + INTERVAL '1 day'
WHERE check_out_date <= check_in_date;

UPDATE reservations
SET status = 'CANCELLED',
    cancellation_reason = 'Auto-cancelled - expired reservation',
    cancelled_at = CURRENT_TIMESTAMP
WHERE check_out_date < CURRENT_DATE
  AND status = 'PENDING'
  AND is_deleted = false;

-- =====================================================
-- 5. ADD VALIDATION TRIGGERS
-- =====================================================

CREATE OR REPLACE FUNCTION validate_reservation_dates()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.check_in_date < CURRENT_DATE THEN
        RAISE EXCEPTION 'Check-in date cannot be in the past';
END IF;

    IF NEW.check_out_date <= NEW.check_in_date THEN
        RAISE EXCEPTION 'Check-out must be at least 1 day after check-in';
END IF;

    IF (NEW.check_out_date - NEW.check_in_date) > 30 THEN
        RAISE EXCEPTION 'Maximum stay duration is 30 nights';
END IF;

    IF NEW.check_in_date > CURRENT_DATE + INTERVAL '1 year' THEN
        RAISE EXCEPTION 'Cannot book more than 1 year in advance';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_reservation_dates_insert
    BEFORE INSERT ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION validate_reservation_dates();

CREATE TRIGGER trg_validate_reservation_dates_update
    BEFORE UPDATE ON reservations
    FOR EACH ROW
    WHEN (OLD.check_in_date IS DISTINCT FROM NEW.check_in_date
      OR OLD.check_out_date IS DISTINCT FROM NEW.check_out_date)
EXECUTE FUNCTION validate_reservation_dates();

-- =====================================================
-- 6. ADD USEFUL VIEWS FOR COMMON QUERIES
-- =====================================================

CREATE OR REPLACE VIEW v_active_reservations AS
SELECT
    r.id,
    r.room_id,
    r.guest_id,
    r.check_in_date,
    r.check_out_date,
    r.status,
    r.total_price,
    r.amount_paid,
    r.payment_status,
    rm.room_number,
    rm.type as room_type,
    g.first_name as guest_first_name,
    g.last_name as guest_last_name,
    g.phone_number,
    CASE
        WHEN r.check_in_date = CURRENT_DATE THEN 'CHECKING_IN_TODAY'
        WHEN r.check_out_date = CURRENT_DATE THEN 'CHECKING_OUT_TODAY'
        WHEN r.check_in_date < CURRENT_DATE AND r.check_out_date > CURRENT_DATE THEN 'CURRENTLY_STAYING'
        WHEN r.check_in_date > CURRENT_DATE THEN 'UPCOMING'
        ELSE 'PAST'
        END as reservation_timeline,
    (r.check_out_date - r.check_in_date) as nights,
    (r.total_price - r.amount_paid) as balance
FROM reservations r
         JOIN rooms rm ON r.room_id = rm.id
         JOIN guests g ON r.guest_id = g.id
WHERE r.is_deleted = false
  AND r.status NOT IN ('CANCELLED', 'NO_SHOW');

CREATE OR REPLACE VIEW v_rooms_available_today AS
SELECT
    r.id,
    r.room_number,
    r.type,
    r.price,
    r.floor,
    r.status as room_status,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM reservations res
            WHERE res.room_id = r.id
              AND res.is_deleted = false
              AND res.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT')
              AND CURRENT_DATE >= res.check_in_date
              AND CURRENT_DATE < res.check_out_date
        ) THEN false
        ELSE true
        END as available_today
FROM rooms r
WHERE r.is_deleted = false
  AND r.status NOT IN ('MAINTENANCE', 'OUT_OF_SERVICE');

-- =====================================================
-- 7. ADD HELPFUL FUNCTIONS
-- =====================================================

CREATE OR REPLACE FUNCTION is_room_available(
    p_room_id BIGINT,
    p_check_in DATE,
    p_check_out DATE
) RETURNS BOOLEAN AS $$
BEGIN
RETURN NOT EXISTS (
    SELECT 1
    FROM reservations
    WHERE room_id = p_room_id
      AND is_deleted = false
      AND status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT')
      AND check_in_date < p_check_out
      AND check_out_date > p_check_in
);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_revenue_for_period(
    p_start_date DATE,
    p_end_date DATE
) RETURNS DECIMAL AS $$
BEGIN
RETURN COALESCE(
        (SELECT SUM(total_price)
         FROM reservations
         WHERE is_deleted = false
           AND status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT')
           AND check_in_date >= p_start_date
           AND check_in_date <= p_end_date),
        0
       );
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 8. ADD SAMPLE DATA FOR TESTING (OPTIONAL)
-- =====================================================

INSERT INTO users (username, password, role, created_by)
VALUES ('receptionist', '$2a$10$hkgemn3l2/eMQIVynLaghuSGszNDwkeGX2FLzN23QMCTUMO1Uiw7a', 'ROLE_RECEPTIONIST', 'SYSTEM')
    ON CONFLICT (username) DO NOTHING;

INSERT INTO receptionists (first_name, last_name, employee_id, shift_type, user_id, created_by)
SELECT 'Sarah', 'Johnson', 'REC001', 'MORNING', u.id, 'SYSTEM'
FROM users u
WHERE u.username = 'receptionist'
  AND NOT EXISTS (SELECT 1 FROM receptionists WHERE user_id = u.id);

INSERT INTO users (username, password, role, created_by)
VALUES ('johnGuest', '$2a$10$hkgemn3l2/eMQIVynLaghuSGszNDwkeGX2FLzN23QMCTUMO1Uiw7a', 'ROLE_GUEST', 'SYSTEM')
    ON CONFLICT (username) DO NOTHING;

INSERT INTO guests (first_name, last_name, phone_number, identity_number, user_id, created_by)
SELECT 'John', 'Doe', '+1234567890', 'ID123456', u.id, 'SYSTEM'
FROM users u
WHERE u.username = 'johnGuest'
  AND NOT EXISTS (SELECT 1 FROM guests WHERE user_id = u.id);