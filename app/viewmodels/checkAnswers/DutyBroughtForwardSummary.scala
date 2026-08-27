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
import pages.{NegativeDutyBroughtForwardInputPage, NegativeDutyPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object DutyBroughtForwardSummary {

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    val negativeDutyAnswer = answers.get(NegativeDutyPage)

    val hasNegativeDuty = Some(
      CheckYourAnswersHelpers.yesNoOrActionLinkRow(
        keyMsg        = "negativeDuty.question",
        answer        = negativeDutyAnswer,
        showValueLink = negativeDutyAnswer.isEmpty,
        linkTextMsg   = "site.setValue",
        url           = routes.NegativeDutyController.onPageLoad(CheckMode).url,
        hiddenMsg     = "negativeDuty.question"
      )
    )

    val screenerYes = negativeDutyAnswer.contains(true)

    val amountBroughtForwardAnswer = answers.get(NegativeDutyBroughtForwardInputPage)
    val amountIsMissing = amountBroughtForwardAnswer.forall(_ == BigDecimal(0))

    val amountBroughtForward =
      Option(
        CheckYourAnswersHelpers.currencyOrActionLinkRow(
          keyMsg        = "submittedReturn.previousReturnAmount",
          amount        = amountBroughtForwardAnswer.map(_ * -1).getOrElse(BigDecimal(0)),
          showValueLink = amountIsMissing,
          linkTextMsg   = "site.enterAmount",
          url           = routes.NegativeDutyBroughtForwardInputController.onPageLoad(CheckMode).url,
          hiddenMsg     = "submittedReturn.previousReturnAmount"
        )
      ).filter(_ => screenerYes)

    Seq(hasNegativeDuty, amountBroughtForward).flatten
  }
}
