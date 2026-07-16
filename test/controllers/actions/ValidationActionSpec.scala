/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import base.SpecBase
import models.Regime

class ValidationActionSpec extends SpecBase {

  "ValidationAction validateRegime" - {
    "validateRegime returns TRUE for GBD" in {
      ValidationAction.validateRegime(Regime.GBD, "XBA00003000000") mustBe true
      ValidationAction.validateRegime(Regime.GBD, "XBA00003199999") mustBe true
    }

    "validateRegime returns FALSE for GBD" in {
      ValidationAction.validateRegime(Regime.GBD, "XBA00002999999") mustBe false
      ValidationAction.validateRegime(Regime.GBD, "XBA00003200000") mustBe false
      ValidationAction.validateRegime(Regime.PBD, "XBA00003199999") mustBe false
    }

    "validateRegime returns TRUE for PBD" in {
      ValidationAction.validateRegime(Regime.PBD, "XBA00003200000") mustBe true
      ValidationAction.validateRegime(Regime.PBD, "XBA00003399999") mustBe true
    }

    "validateRegime returns FALSE for PBD" in {
      ValidationAction.validateRegime(Regime.PBD, "XBA00003199999") mustBe false
      ValidationAction.validateRegime(Regime.PBD, "XBA00003400000") mustBe false
      ValidationAction.validateRegime(Regime.GBD, "XBA00003200000") mustBe false
    }

    "validateRegime returns TRUE for RGD" in {
      ValidationAction.validateRegime(Regime.RGD, "XBA00003400000") mustBe true
      ValidationAction.validateRegime(Regime.RGD, "XBA00003599999") mustBe true
    }

    "validateRegime returns FALSE for RGD" in {
      ValidationAction.validateRegime(Regime.RGD, "XBA00003399999") mustBe false
      ValidationAction.validateRegime(Regime.RGD, "XBA00003600000") mustBe false
      ValidationAction.validateRegime(Regime.GBD, "XBA00003400000") mustBe false
    }

    "validateRegime returns TRUE for MGD" in {
      ValidationAction.validateRegime(Regime.MGD, "XBA00000400000") mustBe true
      ValidationAction.validateRegime(Regime.MGD, "XBA00003500000") mustBe true
    }

    "validateRegime returns FALSE for short RegNums" in {
      ValidationAction.validateRegime(Regime.GBD, "XBA0002999999") mustBe false
      ValidationAction.validateRegime(Regime.GBD, "XBA123") mustBe false
      ValidationAction.validateRegime(Regime.PBD, "XBA") mustBe false
    }

    "validateRegime returns FALSE for Reg Nums with spaces" in {
      ValidationAction.validateRegime(Regime.GBD, " WA00003000000") mustBe false
      ValidationAction.validateRegime(Regime.GBD, "X A00003199999") mustBe false
      ValidationAction.validateRegime(Regime.GBD, "XNA0000 200000") mustBe false
      ValidationAction.validateRegime(Regime.GBD, "XEA000034000 0") mustBe false
      ValidationAction.validateRegime(Regime.GBD, "XGM0000312220 ") mustBe false
    }
  }

  "ValidationAction validateRegNum" - {
    "validateRegNum returns TRUE for valid Reg Nums" in {
      ValidationAction.validateRegNum("XWA00003000000") mustBe true // GBD
      ValidationAction.validateRegNum("XHA00003199999") mustBe true // GBD
      ValidationAction.validateRegNum("XNA00003200000") mustBe true // PBD
      ValidationAction.validateRegNum("XEA00003400000") mustBe true // RGD
      ValidationAction.validateRegNum("XGM00003122200") mustBe true // MGD
    }

    "validateRegNum returns FALSE for invalid Check Digit" in {
      ValidationAction.validateRegNum("XZA00003000000") mustBe false
      ValidationAction.validateRegNum("XZA00003199999") mustBe false
    }

    "validateRegNum returns FALSE for too short" in {
      ValidationAction.validateRegNum("XWA0003000000") mustBe false
    }

    "validateRegNum returns FALSE for very short" in {
      ValidationAction.validateRegNum("XWA001") mustBe false
    }

    "validateRegNum returns FALSE for too long" in {
      ValidationAction.validateRegNum("XWA000003000000") mustBe false
    }

    "validateRegNum returns FALSE for does not match regEx" in {
      ValidationAction.validateRegNum("XWA0000300000Z") mustBe false
      ValidationAction.validateRegNum("1WA00003000000") mustBe false
      ValidationAction.validateRegNum("XW000003000000") mustBe false
    }

    "validateRegNum returns FALSE for Reg Nums with spaces" in {
      ValidationAction.validateRegNum(" WA00003000000") mustBe false
      ValidationAction.validateRegNum("X A00003199999") mustBe false
      ValidationAction.validateRegNum("XNA0000 200000") mustBe false
      ValidationAction.validateRegNum("XEA000034000 0") mustBe false
      ValidationAction.validateRegNum("XGM0000312220 ") mustBe false
    }
  }
}
