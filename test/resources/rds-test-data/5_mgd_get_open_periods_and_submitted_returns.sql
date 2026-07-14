-- ============================================================================
-- Test Data Script for MGD_DC_RTN_PCK.GET_OPEN_PERIODS
-- Registration: XWM00003001200
-- Database: mgd-filing-db  |  Schema: MGD_DATA/MGD_DATA
--
-- GET_OPEN_PERIODS returns rows from mgd_data.mgd_return_details where:
--   1. mgd_reg_number matches
--   2. period_end_date < SYSDATE  (period has ended)
--   3. submitted_date IS NULL     (not yet submitted)
--   4. icr_status != 'FULFILLED'
--   5. No blocking row in MGD_PERIOD_STATUS with status=1
--      and record_updated > mrd.record_updated
--
-- Also needs mgd_data.mgd_operator_details row (other sprocs in the package need it,
-- and GET_MGD_CERTIFICATE uses it with return_details).
--
-- Safe to run multiple times — deletes existing data first, then inserts.
-- Recommend running via DBeaver for large scripts.
-- ============================================================================

SET DEFINE OFF;

-- ============================================================================
-- 0. CLEAN SLATE — Delete existing data for XWM00003001200
-- ============================================================================
DELETE FROM MGD_DATA.MGD_RETURN_CONTENTS_V2   WHERE MGD_REG_NUMBER = 'XWM00003001200';
DELETE FROM mgd_data.mgd_period_status WHERE mgd_reg_number = 'XWM00003001200';
DELETE FROM mgd_data.mgd_return_details WHERE mgd_reg_number = 'XWM00003001200';
DELETE FROM mgd_data.mgd_operator_details WHERE mgd_reg_number = 'XWM00003001200';
COMMIT;

-- ============================================================================
-- 1. mgd_data.mgd_operator_details — needed by other sprocs in the package
-- ============================================================================
INSERT INTO mgd_data.mgd_operator_details (
    MGD_REG_NUMBER, BUSINESS_PARTNER_NUMBER, SOLE_PROP_TITLE,
    SOLE_PROP_FIRST_NAME, SOLE_PROP_MIDDLE_NAME, SOLE_PROP_LAST_NAME,
    BUSINESS_NAME, BUS_ADDRESS_1, BUS_ADDRESS_2, BUS_ADDRESS_3, BUS_ADDRESS_4,
    BUS_POSTCODE, BUS_COUNTRY, BUS_ADI, BUS_IOM_OR_CI,
    BUS_PHONE_NUMBER, BUS_MOBILE_PHONE_NUMBER, BUS_FAX_NUMBER, BUS_EMAIL_ADDR,
    TRADING_NAME, TYPE_OF_BUSINESS, TRADE_CLASS, BUS_ACT_DESCRIPTION,
    VRN, NINO, UTR, CRN, DATE_OF_BIRTH, REGISTRATION_DATE,
    DATE_OF_DEREGISTRATION, REASON_OF_DEREGISTRATION,
    SEASONAL_BUSINESS, UK_INCORPORATED_FLAG, DATE_OF_INCORPORATION,
    COUNTRY_OF_INCORPORATION, FOREIGN_CORPORATE_REF,
    CORRES_NAME1, CORRES_NAME2, CORRES_ADDRESS_1, CORRES_ADDRESS_2,
    CORRES_ADDRESS_3, CORRES_ADDRESS_4, CORRES_POSTCODE, CORRES_COUNTRY,
    CORRES_ADI, CORRES_IOM_OR_CI, CORRES_PHONE_NUMBER,
    CORRES_MOBILE_PHONE_NUMBER, CORRES_FAX_NUMBER, CORRES_EMAIL_ADDR,
    AGENT_OWN_REF, MGD_AGENT_REF, PERIOD_TYPE, RETURN_PERIOD_ID,
    FIRST_RETURN_PERIOD_END_DATE, NO_OF_MACHINES,
    NON_STD_PERIOD_1_END_DATE, NON_STD_PERIOD_2_END_DATE,
    NON_STD_PERIOD_3_END_DATE, NON_STD_PERIOD_4_END_DATE,
    NON_STD_PERIOD_5_END_DATE, NON_STD_PERIOD_6_END_DATE,
    NON_STD_PERIOD_7_END_DATE, NON_STD_PERIOD_8_END_DATE,
    MGDREF_OF_NEW_GROUP, ENROLLED_SIG, RECORD_UPDATED
) VALUES (
    'XWM00003001200', '0100099901', 'Mr',
    'TEST', null, 'OPERATOR',
    null, '10 Test Street', 'Test Town', null, null,
    'TE1 1ST', null, null, 'N',
    null, '07700900001', null, 'test.operator@example.com',
    null, 'Sole proprietor', 1, null,
    null, null, null, null, TO_DATE('01-JAN-1980','DD-MON-YYYY'), TO_DATE('01-APR-2025','DD-MON-YYYY'),
    null, null,
    'N', null, null,
    null, null,
    null, null, null, null,
    null, null, null, null,
    null, null, null,
    null, null, null,
    null, null, 'S', 3,
    TO_DATE('30-JUN-2025','DD-MON-YYYY'), 50,
    null, null,
    null, null,
    null, null,
    null, null,
    null, null, TO_DATE('01-APR-2025','DD-MON-YYYY')
);

-- ============================================================================
-- 2. mgd_data.mgd_return_details — Open periods (submitted_date IS NULL, icr_status != FULFILLED)
--    All period_end_date values must be in the past (< SYSDATE)
--    Mix of overdue (due_date < SYSDATE) and due (due_date >= SYSDATE)
-- ============================================================================

