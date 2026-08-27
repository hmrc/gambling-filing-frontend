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

    val screenerAnswer = answers.get(NetTakingsHigherRatePage)

    val netTakingsLiable = Some(
      CheckYourAnswersHelpers.yesNoOrActionLinkRow(
        keyMsg        = "netTakingsHigherRate.question",
        answer        = screenerAnswer,
        showValueLink = screenerAnswer.isEmpty,
        linkTextMsg   = "site.setValue",
        url           = routes.NetTakingsHigherRateController.onPageLoad(CheckMode).url,
        hiddenMsg     = "netTakingsHigherRate.question"
      )
    )

    val screenerYes = screenerAnswer.contains(true)

    val netTakingsAmount = answers.get(NetTakingsHigherPage)
    val netTakingsIsMissing = netTakingsAmount.forall(_ == BigDecimal(0))

    val netTakings =
      Option(
        CheckYourAnswersHelpers.currencyOrActionLinkRow(
          keyMsg        = "submittedReturn.netTakingsHigherRate",
          amount        = netTakingsAmount.getOrElse(BigDecimal(0)),
          showValueLink = netTakingsIsMissing,
          linkTextMsg   = "site.enterNetTakings",
          url           = routes.NetTakingsHigherController.onPageLoad(CheckMode).url,
          hiddenMsg     = "submittedReturn.netTakingsHigherRate"
        )
      ).filter(_ => screenerYes)

    val calculatedMGDAnswer = answers.get(CalculatedMGDHigherRatePage)

    val calculationCorrect =
      netTakings.flatMap(_ =>
        Option(
          CheckYourAnswersHelpers.yesNoOrActionLinkRow(
            keyMsg        = "checkYourAnswers.mgd.question",
            answer        = calculatedMGDAnswer,
            showValueLink = calculatedMGDAnswer.isEmpty && !netTakingsIsMissing,
            linkTextMsg   = "site.setValue",
            url           = routes.CalculatedMGDHigherRateController.onPageLoad(CheckMode).url,
            hiddenMsg     = "checkYourAnswers.mgd.question"
          )
        ).filter(_ => calculatedMGDAnswer.isDefined || !netTakingsIsMissing)
      )

    val correctedDutyAmount = answers.get(MgdHigherRatePage)
    val correctedDutyIsMissing = correctedDutyAmount.forall(_ == BigDecimal(0))

    val dutyDue =
      calculatedMGDAnswer.flatMap {
        case true => correctedDutyAmount.map(amount => CheckYourAnswersHelpers.currencyRow("submittedReturn.totalDueHigherRate", amount))
        case false =>
          Some(
            CheckYourAnswersHelpers.currencyOrActionLinkRow(
              keyMsg        = "submittedReturn.totalDueHigherRate",
              amount        = correctedDutyAmount.getOrElse(BigDecimal(0)),
              showValueLink = correctedDutyIsMissing,
              linkTextMsg   = "checkYourAnswers.enterMGD",
              url           = routes.MgdHigherRateController.onPageLoad(CheckMode).url,
              hiddenMsg     = "submittedReturn.totalDueHigherRate"
            )
          )
      }

    Seq(netTakingsLiable, netTakings, calculationCorrect, dutyDue).flatten
  }
}
