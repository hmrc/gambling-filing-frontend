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
import pages.{NegativeDutyBroughtForwardInputPage, NegativeDutyPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import views.CurrencyFormatter

class DutyBroughtForwardSummarySpec extends SpecBase {

  private def negativeDutyUrl = routes.NegativeDutyController.onPageLoad(CheckMode).url
  private def amountBroughtForwardUrl = routes.NegativeDutyBroughtForwardInputController.onPageLoad(CheckMode).url

  private def keys(rows: Seq[uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow]): Seq[String] =
    rows.map(_.key.content.asInstanceOf[Text].value)

  "DutyBroughtForwardSummary" - {

    "must show the negative duty row with a 'Set value' link, and hide the amount row, when unanswered" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val rows = DutyBroughtForwardSummary.rows(emptyUserAnswers)
      val negativeDutyRow = rows.find(_.key.content == Text(msgs("negativeDuty.question"))).value

      negativeDutyRow.value.content mustBe HtmlContent(s"""<a class="govuk-link" href="$negativeDutyUrl">${msgs("checkYourAnswers.setValue")}</a>""")
      negativeDutyRow.actions mustBe None

      keys(rows) mustNot contain(msgs("submittedReturn.previousReturnAmount"))
    }

    "must show the negative duty row with the normal 'No' value and a change action when answered No" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val answers = emptyUserAnswers.set(NegativeDutyPage, false).success.value

      val rows = DutyBroughtForwardSummary.rows(answers)
      val negativeDutyRow = rows.find(_.key.content == Text(msgs("negativeDuty.question"))).value

      negativeDutyRow.value.content mustBe Text(msgs("site.no"))
      negativeDutyRow.actions.value.items.head.content mustBe Text(msgs("site.change"))
      negativeDutyRow.actions.value.items.head.href mustBe negativeDutyUrl

      keys(rows) mustNot contain(msgs("submittedReturn.previousReturnAmount"))
    }

    "must show both rows when answered Yes and the amount has been submitted" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val answers = emptyUserAnswers
        .set(NegativeDutyPage, true)
        .success
        .value
        .set(NegativeDutyBroughtForwardInputPage, BigDecimal(123.45))
        .success
        .value

      val rows = DutyBroughtForwardSummary.rows(answers)
      val negativeDutyRow = rows.find(_.key.content == Text(msgs("negativeDuty.question"))).value
      val amountRow = rows.find(_.key.content == Text(msgs("submittedReturn.previousReturnAmount"))).value

      negativeDutyRow.value.content mustBe Text(msgs("site.yes"))
      negativeDutyRow.actions.value.items.head.content mustBe Text(msgs("site.change"))

      amountRow.value.content mustBe HtmlContent(CurrencyFormatter.formattedAmountHtml(BigDecimal(-123.45)))
      amountRow.actions.value.items.head.content mustBe Text(msgs("site.change"))
      amountRow.actions.value.items.head.href mustBe amountBroughtForwardUrl

      keys(rows) mustBe Seq(
        msgs("negativeDuty.question"),
        msgs("submittedReturn.previousReturnAmount")
      )
    }

    "must show the amount row with an 'Enter amount' link when answered Yes but the amount is unanswered" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val answers = emptyUserAnswers.set(NegativeDutyPage, true).success.value

      val rows = DutyBroughtForwardSummary.rows(answers)
      val amountRow = rows.find(_.key.content == Text(msgs("submittedReturn.previousReturnAmount"))).value

      amountRow.value.content mustBe HtmlContent(
        s"""<a class="govuk-link" href="$amountBroughtForwardUrl">${msgs("checkYourAnswers.enterAmount")}</a>"""
      )
      amountRow.actions mustBe None
    }

    "must show the amount row with an 'Enter amount' link when answered Yes and the amount is zero" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val answers = emptyUserAnswers
        .set(NegativeDutyPage, true)
        .success
        .value
        .set(NegativeDutyBroughtForwardInputPage, BigDecimal(0))
        .success
        .value

      val rows = DutyBroughtForwardSummary.rows(answers)
      val amountRow = rows.find(_.key.content == Text(msgs("submittedReturn.previousReturnAmount"))).value

      amountRow.value.content mustBe HtmlContent(
        s"""<a class="govuk-link" href="$amountBroughtForwardUrl">${msgs("checkYourAnswers.enterAmount")}</a>"""
      )
      amountRow.actions mustBe None
    }
  }
}
