-- =============================================================================
-- MGD Filing Test Data
-- =============================================================================
-- Delete existing data for registration number
-- Insert into return details and return contents
--
-- This script can be run multiple times as it clears data before inserting
-- =============================================================================

DELETE FROM MGD_DATA.MGD_RETURN_CONTENTS_V2   WHERE MGD_REG_NUMBER = 'XWM00003001200';
DELETE FROM MGD_DATA.MGD_RETURN_DETAILS       WHERE MGD_REG_NUMBER = 'XWM00003001200';

COMMIT;

insert into mgd_data.MGD_RETURN_DETAILS(
                                        MGD_REG_NUMBER,
                                        CONSEC_NO,
                                        FORM_BUNDLE_ID,
                                        PERIOD_START_DATE,
                                        PERIOD_END_DATE,
                                        DUE_DATE,
                                        SUBMITTED_DATE,
                                        ACK_REF,
                                        ICR_STATUS,
                                        RECORD_UPDATED,
                                        REMINDER_EMAIL_SENT)
VALUES(
        'XWM00003001200',
        1,
        001,
        DATE '2026-03-01',
        DATE '2026-06-30',
        DATE '2026-07-15',
        DATE '2026-07-15',
        'ACK001',
        'FULFILLED',
        DATE '2026-07-15',
        'Y'
    );

insert into mgd_data.mgd_return_contents_v2(
                                            MGD_REG_NUMBER,
                                            CONSEC_NO,
                                            PERIOD_START_DATE,
                                            PERIOD_END_DATE,
                                            NO_OF_MACHINES_AVAIL,
                                            NET_TAKINGS_STD_RATE,
                                            NET_TAKINGS_LOWER_RATE,
                                            TOTAL_DUE_STD_RATE,
                                            TOTAL_DUE_LOWER_RATE,
                                            DUTY_PAYABLE,
                                            UNDER_DECLARED_DUTY,
                                            PREVIOUS_RETURN_AMOUNT,
                                            NEG_AMT_CARRY_FORWARD,
                                            TOTAL_NET_DUTY_PAYABLE,
                                            NET_TAKINGS_HIGHER_RATE,
                                            TOTAL_DUE_HIGHER_RATE,
                                            RECORD_UPDATED)
VALUES (
        'XWM00003001200',
        1,
        DATE '2026-03-01',
        DATE '2026-06-30',
        5,
        100.00,
        100.00,
        10.00,
        5.00,
        35.00,
        0.00,
        1000.00,
        5.00,
        30.00,
        100.00,
        20.00,
        DATE '2026-06-25'
       );


COMMIT;
