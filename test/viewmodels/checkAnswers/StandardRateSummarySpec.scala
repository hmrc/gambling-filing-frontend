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

import base.SpecBase
import controllers.routes
import models.CheckMode
import pages.{MgdStandardRatePage, NetTakingsStandardPage, NetTakingsStandardRatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

class StandardRateSummarySpec extends SpecBase {

  private def screenerUrl = routes.NetTakingsStandardRateController.onPageLoad(CheckMode).url
  private def netTakingsUrl = routes.NetTakingsStandardController.onPageLoad(CheckMode).url
  private def mgdStandardUrl = routes.MgdStandardRateController.onPageLoad(CheckMode).url

  private def keys(rows: Seq[uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow]): Seq[String] =
    rows.map(_.key.content.asInstanceOf[Text].value)

  "StandardRateSummary" - {

    "must show the screener row with a 'Set value' link, and hide the net takings and calculation rows, when the screener is unanswered" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val answers = emptyUserAnswers
        .set(NetTakingsStandardPage, BigDecimal(500))
        .success
        .value

      val rows = StandardRateSummary.rows(answers)
      val screenerRow = rows.find(_.key.content == Text(msgs("netTakingsStandardRate.question"))).value

      screenerRow.value.content mustBe HtmlContent(s"""<a class="govuk-link" href="$screenerUrl">${msgs("checkYourAnswers.setValue")}</a>""")
      screenerRow.actions mustBe None

      keys(rows) mustNot contain(msgs("submittedReturn.netTakingsStdRate"))
      keys(rows) mustNot contain(msgs("checkYourAnswers.totalDueStdRate"))
    }

    "must show the screener row with the 'No' value and hide the net takings and calculation rows when the screener is No" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val answers = emptyUserAnswers
        .set(NetTakingsStandardRatePage, false)
        .success
        .value
        .set(NetTakingsStandardPage, BigDecimal(500))
        .success
        .value

      val rows = StandardRateSummary.rows(answers)
      val screenerRow = rows.find(_.key.content == Text(msgs("netTakingsStandardRate.question"))).value

      screenerRow.value.content mustBe Text(msgs("site.no"))
      screenerRow.actions.value.items.head.content mustBe Text(msgs("site.change"))
      screenerRow.actions.value.items.head.href mustBe screenerUrl

      keys(rows) mustNot contain(msgs("submittedReturn.netTakingsStdRate"))
      keys(rows) mustNot contain(msgs("checkYourAnswers.totalDueStdRate"))
    }

    "when the screener is Yes and net takings is unanswered and calculation is unanswered" - {
      "must show an 'Enter net takings' link and hide the calculation row" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(NetTakingsStandardRatePage, true)
          .success
          .value

        val rows = StandardRateSummary.rows(answers)
        val netTakingsRow = rows.find(_.key.content == Text(msgs("submittedReturn.netTakingsStdRate"))).value

        netTakingsRow.value.content mustBe HtmlContent(
          s"""<a class="govuk-link" href="$netTakingsUrl">${msgs("checkYourAnswers.enterNetTakings")}</a>"""
        )
        netTakingsRow.actions mustBe None

        rows.exists(_.key.content == Text(msgs("checkYourAnswers.totalDueStdRate"))) mustBe false
      }
    }

    "when the screener is Yes, net takings standard is answered and MGD is unanswered" - {
      "must show the net takings value and an 'Enter MGD' link" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(NetTakingsStandardRatePage, true)
          .success
          .value
          .set(NetTakingsStandardPage, BigDecimal(123.45))
          .success
          .value

        val rows = StandardRateSummary.rows(answers)

        val netTakingsRow =
          rows.find(_.key.content == Text(msgs("submittedReturn.netTakingsStdRate"))).value

        val dutyDueRow =
          rows.find(_.key.content == Text(msgs("checkYourAnswers.totalDueStdRate"))).value

        netTakingsRow.actions.value.items.head.content mustBe Text(msgs("site.change"))
        netTakingsRow.actions.value.items.head.href mustBe netTakingsUrl

        dutyDueRow.value.content mustBe HtmlContent(
          s"""<a class="govuk-link" href="$mgdStandardUrl">${msgs("checkYourAnswers.enterMGD")}</a>"""
        )

        dutyDueRow.actions mustBe None
      }
    }

    "when the screener is Yes and both net takings and MGD are answered" - {
      "must show the net takings and MGD values" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(NetTakingsStandardRatePage, true)
          .success
          .value
          .set(NetTakingsStandardPage, BigDecimal(123.45))
          .success
          .value
          .set(MgdStandardRatePage, BigDecimal(50))
          .success
          .value

        val rows = StandardRateSummary.rows(answers)

        val netTakingsRow =
          rows.find(_.key.content == Text(msgs("submittedReturn.netTakingsStdRate"))).value

        val dutyDueRow =
          rows.find(_.key.content == Text(msgs("checkYourAnswers.totalDueStdRate"))).value

        netTakingsRow.actions.value.items.head.content mustBe Text(msgs("site.change"))
        netTakingsRow.actions.value.items.head.href mustBe netTakingsUrl

        dutyDueRow.value.content mustBe
          HtmlContent(views.CurrencyFormatter.formattedAmountHtml(BigDecimal(50)))

        dutyDueRow.actions.value.items.head.content mustBe Text(msgs("site.change"))
        dutyDueRow.actions.value.items.head.href mustBe mgdStandardUrl

        keys(rows) mustBe Seq(
          msgs("netTakingsStandardRate.question"),
          msgs("submittedReturn.netTakingsStdRate"),
          msgs("checkYourAnswers.totalDueStdRate")
        )
      }
    }
  }
}
