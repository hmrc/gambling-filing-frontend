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
import play.api.mvc.Call
import pages.*

object CheckYourAnswersValidator {

  private def isMissingAmount(value: Option[BigDecimal]): Boolean = value.forall(_ == BigDecimal(0))

  def validateFirstIncompletePage(answers: UserAnswers, mode: Mode): Option[Call] = {
    val incompletePages =
      machines(answers, mode) ++
        lowerRate(answers, mode) ++
        standardRate(answers, mode) ++
        higherRate(answers, mode) ++
        underDeclaredDuty(answers, mode) ++
        dutyBroughtForward(answers, mode)

    incompletePages.flatten.headOption
  }

  private def machines(answers: UserAnswers, mode: Mode): Seq[Option[Call]] =
    Seq(
      Option.when(
        answers.get(MachinesAvailablePage).isEmpty
      )(
        routes.MachinesAvailableController.onPageLoad(mode)
      )
    )

  private def lowerRate(answers: UserAnswers, mode: Mode): Seq[Option[Call]] =
    Seq(
      Option.when(
        answers.get(NetTakingsLowerRatePage).isEmpty
      )(
        routes.NetTakingsLowerRateController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(NetTakingsLowerRatePage).contains(true) && isMissingAmount(answers.get(NetTakingsLowerPage))
      )(
        routes.NetTakingsLowerController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(NetTakingsLowerRatePage).contains(true) && isMissingAmount(answers.get(MgdLowerRatePage))
      )(
        routes.MgdLowerRateController.onPageLoad(mode)
      )
    )

  private def standardRate(answers: UserAnswers, mode: Mode): Seq[Option[Call]] =
    Seq(
      Option.when(
        answers.get(NetTakingsStandardRatePage).isEmpty
      )(
        routes.NetTakingsStandardRateController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(NetTakingsStandardRatePage).contains(true) && isMissingAmount(answers.get(NetTakingsStandardPage))
      )(
        routes.NetTakingsStandardController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(NetTakingsStandardRatePage).contains(true) && isMissingAmount(answers.get(MgdStandardRatePage))
      )(
        routes.MgdStandardRateController.onPageLoad(mode)
      )
    )

  private def higherRate(answers: UserAnswers, mode: Mode): Seq[Option[Call]] =
    Seq(
      Option.when(
        answers.get(NetTakingsHigherRatePage).isEmpty
      )(
        routes.NetTakingsHigherRateController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(NetTakingsHigherRatePage).contains(true) && isMissingAmount(answers.get(NetTakingsHigherPage))
      )(
        routes.NetTakingsHigherController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(NetTakingsHigherRatePage).contains(true) && isMissingAmount(answers.get(MgdHigherRatePage))
      )(
        routes.MgdHigherRateController.onPageLoad(mode)
      )
    )

  private def underDeclaredDuty(answers: UserAnswers, mode: Mode): Seq[Option[Call]] =
    Seq(
      Option.when(
        answers.get(UnderDeclaredDutyPage).isEmpty
      )(
        routes.UnderDeclaredDutyController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(UnderDeclaredDutyPage).contains(true) && answers.get(UnderDeclaredDutyReasonableCarePage).isEmpty
      )(
        routes.UnderDeclaredDutyReasonableCareController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(UnderDeclaredDutyPage).contains(true) &&
          answers
            .get(UnderDeclaredDutyReasonableCarePage)
            .contains(false) &&
          answers.get(UnderDeclaredDutyLimitsPage).isEmpty
      )(
        routes.UnderDeclaredDutyLimitsController
          .onPageLoad(mode)
      ),
      Option.when(
        answers.get(UnderDeclaredDutyPage).contains(true) &&
          answers
            .get(UnderDeclaredDutyReasonableCarePage)
            .contains(false) &&
          answers
            .get(UnderDeclaredDutyLimitsPage)
            .contains(true) &&
          isMissingAmount(
            answers.get(TotalUnderDeclaredDutyPage)
          )
      )(
        routes.TotalUnderDeclaredDutyController.onPageLoad(mode)
      )
    )

  private def dutyBroughtForward(answers: UserAnswers, mode: Mode): Seq[Option[Call]] =
    Seq(
      Option.when(
        answers.get(NegativeDutyPage).isEmpty
      )(
        routes.NegativeDutyController.onPageLoad(mode)
      ),
      Option.when(
        answers.get(NegativeDutyPage).contains(true) && isMissingAmount(answers.get(NegativeDutyBroughtForwardInputPage))
      )(
        routes.NegativeDutyBroughtForwardInputController.onPageLoad(mode)
      )
    )
}
