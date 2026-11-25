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
import models.{CheckMode, NormalMode}
import pages.*

class CheckYourAnswersValidatorSpec extends SpecBase {

  private val amount = BigDecimal(100)

  private def machinesComplete = emptyUserAnswers
    .set(MachinesAvailablePage, 10L)
    .success
    .value

  private def lowerComplete = machinesComplete
    .set(NetTakingsLowerRatePage, false)
    .success
    .value

  private def standardComplete = lowerComplete
    .set(NetTakingsStandardRatePage, false)
    .success
    .value

  private def higherComplete = standardComplete
    .set(NetTakingsHigherRatePage, false)
    .success
    .value

  "CheckYourAnswersValidator" - {

    "must return Machines Available when machines is missing" in {

      CheckYourAnswersValidator
        .checkForMissingAnswers(emptyUserAnswers, NormalMode) mustBe
        Some(controllers.routes.MachinesAvailableController.onPageLoad(NormalMode))
    }

    "must return Lower Rate screener when it is missing" in {

      CheckYourAnswersValidator
        .checkForMissingAnswers(machinesComplete, NormalMode) mustBe
        Some(controllers.routes.NetTakingsLowerRateController.onPageLoad(NormalMode))
    }

    "must return Lower Net Takings when Lower Rate is Yes and Net Takings is missing" in {
      val answers = machinesComplete
        .set(NetTakingsLowerRatePage, true)
        .success
        .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe
        Some(controllers.routes.NetTakingsLowerController.onPageLoad(NormalMode))
    }

    "must return Lower MGD when Net Takings is answered and MGD is missing" in {
      val answers =
        machinesComplete
          .set(NetTakingsLowerRatePage, true)
          .success
          .value
          .set(NetTakingsLowerPage, amount)
          .success
          .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe
        Some(controllers.routes.MgdLowerRateController.onPageLoad(NormalMode))
    }

    "must move past Lower MGD to the Standard Rate screener when MGD is zero" in {
      val answers =
        machinesComplete
          .set(NetTakingsLowerRatePage, true)
          .success
          .value
          .set(NetTakingsLowerPage, amount)
          .success
          .value
          .set(MgdLowerRatePage, BigDecimal(0))
          .success
          .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe
        Some(controllers.routes.NetTakingsStandardRateController.onPageLoad(NormalMode))
    }

    "must return Standard Rate screener when it is missing" in {

      CheckYourAnswersValidator
        .checkForMissingAnswers(lowerComplete, NormalMode) mustBe
        Some(controllers.routes.NetTakingsStandardRateController.onPageLoad(NormalMode))
    }

    "must return Higher Rate screener when it is missing" in {

      CheckYourAnswersValidator
        .checkForMissingAnswers(standardComplete, NormalMode) mustBe
        Some(controllers.routes.NetTakingsHigherRateController.onPageLoad(NormalMode))
    }

    "must return Under Declared Duty when it is missing" in {

      CheckYourAnswersValidator
        .checkForMissingAnswers(higherComplete, NormalMode) mustBe
        Some(controllers.routes.UnderDeclaredDutyController.onPageLoad(NormalMode))
    }

    "must return Reasonable Care when Under Declared Duty is Yes and it is missing" in {
      val answers =
        higherComplete
          .set(UnderDeclaredDutyPage, true)
          .success
          .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe
        Some(controllers.routes.UnderDeclaredDutyReasonableCareController.onPageLoad(NormalMode))
    }

    "must return Under Declared Duty Limits when required and missing" in {
      val answers =
        higherComplete
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, false)
          .success
          .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe
        Some(controllers.routes.UnderDeclaredDutyLimitsController.onPageLoad(NormalMode))
    }

