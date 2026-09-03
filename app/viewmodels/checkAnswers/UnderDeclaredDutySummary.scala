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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.{TotalUnderDeclaredDutyPage, UnderDeclaredDutyLimitsPage, UnderDeclaredDutyPage, UnderDeclaredDutyReasonableCarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object UnderDeclaredDutySummary {

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {

    val hasUnderDeclaredDutyAnswer = answers.get(UnderDeclaredDutyPage)

    val hasUnderDeclaredDuty = Some(
      CheckYourAnswersHelpers.yesNoOrActionLinkRow(
        keyMsg        = "underDeclaredDuty.heading",
        answer        = hasUnderDeclaredDutyAnswer,
        showValueLink = hasUnderDeclaredDutyAnswer.isEmpty,
        linkTextMsg   = "checkYourAnswers.setValue",
        url           = routes.UnderDeclaredDutyController.onPageLoad(CheckMode).url,
        hiddenMsg     = "underDeclaredDuty.heading"
      )
    )

    val screenerYes = hasUnderDeclaredDutyAnswer.contains(true)

    val reasonableCareAnswer = answers.get(UnderDeclaredDutyReasonableCarePage)

    val reasonableCare =
      Option(
        CheckYourAnswersHelpers.yesNoOrActionLinkRow(
          keyMsg        = "checkYourAnswers.underDeclaredDutyReasonableCare.question",
          answer        = reasonableCareAnswer,
          showValueLink = reasonableCareAnswer.isEmpty,
          linkTextMsg   = "checkYourAnswers.setValue",
          url           = routes.UnderDeclaredDutyReasonableCareController.onPageLoad(CheckMode).url,
          hiddenMsg     = "checkYourAnswers.underDeclaredDutyReasonableCare.question"
        )
      ).filter(_ => screenerYes)

    val withinLimitsAnswer = answers.get(UnderDeclaredDutyLimitsPage)

    val withinLimits =
      reasonableCare.flatMap(_ =>
        Option(
          CheckYourAnswersHelpers.yesNoOrActionLinkRow(
            keyMsg        = "checkYourAnswers.underDeclaredDutyLimits.question",
            answer        = withinLimitsAnswer,
            showValueLink = withinLimitsAnswer.isEmpty,
            linkTextMsg   = "checkYourAnswers.setValue",
            url           = routes.UnderDeclaredDutyLimitsController.onPageLoad(CheckMode).url,
            hiddenMsg     = "checkYourAnswers.underDeclaredDutyLimits.question"
          )
        ).filter(_ => reasonableCareAnswer.contains(false))
      )

    val totalUnderDeclaredDutyAnswer = answers.get(TotalUnderDeclaredDutyPage)
    val totalUnderDeclaredDutyIsMissing = totalUnderDeclaredDutyAnswer.forall(_ == BigDecimal(0))

    val totalUnderDeclaredDuty =
      withinLimits.flatMap(_ =>
        Option(
          CheckYourAnswersHelpers.currencyOrActionLinkRow(
            keyMsg        = "checkYourAnswers.totalUnderDeclaredDuty.question",
            amount        = totalUnderDeclaredDutyAnswer.getOrElse(BigDecimal(0)),
            showValueLink = totalUnderDeclaredDutyIsMissing,
            linkTextMsg   = "checkYourAnswers.enterAmount",
            url           = routes.TotalUnderDeclaredDutyController.onPageLoad(CheckMode).url,
            hiddenMsg     = "checkYourAnswers.totalUnderDeclaredDuty.question"
          )
        ).filter(_ => withinLimitsAnswer.contains(true))
      )

    Seq(hasUnderDeclaredDuty, reasonableCare, withinLimits, totalUnderDeclaredDuty).flatten
  }
}
