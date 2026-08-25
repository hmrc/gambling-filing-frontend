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
import pages.{CalculatedMGDLowerRatePage, MgdLowerRatePage, NetTakingsLowerPage, NetTakingsLowerRatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object LowerRateSummary {

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {

    val netTakingsLiable = answers.get(NetTakingsLowerRatePage).map { answer =>
      CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "netTakingsLowerRate.question",
        answer    = answer,
        changeUrl = routes.NetTakingsLowerRateController.onPageLoad(CheckMode).url,
        hiddenMsg = "netTakingsLowerRate.question"
      )
    }

    val netTakings = answers.get(NetTakingsLowerPage).map { answer =>
      CheckYourAnswersHelpers.currencyRow(
        keyMsg    = "submittedReturn.netTakingsLowerRate",
        amount    = answer,
        changeUrl = Some(routes.NetTakingsLowerController.onPageLoad(CheckMode).url),
        hiddenMsg = Some("submittedReturn.netTakingsLowerRate")
      )
    }

    val calculationCorrect = answers.get(CalculatedMGDLowerRatePage).map { answer =>
      CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "calculatedMGDLowerRate.subheading",
        answer    = answer,
        changeUrl = routes.CalculatedMGDLowerRateController.onPageLoad(CheckMode).url,
        hiddenMsg = "calculatedMGDLowerRate.subheading"
      )
    }

    val dutyDue = answers.get(MgdLowerRatePage).map { amount =>
      CheckYourAnswersHelpers.currencyRow(keyMsg = "submittedReturn.totalDueLowerRate", amount = amount)
    }

    Seq(netTakingsLiable, netTakings, calculationCorrect, dutyDue).flatten
  }
}