    "must return Total Under Declared Duty when required and missing" in {
      val answers =
        higherComplete
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, false)
          .success
          .value
          .set(UnderDeclaredDutyLimitsPage, true)
          .success
          .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe
        Some(controllers.routes.TotalUnderDeclaredDutyController.onPageLoad(NormalMode))
    }

    "must return Negative Duty when it is missing" in {
      val answers =
        higherComplete
          .set(UnderDeclaredDutyPage, false)
          .success
          .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe
        Some(controllers.routes.NegativeDutyController.onPageLoad(NormalMode))
    }

    "must return Negative Duty amount when Negative Duty is Yes and amount is missing" in {
      val answers =
        higherComplete
          .set(UnderDeclaredDutyPage, false)
          .success
          .value
          .set(NegativeDutyPage, true)
          .success
          .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe
        Some(controllers.routes.NegativeDutyBroughtForwardInputController.onPageLoad(NormalMode))
    }

    "must use CheckMode when supplied" in {

      CheckYourAnswersValidator
        .checkForMissingAnswers(machinesComplete, CheckMode) mustBe
        Some(controllers.routes.NetTakingsLowerRateController.onPageLoad(CheckMode))
    }

    "must return None when all required answers are complete" in {
      val answers =
        higherComplete
          .set(UnderDeclaredDutyPage, false)
          .success
          .value
          .set(NegativeDutyPage, false)
          .success
          .value

      CheckYourAnswersValidator
        .checkForMissingAnswers(answers, NormalMode) mustBe None
    }
  }

  "isReachable" - {

    def userAnswers(answers: (QuestionPage[Boolean], Boolean)*) =
      answers.foldLeft(emptyUserAnswers)((acc, a) => acc.set(a._1, a._2).success.value)

    def isReachable(page: Page, answers: models.UserAnswers) = CheckYourAnswersValidator.isReachable(page)(answers)

    val rateFlows = Seq(
      (NetTakingsLowerRatePage, NetTakingsLowerPage, MgdLowerRatePage),
      (NetTakingsStandardRatePage, NetTakingsStandardPage, MgdStandardRatePage),
      (NetTakingsHigherRatePage, NetTakingsHigherPage, MgdHigherRatePage)
    )

    "net takings pages are only reachable when their rate screener is Yes" in {
      rateFlows.foreach { case (screener, netTakings, _) =>
        isReachable(netTakings, userAnswers(screener -> true)) mustBe true
        isReachable(netTakings, userAnswers(screener -> false)) mustBe false
        isReachable(netTakings, emptyUserAnswers) mustBe false
      }
    }

    "MGD due pages are only reachable when their rate screener is Yes and net takings has been entered" in {
      rateFlows.foreach { case (screener, netTakings, mgd) =>
        def withNetTakings(amount: BigDecimal) = userAnswers(screener -> true).set(netTakings, amount).success.value

        isReachable(mgd, withNetTakings(amount)) mustBe true
        isReachable(mgd, userAnswers(screener -> true)) mustBe false
        isReachable(mgd, withNetTakings(BigDecimal(0))) mustBe false
        isReachable(mgd, userAnswers(screener -> false)) mustBe false
        isReachable(mgd, emptyUserAnswers) mustBe false
      }
    }

    "under-declared duty child pages are only reachable down the branch their parent answers lead to" in {
      isReachable(UnderDeclaredDutyReasonableCarePage, userAnswers(UnderDeclaredDutyPage -> true)) mustBe true
      isReachable(UnderDeclaredDutyReasonableCarePage, userAnswers(UnderDeclaredDutyPage -> false)) mustBe false

      val careFailed = Seq(UnderDeclaredDutyPage -> true, UnderDeclaredDutyReasonableCarePage -> false)
      isReachable(UnderDeclaredDutyLimitsPage, userAnswers(careFailed*)) mustBe true
      isReachable(UnderDeclaredDutyLimitsPage, userAnswers(UnderDeclaredDutyPage -> true, UnderDeclaredDutyReasonableCarePage -> true)) mustBe false
      isReachable(UnderDeclaredDutyLimitsPage, userAnswers(UnderDeclaredDutyPage -> false)) mustBe false

      isReachable(TotalUnderDeclaredDutyPage, userAnswers((careFailed :+ (UnderDeclaredDutyLimitsPage -> true))*)) mustBe true
      isReachable(TotalUnderDeclaredDutyPage, userAnswers((careFailed :+ (UnderDeclaredDutyLimitsPage -> false))*)) mustBe false
      isReachable(TotalUnderDeclaredDutyPage, userAnswers(UnderDeclaredDutyPage -> false)) mustBe false

      isReachable(ContactHmrcPage, userAnswers(UnderDeclaredDutyPage -> true, UnderDeclaredDutyReasonableCarePage -> true)) mustBe true
      isReachable(ContactHmrcPage, userAnswers((careFailed :+ (UnderDeclaredDutyLimitsPage -> false))*)) mustBe true
      isReachable(ContactHmrcPage, userAnswers((careFailed :+ (UnderDeclaredDutyLimitsPage -> true))*)) mustBe false
      isReachable(ContactHmrcPage, userAnswers(UnderDeclaredDutyPage -> false)) mustBe false
    }

    "negative duty input is only reachable when negative duty is Yes" in {
      isReachable(NegativeDutyBroughtForwardInputPage, userAnswers(NegativeDutyPage -> true)) mustBe true
      isReachable(NegativeDutyBroughtForwardInputPage, userAnswers(NegativeDutyPage -> false)) mustBe false
    }

    "top-level questions are always reachable" in {
      Seq(MachinesAvailablePage, NetTakingsLowerRatePage, UnderDeclaredDutyPage, NegativeDutyPage)
        .foreach(isReachable(_, emptyUserAnswers) mustBe true)
    }
  }
}
