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
import pages.{CalculatedMGDHigherRatePage, MgdHigherRatePage, NetTakingsHigherPage, NetTakingsHigherRatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object HigherRateSummary {

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {

    val netTakingsLiable = answers.get(NetTakingsHigherRatePage).map { answer =>
      CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "netTakingsHigherRate.question",
        answer    = answer,
        changeUrl = routes.NetTakingsHigherRateController.onPageLoad(CheckMode).url,
        hiddenMsg = "netTakingsHigherRate.question"
      )
    }

    val netTakings = answers.get(NetTakingsHigherPage).map { answer =>
      CheckYourAnswersHelpers.currencyRow(
        keyMsg    = "submittedReturn.netTakingsHigherRate",
        amount    = answer,
        changeUrl = Some(routes.NetTakingsHigherController.onPageLoad(CheckMode).url),
        hiddenMsg = Some("submittedReturn.netTakingsHigherRate")
      )
    }

    val calculationCorrect = answers.get(CalculatedMGDHigherRatePage).map { answer =>
      CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "calculatedMGDHigherRate.subheading",
        answer    = answer,
        changeUrl = routes.CalculatedMGDHigherRateController.onPageLoad(CheckMode).url,
        hiddenMsg = "calculatedMGDHigherRate.subheading"
      )
    }

    val dutyDue = answers.get(MgdHigherRatePage).map { amount =>
      CheckYourAnswersHelpers.currencyRow(keyMsg = "submittedReturn.totalDueHigherRate", amount = amount)
    }

    Seq(netTakingsLiable, netTakings, calculationCorrect, dutyDue).flatten
  }
}
