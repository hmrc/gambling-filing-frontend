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
import pages.{CalculatedMGDStandardRatePage, MgdStandardRatePage, NetTakingsStandardPage, NetTakingsStandardRatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object StandardRateSummary {

  def rows(answers: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {

    val screenerAnswer = answers.get(NetTakingsStandardRatePage)

    val netTakingsLiable = Some(
      CheckYourAnswersHelpers.yesNoOrActionLinkRow(
        keyMsg        = "netTakingsStandardRate.question",
        answer        = screenerAnswer,
        showValueLink = screenerAnswer.isEmpty,
        linkTextMsg   = "site.setValue",
        url           = routes.NetTakingsStandardRateController.onPageLoad(CheckMode).url,
        hiddenMsg     = "netTakingsStandardRate.question"
      )
    )

    val screenerYes = screenerAnswer.contains(true)

    val netTakingsAmount = answers.get(NetTakingsStandardPage)
    val netTakingsIsMissing = netTakingsAmount.forall(_ == BigDecimal(0))

    val netTakings =
      Option(
        CheckYourAnswersHelpers.currencyOrActionLinkRow(
          keyMsg        = "submittedReturn.netTakingsStdRate",
          amount        = netTakingsAmount.getOrElse(BigDecimal(0)),
          showValueLink = netTakingsIsMissing,
          linkTextMsg   = "site.enterNetTakings",
          url           = routes.NetTakingsStandardController.onPageLoad(CheckMode).url,
          hiddenMsg     = "submittedReturn.netTakingsStdRate"
        )
      ).filter(_ => screenerYes)

    val calculatedMGDAnswer = answers.get(CalculatedMGDStandardRatePage)

    val calculationCorrect =
      netTakings.flatMap(_ =>
        Option(
          CheckYourAnswersHelpers.yesNoOrActionLinkRow(
            keyMsg        = "checkYourAnswers.mgd.question",
            answer        = calculatedMGDAnswer,
            showValueLink = calculatedMGDAnswer.isEmpty && !netTakingsIsMissing,
            linkTextMsg   = "site.setValue",
            url           = routes.CalculatedMGDStandardRateController.onPageLoad(CheckMode).url,
            hiddenMsg     = "checkYourAnswers.mgd.question"
          )
        ).filter(_ => calculatedMGDAnswer.isDefined || !netTakingsIsMissing)
      )

    val correctedDutyAmount = answers.get(MgdStandardRatePage)
    val correctedDutyIsMissing = correctedDutyAmount.forall(_ == BigDecimal(0))

    val dutyDue =
      calculatedMGDAnswer.flatMap {
        case true => correctedDutyAmount.map(amount => CheckYourAnswersHelpers.currencyRow("submittedReturn.totalDueStdRate", amount))
        case false =>
          Some(
            CheckYourAnswersHelpers.currencyOrActionLinkRow(
              keyMsg        = "submittedReturn.totalDueStdRate",
              amount        = correctedDutyAmount.getOrElse(BigDecimal(0)),
              showValueLink = correctedDutyIsMissing,
              linkTextMsg   = "checkYourAnswers.enterMGD",
              url           = routes.MgdStandardRateController.onPageLoad(CheckMode).url,
              hiddenMsg     = "submittedReturn.totalDueStdRate"
            )
          )
      }

    Seq(netTakingsLiable, netTakings, calculationCorrect, dutyDue).flatten
  }
}
