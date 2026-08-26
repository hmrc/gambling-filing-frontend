/*
 * Copyright 2026 HM Revenue & Customs
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

package navigation

import base.SpecBase
import controllers.routes
import pages.*
import models.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator()

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe
          routes.IndexController.onPageLoad()
      }

      "must go from MachinesAvailablePage to NetTakingsLowerRatePage" in {
        navigator.nextPage(
          MachinesAvailablePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.NetTakingsLowerRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsLowerRatePage to NetTakingsLowerPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsLowerRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsLowerController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsLowerRatePage to NetTakingsStandardRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsLowerRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsLowerPage to CalculatedMGDLowerRatePage" in {
        navigator.nextPage(
          NetTakingsLowerPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CalculatedMGDLowerRateController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDLowerRatePage to NetTakingsStandardRatePage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDLowerRatePage, true)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDLowerRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDLowerRatePage to Mgd LowerRateRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDLowerRatePage, false)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDLowerRatePage,
          NormalMode,
          answers
        ) mustBe routes.MgdLowerRateController.onPageLoad(NormalMode)
      }

      "must go from MgdLowerRatePage to NetTakingsStandardRatePage" in {
        navigator.nextPage(
          MgdLowerRatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsStandardRatePage to NetTakingsStandardPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsStandardRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsStandardController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsStandardRatePage to NetTakingsStandardPage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsStandardRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsHigherRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsStandardPage to CalculatedMGDStandardRatePage" in {
        navigator.nextPage(
          NetTakingsStandardPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CalculatedMGDStandardRateController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDStandardRatePage to NetTakingsHigherRatePage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDStandardRatePage, true)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDStandardRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsHigherRateController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDStandardRatePage to MgdStandardRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDStandardRatePage, false)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDStandardRatePage,
          NormalMode,
          answers
        ) mustBe routes.MgdStandardRateController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDStandardRatePage to Index when no answer exists" in {
        navigator.nextPage(
          CalculatedMGDStandardRatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.IndexController.onPageLoad()
      }

      "must go from NetTakingsHigherRatePage to NetTakingsHigherPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsHigherController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsHigherRatePage to UnderDeclaredDutyPage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.UnderDeclaredDutyController.onPageLoad(NormalMode)
      }

      "must go from UnderDeclaredDutyPage to UnderDeclaredDutyReasonableCare when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(UnderDeclaredDutyPage, true)
            .success
            .value

        navigator.nextPage(
          UnderDeclaredDutyPage,
          NormalMode,
          answers
        ) mustBe routes.UnderDeclaredDutyReasonableCareController.onPageLoad(NormalMode)
      }

      "must go from UnderDeclaredDutyPage to duty-brought-forward page when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(UnderDeclaredDutyPage, false)
            .success
            .value

        navigator.nextPage(
          UnderDeclaredDutyPage,
          NormalMode,
          answers
        ) mustBe routes.NegativeDutyController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDHigherRatePage to UnderDeclaredDutyController when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDHigherRatePage, true)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.UnderDeclaredDutyController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDHigherRatePage to MgdHigherRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDHigherRatePage, false)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.MgdHigherRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsHigherPage to CalculatedMGDHigherRatePage" in {
        navigator.nextPage(
          NetTakingsHigherPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CalculatedMGDHigherRateController.onPageLoad(NormalMode)
      }

      "must go from UnderDeclaredDutyReasonableCarePage to ContactHmrcPage when answer is Yes" in {
        val answers = emptyUserAnswers.set(UnderDeclaredDutyReasonableCarePage, true).success.value

        navigator.nextPage(
          UnderDeclaredDutyReasonableCarePage,
          NormalMode,
          answers
        ) mustBe routes.ContactHmrcController.onPageLoad(NormalMode)
      }

      "must go from UnderDeclaredDutyReasonableCarePage to UnderDeclaredDutyLimitsPage when answer is No" in {
        val answers = emptyUserAnswers.set(UnderDeclaredDutyReasonableCarePage, false).success.value

        navigator.nextPage(
          UnderDeclaredDutyReasonableCarePage,
          NormalMode,
          answers
        ) mustBe routes.UnderDeclaredDutyLimitsController.onPageLoad(NormalMode)
      }

      "must go from UnderDeclaredDutyReasonableCarePage to SelectReturnPage when no answer exists" in {
        navigator.nextPage(
          UnderDeclaredDutyReasonableCarePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.SelectReturnController.onPageLoad()
      }

      "must go from MgdStandardRatePage to NetTakingsHigherRatePage when answer exists" in {
        val answers = emptyUserAnswers.set(MgdStandardRatePage, BigDecimal(100)).success.value

        val result = navigator.nextPage(MgdStandardRatePage, NormalMode, answers)
        result mustBe routes.NetTakingsHigherRateController.onPageLoad(NormalMode)
      }

      "must go from MgdStandardRatePage to Index when no answer exists" in {
        navigator.nextPage(MgdStandardRatePage, NormalMode, emptyUserAnswers) mustBe routes.IndexController.onPageLoad()
      }

      "must go from MgdHigherRatePage to UnderDeclaredDutyPage" in {
        navigator.nextPage(
          MgdHigherRatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.UnderDeclaredDutyController.onPageLoad(NormalMode)
      }

      "must go from UnderDeclaredDutyLimitsPage to Under-declared duty input when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(UnderDeclaredDutyLimitsPage, true)
            .success
            .value

        navigator.nextPage(
          UnderDeclaredDutyLimitsPage,
          NormalMode,
          answers
        ) mustBe routes.TotalUnderDeclaredDutyController.onPageLoad(NormalMode)
      }

      "must go from UnderDeclaredDutyLimitsPage  to Contact HMRC when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(UnderDeclaredDutyLimitsPage, false)
            .success
            .value

        navigator.nextPage(
          UnderDeclaredDutyLimitsPage,
          NormalMode,
          answers
        ) mustBe routes.ContactHmrcController.onPageLoad(NormalMode)
      }

      "must go from UnderDeclaredDutyLimitsPage to JourneyRecoveryController when no answer exists" in {
        navigator.nextPage(
          UnderDeclaredDutyLimitsPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from ContactHmrcPage to DutyBroughtForwardPage" in {
        navigator.nextPage(
          ContactHmrcPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.NegativeDutyController.onPageLoad(NormalMode)
      }

      "must go from NegativeDutyBroughtForwardInputPage to CheckYourAnswersPage" in {
        navigator.nextPage(
          NegativeDutyBroughtForwardInputPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from NegativeDutyPage  to NegativeDutyBroughtForwardInputPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NegativeDutyPage, true)
            .success
            .value

        navigator.nextPage(
          NegativeDutyPage,
          NormalMode,
          answers
        ) mustBe routes.NegativeDutyBroughtForwardInputController.onPageLoad(NormalMode)
      }

      "must go from TotalUnderDeclaredDutyPage to NegativeDutyPage" in {
        navigator.nextPage(
          TotalUnderDeclaredDutyPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.NegativeDutyController.onPageLoad(NormalMode)
      }

      "must go from NegativeDutyPage  to CheckYourAnswersPage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NegativeDutyPage, false)
            .success
            .value

        navigator.nextPage(
          NegativeDutyPage,
          NormalMode,
          answers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from DeclareAndSubmitPage to Confirmation page" in { // TODO: 24. FAR-CON - File a return - Confirmation  /manage-gambling-tax/returns/confirmation
        navigator.nextPage(
          DeclareAndSubmitPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.PageNotFoundController.onPageLoad()
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from MachinesAvailablePage to CheckYourAnswersController" in {
        navigator.nextPage(
          MachinesAvailablePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from NetTakingsLowerRatePage to NetTakingsLowerPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsLowerRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsLowerController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsLowerRatePage to NetTakingsStandardPage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsLowerRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsLowerPage to CheckYourAnswersController" in {
        navigator.nextPage(
          NetTakingsLowerPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from CalculatedMGDLowerRatePage to NetTakingsStandardRatePage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDLowerRatePage, true)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDLowerRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(CheckMode)
      }

      "must go from CalculatedMGDLowerRatePage to Mgd LowerRateRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDLowerRatePage, false)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDLowerRatePage,
          CheckMode,
          answers
        ) mustBe routes.MgdLowerRateController.onPageLoad(CheckMode)
      }

      "must go from Mgd LowerRatePage to CheckYourAnswersController" in {
        navigator.nextPage(
          MgdLowerRatePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from NetTakingsStandardRatePage to NetTakingsStandardPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsStandardRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsStandardController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsStandardRatePage to NetTakingsHigherRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsStandardRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsHigherRateController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsStandardPage to CheckYourAnswersController" in {
        navigator.nextPage(
          NetTakingsStandardPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from CalculatedMGDStandardRatePage to NetTakingsHigherRatePage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDStandardRatePage, true)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDStandardRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsHigherRateController.onPageLoad(CheckMode)
      }

      "must go from CalculatedMGDStandardRatePage to MgdStandardRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDStandardRatePage, false)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDStandardRatePage,
          CheckMode,
          answers
        ) mustBe routes.MgdStandardRateController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsHigherRatePage to NetTakingsHigherPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsHigherRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsHigherController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsHigherRatePage to UnderDeclaredDutyPage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsHigherRatePage,
          CheckMode,
          answers
        ) mustBe routes.UnderDeclaredDutyController.onPageLoad(CheckMode)
      }

      "must go from UnderDeclaredDutyPage to UnderDeclaredDutyReasonableCare when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(UnderDeclaredDutyPage, true)
            .success
            .value

        navigator.nextPage(
          UnderDeclaredDutyPage,
          CheckMode,
          answers
        ) mustBe routes.UnderDeclaredDutyReasonableCareController.onPageLoad(CheckMode)
      }

      "must go from UnderDeclaredDutyPage to duty-brought-forward page when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(UnderDeclaredDutyPage, false)
            .success
            .value

        navigator.nextPage(
          UnderDeclaredDutyPage,
          CheckMode,
          answers
        ) mustBe routes.NegativeDutyController.onPageLoad(CheckMode)
      }

      "must go from CalculatedMGDHigherRatePage to UnderDeclaredDutyController when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDHigherRatePage, true)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDHigherRatePage,
          CheckMode,
          answers
        ) mustBe routes.UnderDeclaredDutyController.onPageLoad(CheckMode)
      }

      "must go from CalculatedMGDHigherRatePage to MgdHigherRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDHigherRatePage, false)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDHigherRatePage,
          CheckMode,
          answers
        ) mustBe routes.MgdHigherRateController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsHigherPage to CheckYourAnswersController" in {
        navigator.nextPage(
          NetTakingsHigherPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from MgdStandardRatePage to NetTakingsHigherRatePage when answer exists" in {
        val answers = emptyUserAnswers.set(MgdStandardRatePage, BigDecimal(100)).success.value

        val result = navigator.nextPage(MgdStandardRatePage, CheckMode, answers)
        result mustBe routes.NetTakingsHigherRateController.onPageLoad(CheckMode)
      }

      "must go from MgdStandardRatePage to Index when no answer exists" in {
        val result = navigator.nextPage(MgdStandardRatePage, CheckMode, emptyUserAnswers)
        result mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from MgdHigherRatePage to CheckYourAnswersController" in {
        navigator.nextPage(
          MgdHigherRatePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from UnderDeclaredDutyReasonableCarePage to ContactHmrcController when answer is Yes" in {
        val answers = emptyUserAnswers.set(UnderDeclaredDutyReasonableCarePage, true).success.value

        navigator.nextPage(
          UnderDeclaredDutyReasonableCarePage,
          CheckMode,
          answers
        ) mustBe routes.ContactHmrcController.onPageLoad(CheckMode)
      }

      "must go from UnderDeclaredDutyReasonableCarePage to UnderDeclaredDutyLimitsController when answer is No" in {
        val answers = emptyUserAnswers.set(UnderDeclaredDutyReasonableCarePage, false).success.value

        navigator.nextPage(
          UnderDeclaredDutyReasonableCarePage,
          CheckMode,
          answers
        ) mustBe routes.UnderDeclaredDutyLimitsController.onPageLoad(CheckMode)
      }

      "must go from UnderDeclaredDutyReasonableCarePage to CheckYourAnswersPage when no answer exists" in {
        navigator.nextPage(
          UnderDeclaredDutyReasonableCarePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from ContactHmrcPage to CYA" in {
        navigator.nextPage(
          ContactHmrcPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from NegativeDutyBroughtForwardInputPage to CheckYourAnswersPage" in {
        navigator.nextPage(
          NegativeDutyBroughtForwardInputPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from TotalUnderDeclaredDutyPage to CheckYourAnswersController" in {
        navigator.nextPage(
          TotalUnderDeclaredDutyPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from NegativeDutyPage  to NegativeDutyBroughtForwardInputPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NegativeDutyPage, true)
            .success
            .value

        navigator.nextPage(
          NegativeDutyPage,
          CheckMode,
          answers
        ) mustBe routes.NegativeDutyBroughtForwardInputController.onPageLoad(CheckMode)
      }

      "must go from NegativeDutyPage  to CheckYourAnswersPage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NegativeDutyPage, false)
            .success
            .value

        navigator.nextPage(
          NegativeDutyPage,
          CheckMode,
          answers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
