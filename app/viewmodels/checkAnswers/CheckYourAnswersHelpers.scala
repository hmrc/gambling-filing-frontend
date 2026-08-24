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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist.*
import views.CurrencyFormatter

object CheckYourAnswersHelpers {

  def yesNoRow(keyMsg: String, answer: Boolean, changeUrl: String, hiddenMsg: String)(implicit
    messages: Messages
  ): SummaryListRow = {
    val msg = if (answer) "site.yes" else "site.no"
    SummaryListRowViewModel(
      key   = KeyViewModel(Text(messages(keyMsg))).withCssClass("govuk-!-width-one-half"),
      value = ValueViewModel(Text(messages(msg))).withCssClass("govuk-!-text-align-right"),
      actions = Seq(
        ActionItemViewModel(Text(messages("site.change")), changeUrl)
          .withVisuallyHiddenText(messages(hiddenMsg))
      )
    )
  }

  def currencyRow(
    keyMsg: String,
    amount: BigDecimal,
    changeUrl: Option[String] = None,
    hiddenMsg: Option[String] = None
  )(implicit messages: Messages): SummaryListRow = {
    val summary = SummaryListRowViewModel(
      key   = KeyViewModel(Text(messages(keyMsg))).withCssClass("govuk-!-width-one-half"),
      value = ValueViewModel(HtmlContent(CurrencyFormatter.formattedAmountHtml(amount))).withCssClass("govuk-!-text-align-right")
    )

    (changeUrl, hiddenMsg) match
      case (Some(url), Some(hidden)) if url.trim.nonEmpty && hidden.trim.nonEmpty =>
        summary.copy(actions =
          Some(Actions(items = Seq(ActionItemViewModel(Text(messages("site.change")), url).withVisuallyHiddenText(messages(hidden)))))
        )
      case _ => summary
  }

  def textRow(
    keyMsg: String,
    answer: String,
    changeUrl: Option[String] = None,
    hiddenMsg: Option[String] = None
  )(implicit messages: Messages): SummaryListRow = {
    val summary = SummaryListRowViewModel(
      key   = KeyViewModel(Text(messages(keyMsg))).withCssClass("govuk-!-width-one-half"),
      value = ValueViewModel(Text(answer)).withCssClass("govuk-!-text-align-right")
    )

    (changeUrl, hiddenMsg) match
      case (Some(url), Some(hidden)) if url.trim.nonEmpty && hidden.trim.nonEmpty =>
        summary.copy(actions =
          Some(Actions(items = Seq(ActionItemViewModel(Text(messages("site.change")), url).withVisuallyHiddenText(messages(hidden)))))
        )
      case _ => summary
  }
}
