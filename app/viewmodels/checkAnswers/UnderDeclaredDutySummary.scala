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

    val hasUnderDeclaredDuty = answers.get(UnderDeclaredDutyPage).map { answer =>
      CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "underDeclaredDuty.heading",
        answer    = answer,
        changeUrl = routes.UnderDeclaredDutyController.onPageLoad(CheckMode).url,
        hiddenMsg = "underDeclaredDuty.heading"
      )
    }

    val reasonableCare = answers.get(UnderDeclaredDutyReasonableCarePage).map { answer =>
      CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "underDeclaredDutyReasonableCare.heading",
        answer    = answer,
        changeUrl = routes.UnderDeclaredDutyReasonableCareController.onPageLoad(CheckMode).url,
        hiddenMsg = "underDeclaredDutyReasonableCare.heading"
      )
    }

    val withinLimits = answers.get(UnderDeclaredDutyLimitsPage).map { answer =>
      CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "underDeclaredDutyLimits.question",
        answer    = answer,
        changeUrl = routes.UnderDeclaredDutyLimitsController.onPageLoad(CheckMode).url,
        hiddenMsg = "underDeclaredDutyLimits.question"
      )
    }

    val totalUnderDeclaredDuty = answers.get(TotalUnderDeclaredDutyPage).map { answer =>
      CheckYourAnswersHelpers.currencyRow(
        keyMsg    = "submittedReturn.underDeclaredDuty",
        amount    = answer,
        changeUrl = Some(routes.TotalUnderDeclaredDutyController.onPageLoad(CheckMode).url),
        hiddenMsg = Some("submittedReturn.underDeclaredDuty")
      )
    }

    Seq(hasUnderDeclaredDuty, reasonableCare, withinLimits, totalUnderDeclaredDuty).flatten
  }
}
