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
import pages.{TotalUnderDeclaredDutyPage, UnderDeclaredDutyLimitsPage, UnderDeclaredDutyPage, UnderDeclaredDutyReasonableCarePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import views.CurrencyFormatter

class UnderDeclaredDutySummarySpec extends SpecBase {

  private def hasUnderDeclaredDutyUrl = routes.UnderDeclaredDutyController.onPageLoad(CheckMode).url
  private def reasonableCareUrl = routes.UnderDeclaredDutyReasonableCareController.onPageLoad(CheckMode).url
  private def withinLimitsUrl = routes.UnderDeclaredDutyLimitsController.onPageLoad(CheckMode).url
  private def totalUnderDeclaredDutyUrl = routes.TotalUnderDeclaredDutyController.onPageLoad(CheckMode).url

  private def keys(rows: Seq[uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow]): Seq[String] =
    rows.map(_.key.content.asInstanceOf[Text].value)

  "UnderDeclaredDutySummary" - {

    "must show the screener row with a 'Set value' link, and hide the other rows, when the screener is unanswered" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val rows = UnderDeclaredDutySummary.rows(emptyUserAnswers)
      val hasUnderDeclaredDutyRow = rows.find(_.key.content == Text(msgs("underDeclaredDuty.heading"))).value

      hasUnderDeclaredDutyRow.value.content mustBe HtmlContent(
        s"""<a class="govuk-link" href="$hasUnderDeclaredDutyUrl">${msgs("checkYourAnswers.setValue")}</a>"""
      )
      hasUnderDeclaredDutyRow.actions mustBe None

      keys(rows) mustNot contain(msgs("underDeclaredDutyReasonableCare.heading"))
      keys(rows) mustNot contain(msgs("underDeclaredDutyLimits.question"))
      keys(rows) mustNot contain(msgs("totalUnderDeclaredDuty.heading"))
    }

    "must show the screener row with the normal 'No' value, and hide the other rows, when the screener is No" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val answers = emptyUserAnswers
        .set(UnderDeclaredDutyPage, false)
        .success
        .value
        .set(UnderDeclaredDutyReasonableCarePage, true)
        .success
        .value
        .set(UnderDeclaredDutyLimitsPage, true)
        .success
        .value
        .set(TotalUnderDeclaredDutyPage, BigDecimal(250))
        .success
        .value

      val rows = UnderDeclaredDutySummary.rows(answers)
      val hasUnderDeclaredDutyRow = rows.find(_.key.content == Text(msgs("underDeclaredDuty.heading"))).value

      hasUnderDeclaredDutyRow.value.content mustBe Text(msgs("site.no"))
      hasUnderDeclaredDutyRow.actions.value.items.head.href mustBe hasUnderDeclaredDutyUrl

      keys(rows) mustNot contain(msgs("underDeclaredDutyReasonableCare.heading"))
      keys(rows) mustNot contain(msgs("underDeclaredDutyLimits.question"))
      keys(rows) mustNot contain(msgs("totalUnderDeclaredDuty.heading"))
    }

    "when the screener is Yes" - {

      "must show the reasonable care row with a 'Set value' link, and hide within limits and the amount row, when reasonable care is unanswered" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers.set(UnderDeclaredDutyPage, true).success.value

        val rows = UnderDeclaredDutySummary.rows(answers)
        val reasonableCareRow = rows.find(_.key.content == Text(msgs("underDeclaredDutyReasonableCare.heading"))).value

        reasonableCareRow.value.content mustBe HtmlContent(
          s"""<a class="govuk-link" href="$reasonableCareUrl">${msgs("checkYourAnswers.setValue")}</a>"""
        )
        reasonableCareRow.actions mustBe None

        keys(rows) mustNot contain(msgs("underDeclaredDutyLimits.question"))
        keys(rows) mustNot contain(msgs("totalUnderDeclaredDuty.heading"))
      }

      "must hide within limits when reasonable care is answered Yes" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, true)
          .success
          .value
          .set(UnderDeclaredDutyLimitsPage, false)
          .success
          .value

        val rows = UnderDeclaredDutySummary.rows(answers)
        val reasonableCareRow = rows.find(_.key.content == Text(msgs("underDeclaredDutyReasonableCare.heading"))).value

        reasonableCareRow.value.content mustBe Text(msgs("site.yes"))
        reasonableCareRow.actions.value.items.head.href mustBe reasonableCareUrl

        keys(rows) mustNot contain(msgs("underDeclaredDutyLimits.question"))
        keys(rows) mustNot contain(msgs("totalUnderDeclaredDuty.heading"))
      }

      "must show the within limits row with a 'Set value' link when reasonable care is answered No and within limits is unanswered" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, false)
          .success
          .value

        val rows = UnderDeclaredDutySummary.rows(answers)
        val withinLimitsRow = rows.find(_.key.content == Text(msgs("underDeclaredDutyLimits.question"))).value

        withinLimitsRow.value.content mustBe HtmlContent(
          s"""<a class="govuk-link" href="$withinLimitsUrl">${msgs("checkYourAnswers.setValue")}</a>"""
        )
        withinLimitsRow.actions mustBe None
      }

      "must show the normal 'No' value with a change action for reasonable care and within limits once answered" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, false)
          .success
          .value
          .set(UnderDeclaredDutyLimitsPage, false)
          .success
          .value

        val rows = UnderDeclaredDutySummary.rows(answers)
        val reasonableCareRow = rows.find(_.key.content == Text(msgs("underDeclaredDutyReasonableCare.heading"))).value
        val withinLimitsRow = rows.find(_.key.content == Text(msgs("underDeclaredDutyLimits.question"))).value

        reasonableCareRow.value.content mustBe Text(msgs("site.no"))
        reasonableCareRow.actions.value.items.head.href mustBe reasonableCareUrl

        withinLimitsRow.value.content mustBe Text(msgs("site.no"))
        withinLimitsRow.actions.value.items.head.href mustBe withinLimitsUrl
      }

      "must show the amount row when it has been submitted" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, false)
          .success
          .value
          .set(UnderDeclaredDutyLimitsPage, true)
          .success
          .value
          .set(TotalUnderDeclaredDutyPage, BigDecimal(250))
          .success
          .value

        val rows = UnderDeclaredDutySummary.rows(answers)
        val amountRow = rows.find(_.key.content == Text(msgs("totalUnderDeclaredDuty.heading"))).value

        amountRow.value.content mustBe HtmlContent(CurrencyFormatter.formattedAmountHtml(BigDecimal(250)))
        amountRow.actions.value.items.head.content mustBe Text(msgs("site.change"))
        amountRow.actions.value.items.head.href mustBe totalUnderDeclaredDutyUrl

        keys(rows) mustBe Seq(
          msgs("underDeclaredDuty.heading"),
          msgs("underDeclaredDutyReasonableCare.heading"),
          msgs("underDeclaredDutyLimits.question"),
          msgs("totalUnderDeclaredDuty.heading")
        )
      }

      "must show the amount row with an 'Enter amount' link when it is unanswered even if the other rows are answered" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, false)
          .success
          .value
          .set(UnderDeclaredDutyLimitsPage, true)
          .success
          .value

        val rows = UnderDeclaredDutySummary.rows(answers)
        val amountRow = rows.find(_.key.content == Text(msgs("totalUnderDeclaredDuty.heading"))).value

        amountRow.value.content mustBe HtmlContent(
          s"""<a class="govuk-link" href="$totalUnderDeclaredDutyUrl">${msgs("checkYourAnswers.enterAmount")}</a>"""
        )
        amountRow.actions mustBe None
      }

      "must show the amount row with an 'Enter amount' link when the amount is zero" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, false)
          .success
          .value
          .set(UnderDeclaredDutyLimitsPage, true)
          .success
          .value
          .set(TotalUnderDeclaredDutyPage, BigDecimal(0))
          .success
          .value

        val rows = UnderDeclaredDutySummary.rows(answers)
        val amountRow = rows.find(_.key.content == Text(msgs("totalUnderDeclaredDuty.heading"))).value

        amountRow.value.content mustBe HtmlContent(
          s"""<a class="govuk-link" href="$totalUnderDeclaredDutyUrl">${msgs("checkYourAnswers.enterAmount")}</a>"""
        )
        amountRow.actions mustBe None
      }

      "must hide the amount row when within limits is answered No, even if a stale amount is still present" in {
        implicit val msgs: Messages = messages(applicationBuilder().build())

        val answers = emptyUserAnswers
          .set(UnderDeclaredDutyPage, true)
          .success
          .value
          .set(UnderDeclaredDutyReasonableCarePage, false)
          .success
          .value
          .set(UnderDeclaredDutyLimitsPage, false)
          .success
          .value
          .set(TotalUnderDeclaredDutyPage, BigDecimal(250))
          .success
          .value

        val rows = UnderDeclaredDutySummary.rows(answers)

        keys(rows) mustNot contain(msgs("totalUnderDeclaredDuty.heading"))
      }
    }
  }
}
