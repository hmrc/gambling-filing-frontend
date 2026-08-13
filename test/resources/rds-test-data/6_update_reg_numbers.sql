-- ============================================================================
-- ONE-OFF MIGRATION: rename existing test-data registration numbers.
--
-- Context: environments (e.g. local, QA) were already populated by scripts 1-5
-- using the OLD reg numbers. This script renames those already-loaded rows in
-- place to the NEW reg numbers. Re-running scripts 1-5 would NOT achieve this
-- for an already-populated DB (their clean-slate DELETEs are keyed on the old
-- number, so a rename must be done with UPDATEs instead).
--
-- Run this ONCE against each already-populated DB (local, QA). It is safe to
-- re-run: the WHERE clauses match only the OLD numbers, so a second run is a
-- no-op once the rename has been applied.
--
-- Tables span two schemas (GTR_DATA, MGD_DATA) and are schema-qualified below,
-- so run this connected as a user with UPDATE rights on both (e.g. a DBA
-- account such as SYSTEM), not as a single schema owner.
--
-- Mapping:
--   GBD  XBA00003000001 -> XRG00003005200   (gtr_* tables)
--   PBD  XPA00003200001 -> XDP00003215200   (gtr_* tables)
--   RGD  XGA00003400001 -> XER00003400200   (gtr_* tables)
--   MGD  XWM00003001200 -> XFM00003001200   (mgd_* tables)
-- ============================================================================

-- ---------- GBD : XBA00003000001 -> XRG00003005200 (gtr_* tables) ----------
UPDATE GTR_DATA.gtr_lp_accruing_interest  SET gtr_reg_number = 'XRG00003005200' WHERE gtr_reg_number = 'XBA00003000001';
UPDATE GTR_DATA.gtr_lp_late_pay_interest  SET gtr_reg_number = 'XRG00003005200' WHERE gtr_reg_number = 'XBA00003000001';
UPDATE GTR_DATA.gtr_lp_payment            SET gtr_reg_number = 'XRG00003005200' WHERE gtr_reg_number = 'XBA00003000001';
UPDATE GTR_DATA.gtr_lp_payment_on_account SET gtr_reg_number = 'XRG00003005200' WHERE gtr_reg_number = 'XBA00003000001';
UPDATE GTR_DATA.gtr_lp_reallocation_out   SET gtr_reg_number = 'XRG00003005200' WHERE gtr_reg_number = 'XBA00003000001';
UPDATE GTR_DATA.gtr_lp_charge             SET gtr_reg_number = 'XRG00003005200' WHERE gtr_reg_number = 'XBA00003000001';
UPDATE GTR_DATA.gtr_lp_total              SET gtr_reg_number = 'XRG00003005200' WHERE gtr_reg_number = 'XBA00003000001';
UPDATE GTR_DATA.gtr_operator_details      SET gtr_reg_number = 'XRG00003005200' WHERE gtr_reg_number = 'XBA00003000001';

-- ---------- PBD : XPA00003200001 -> XDP00003215200 (gtr_* tables) ----------
UPDATE GTR_DATA.gtr_lp_accruing_interest  SET gtr_reg_number = 'XDP00003215200' WHERE gtr_reg_number = 'XPA00003200001';
UPDATE GTR_DATA.gtr_lp_late_pay_interest  SET gtr_reg_number = 'XDP00003215200' WHERE gtr_reg_number = 'XPA00003200001';
UPDATE GTR_DATA.gtr_lp_payment            SET gtr_reg_number = 'XDP00003215200' WHERE gtr_reg_number = 'XPA00003200001';
UPDATE GTR_DATA.gtr_lp_payment_on_account SET gtr_reg_number = 'XDP00003215200' WHERE gtr_reg_number = 'XPA00003200001';
UPDATE GTR_DATA.gtr_lp_reallocation_out   SET gtr_reg_number = 'XDP00003215200' WHERE gtr_reg_number = 'XPA00003200001';
UPDATE GTR_DATA.gtr_lp_charge             SET gtr_reg_number = 'XDP00003215200' WHERE gtr_reg_number = 'XPA00003200001';
UPDATE GTR_DATA.gtr_lp_total              SET gtr_reg_number = 'XDP00003215200' WHERE gtr_reg_number = 'XPA00003200001';
UPDATE GTR_DATA.gtr_operator_details      SET gtr_reg_number = 'XDP00003215200' WHERE gtr_reg_number = 'XPA00003200001';

-- ---------- RGD : XGA00003400001 -> XER00003400200 (gtr_* tables) ----------
UPDATE GTR_DATA.gtr_lp_accruing_interest  SET gtr_reg_number = 'XER00003400200' WHERE gtr_reg_number = 'XGA00003400001';
UPDATE GTR_DATA.gtr_lp_late_pay_interest  SET gtr_reg_number = 'XER00003400200' WHERE gtr_reg_number = 'XGA00003400001';
UPDATE GTR_DATA.gtr_lp_payment            SET gtr_reg_number = 'XER00003400200' WHERE gtr_reg_number = 'XGA00003400001';
UPDATE GTR_DATA.gtr_lp_payment_on_account SET gtr_reg_number = 'XER00003400200' WHERE gtr_reg_number = 'XGA00003400001';
UPDATE GTR_DATA.gtr_lp_reallocation_out   SET gtr_reg_number = 'XER00003400200' WHERE gtr_reg_number = 'XGA00003400001';
UPDATE GTR_DATA.gtr_lp_charge             SET gtr_reg_number = 'XER00003400200' WHERE gtr_reg_number = 'XGA00003400001';
UPDATE GTR_DATA.gtr_lp_total              SET gtr_reg_number = 'XER00003400200' WHERE gtr_reg_number = 'XGA00003400001';
UPDATE GTR_DATA.gtr_operator_details      SET gtr_reg_number = 'XER00003400200' WHERE gtr_reg_number = 'XGA00003400001';

-- ---------- MGD : XWM00003001200 -> XFM00003001200 (mgd_* tables) ----------
UPDATE MGD_DATA.mgd_lp_accruing_interest  SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_lp_late_pay_interest  SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_lp_payment            SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_lp_payment_on_account SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_lp_reallocation_out   SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_lp_charge             SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_lp_total              SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_return_details        SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_operator_details      SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
-- extra MGD tables populated by script 5 (open periods / submitted returns)
UPDATE MGD_DATA.mgd_period_status         SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';
UPDATE MGD_DATA.mgd_return_contents_v2    SET mgd_reg_number = 'XFM00003001200' WHERE mgd_reg_number = 'XWM00003001200';

COMMIT;