-- Period 1: Overdue — due_date already passed
INSERT INTO mgd_data.mgd_return_details (
    MGD_REG_NUMBER, CONSEC_NO, FORM_BUNDLE_ID,
    PERIOD_START_DATE, PERIOD_END_DATE, DUE_DATE,
    SUBMITTED_DATE, ACK_REF, ICR_STATUS, RECORD_UPDATED, REMINDER_EMAIL_SENT
) VALUES (
    'XWM00003001200', 1, null,
    TO_DATE('01-APR-2025','DD-MON-YYYY'), TO_DATE('30-JUN-2025','DD-MON-YYYY'), TO_DATE('31-JUL-2025','DD-MON-YYYY'),
    null, null, 'CREATED', TO_DATE('01-APR-2025','DD-MON-YYYY'), null
);

-- Period 2: Overdue — due_date already passed
INSERT INTO mgd_data.mgd_return_details (
    MGD_REG_NUMBER, CONSEC_NO, FORM_BUNDLE_ID,
    PERIOD_START_DATE, PERIOD_END_DATE, DUE_DATE,
    SUBMITTED_DATE, ACK_REF, ICR_STATUS, RECORD_UPDATED, REMINDER_EMAIL_SENT
) VALUES (
    'XWM00003001200', 2, null,
    TO_DATE('01-JUL-2025','DD-MON-YYYY'), TO_DATE('30-SEP-2025','DD-MON-YYYY'), TO_DATE('31-OCT-2025','DD-MON-YYYY'),
    null, null, 'CREATED', TO_DATE('01-JUL-2025','DD-MON-YYYY'), null
);

-- Period 3: Overdue — due_date already passed
INSERT INTO mgd_data.mgd_return_details (
    MGD_REG_NUMBER, CONSEC_NO, FORM_BUNDLE_ID,
    PERIOD_START_DATE, PERIOD_END_DATE, DUE_DATE,
    SUBMITTED_DATE, ACK_REF, ICR_STATUS, RECORD_UPDATED, REMINDER_EMAIL_SENT
) VALUES (
    'XWM00003001200', 3, null,
    TO_DATE('01-OCT-2025','DD-MON-YYYY'), TO_DATE('31-DEC-2025','DD-MON-YYYY'), TO_DATE('31-JAN-2026','DD-MON-YYYY'),
    null, null, 'CREATED', TO_DATE('01-OCT-2025','DD-MON-YYYY'), null
);

-- Period 4: Due but not yet overdue — due_date in the future
INSERT INTO mgd_data.mgd_return_details (
    MGD_REG_NUMBER, CONSEC_NO, FORM_BUNDLE_ID,
    PERIOD_START_DATE, PERIOD_END_DATE, DUE_DATE,
    SUBMITTED_DATE, ACK_REF, ICR_STATUS, RECORD_UPDATED, REMINDER_EMAIL_SENT
) VALUES (
    'XWM00003001200', 4, null,
    TO_DATE('01-JAN-2026','DD-MON-YYYY'), TO_DATE('31-MAR-2026','DD-MON-YYYY'), TO_DATE('30-APR-2026','DD-MON-YYYY'),
    null, null, 'CREATED', TO_DATE('01-JAN-2026','DD-MON-YYYY'), null
);

-- Period 5: Most recent open period — due_date in the future
INSERT INTO mgd_data.mgd_return_details (
    MGD_REG_NUMBER, CONSEC_NO, FORM_BUNDLE_ID,
    PERIOD_START_DATE, PERIOD_END_DATE, DUE_DATE,
    SUBMITTED_DATE, ACK_REF, ICR_STATUS, RECORD_UPDATED, REMINDER_EMAIL_SENT
) VALUES (
    'XWM00003001200', 5, null,
    TO_DATE('01-APR-2026','DD-MON-YYYY'), TO_DATE('30-JUN-2026','DD-MON-YYYY'), TO_DATE('31-JUL-2026','DD-MON-YYYY'),
    null, null, 'CREATED', TO_DATE('01-APR-2026','DD-MON-YYYY'), null
);

-- Period 6: Submitted period — should NOT appear (submitted_date is set)

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
          6,
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
           6,
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

-- ============================================================================
-- No mgd_data.mgd_period_status rows inserted — ensures no periods are blocked.
-- If you need to test the blocking behaviour, insert a row like:
--   INSERT INTO mgd_data.mgd_period_status (MGD_REG_NUMBER, CONSEC_NO, RECORD_UPDATED, STATUS)
--   VALUES ('XWM00003001200', 1, TO_DATE('02-APR-2025','DD-MON-YYYY'), 1);
-- That would hide consec_no=1 from the results.
-- ============================================================================

-- ============================================================================
-- EXPECTED RESULTS from GET_OPEN_PERIODS:
--   consec_no 1: 01/04/2025 - 30/06/2025  due 31-JUL-2025  status=2 (overdue)
--   consec_no 2: 01/07/2025 - 30/09/2025  due 31-OCT-2025  status=2 (overdue)
--   consec_no 3: 01/10/2025 - 31/12/2025  due 31-JAN-2026  status=2 (overdue)
--   consec_no 4: 01/01/2026 - 31/03/2026  due 30-APR-2026  status=2 (overdue)
--   consec_no 5: 01/04/2026 - 30/06/2026  due 31-JUL-2026  status=1 (due, not overdue)
--
--   consec_no 6 will NOT appear (submitted_date is set)
--
-- Status decode: SIGN(due_date - current_date)
--   0 or 1 (due_date >= today) => status 1
--   -1     (due_date < today)  => status 2
-- ============================================================================
