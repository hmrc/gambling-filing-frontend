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

import controllers.routes
import models.{Mode, UserAnswers}
import play.api.libs.json.Reads
import play.api.mvc.Call
import pages.*

object CheckYourAnswersValidator {

  def checkForMissingAnswers(answers: UserAnswers, mode: Mode): Option[Call] = {
    implicit val ua: UserAnswers = answers

    val incompletePages =
      machines(mode) ++
        rateSection(
          ratePage       = NetTakingsLowerRatePage,
          rateCall       = routes.NetTakingsLowerRateController.onPageLoad(mode),
          netTakingsPage = NetTakingsLowerPage,
          netTakingsCall = routes.NetTakingsLowerController.onPageLoad(mode),
          mgdPage        = MgdLowerRatePage,
          mgdCall        = routes.MgdLowerRateController.onPageLoad(mode)
        ) ++
        rateSection(
          ratePage       = NetTakingsStandardRatePage,
          rateCall       = routes.NetTakingsStandardRateController.onPageLoad(mode),
          netTakingsPage = NetTakingsStandardPage,
          netTakingsCall = routes.NetTakingsStandardController.onPageLoad(mode),
          mgdPage        = MgdStandardRatePage,
          mgdCall        = routes.MgdStandardRateController.onPageLoad(mode)
        ) ++
        rateSection(
          ratePage       = NetTakingsHigherRatePage,
          rateCall       = routes.NetTakingsHigherRateController.onPageLoad(mode),
          netTakingsPage = NetTakingsHigherPage,
          netTakingsCall = routes.NetTakingsHigherController.onPageLoad(mode),
          mgdPage        = MgdHigherRatePage,
          mgdCall        = routes.MgdHigherRateController.onPageLoad(mode)
        ) ++
        underDeclaredDuty(mode) ++
        dutyBroughtForward(mode)

    incompletePages.flatten.headOption
  }

  private def unanswered[A](page: QuestionPage[A])(implicit answers: UserAnswers, rds: Reads[A]): Boolean =
    answers.get(page).isEmpty

  private def answeredYes(page: QuestionPage[Boolean])(implicit answers: UserAnswers): Boolean =
    answers.get(page).contains(true)

  private def answeredNo(page: QuestionPage[Boolean])(implicit answers: UserAnswers): Boolean =
    answers.get(page).contains(false)

  private def missingAmount(page: QuestionPage[BigDecimal])(implicit answers: UserAnswers): Boolean =
    answers.get(page).forall(_ == BigDecimal(0))

  private def machines(mode: Mode)(implicit answers: UserAnswers): Seq[Option[Call]] =
    Seq(
      Option.when(unanswered(MachinesAvailablePage))(routes.MachinesAvailableController.onPageLoad(mode))
    )

  private def rateSection(
    ratePage: QuestionPage[Boolean],
    rateCall: Call,
    netTakingsPage: QuestionPage[BigDecimal],
    netTakingsCall: Call,
    mgdPage: QuestionPage[BigDecimal],
    mgdCall: Call
  )(implicit answers: UserAnswers): Seq[Option[Call]] = {
    val rateApplies = answeredYes(ratePage)
    Seq(
      Option.when(unanswered(ratePage))(rateCall),
      Option.when(rateApplies && missingAmount(netTakingsPage))(netTakingsCall),
      Option.when(rateApplies && missingAmount(mgdPage))(mgdCall)
    )
  }

  private def underDeclaredDuty(mode: Mode)(implicit answers: UserAnswers): Seq[Option[Call]] = {
    val dutyUnderDeclared = answeredYes(UnderDeclaredDutyPage)
    val reasonableCareFail = dutyUnderDeclared && answeredNo(UnderDeclaredDutyReasonableCarePage)
    Seq(
      Option.when(unanswered(UnderDeclaredDutyPage))(routes.UnderDeclaredDutyController.onPageLoad(mode)),
      Option.when(dutyUnderDeclared && unanswered(UnderDeclaredDutyReasonableCarePage))(
        routes.UnderDeclaredDutyReasonableCareController.onPageLoad(mode)
      ),
      Option.when(reasonableCareFail && unanswered(UnderDeclaredDutyLimitsPage))(
        routes.UnderDeclaredDutyLimitsController.onPageLoad(mode)
      ),
      Option.when(
        reasonableCareFail && answeredYes(UnderDeclaredDutyLimitsPage) && missingAmount(TotalUnderDeclaredDutyPage)
      )(
        routes.TotalUnderDeclaredDutyController.onPageLoad(mode)
      )
    )
  }

  private def dutyBroughtForward(mode: Mode)(implicit answers: UserAnswers): Seq[Option[Call]] =
    Seq(
      Option.when(unanswered(NegativeDutyPage))(routes.NegativeDutyController.onPageLoad(mode)),
      Option.when(answeredYes(NegativeDutyPage) && missingAmount(NegativeDutyBroughtForwardInputPage))(
        routes.NegativeDutyBroughtForwardInputController.onPageLoad(mode)
      )
    )
}
