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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{Messages, MessagesImpl}
import play.api.test.Helpers.stubMessagesApi
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions}
import views.CurrencyFormatter

class CheckYourAnswersHelpersSpec extends AnyFreeSpec with Matchers {

  private val messagesApi = stubMessagesApi(
    Map(
      "en" -> Map(
        "site.change" -> "Change",
        "site.yes"    -> "Yes",
        "site.no"     -> "No",
        "some.key"    -> "Some key",
        "some.hidden" -> "Some hidden text"
      )
    )
  )

  implicit private val messages: Messages =
    MessagesImpl(play.api.i18n.Lang.defaultLang, messagesApi)

  ".yesNoRow" - {
    "must build a row with a Yes value and a change action" in {
      val result = CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "some.key",
        answer    = true,
        changeUrl = "/change-url",
        hiddenMsg = "some.hidden"
      )

      result.key.content mustBe Text("Some key")
      result.value.content mustBe Text("Yes")
      result.actions mustBe Some(
        Actions(items = Seq(ActionItem(href = "/change-url", content = Text("Change"), visuallyHiddenText = Some("Some hidden text"))))
      )
    }

    "must build a row with a No value when the answer is false" in {
      val result = CheckYourAnswersHelpers.yesNoRow(
        keyMsg    = "some.key",
        answer    = false,
        changeUrl = "/change-url",
        hiddenMsg = "some.hidden"
      )

      result.value.content mustBe Text("No")
    }
  }

  ".currencyRow" - {
    "must build a row with a formatted amount and a change action when changeUrl and hiddenMsg are provided" in {
      val result = CheckYourAnswersHelpers.currencyRow(
        keyMsg    = "some.key",
        amount    = BigDecimal(123.45),
        changeUrl = Some("/change-url"),
        hiddenMsg = Some("some.hidden")
      )

      result.key.content mustBe Text("Some key")
      result.value.content mustBe HtmlContent(CurrencyFormatter.formattedAmountHtml(BigDecimal(123.45)))
      result.actions mustBe Some(
        Actions(items = Seq(ActionItem(href = "/change-url", content = Text("Change"), visuallyHiddenText = Some("Some hidden text"))))
      )
    }

    "must build a read-only row with no actions when changeUrl and hiddenMsg are omitted" in {
      val result = CheckYourAnswersHelpers.currencyRow(keyMsg = "some.key", amount = BigDecimal(123.45))

      result.key.content mustBe Text("Some key")
      result.value.content mustBe HtmlContent(CurrencyFormatter.formattedAmountHtml(BigDecimal(123.45)))
      result.actions mustBe None
    }

    List(
      ("white spaces", " "),
      ("empty", "")
    ).foreach((scenario, string) =>
      s"must build a read-only row with no actions when changeUrl and hiddenMsg are $scenario" in {
        val result = CheckYourAnswersHelpers.currencyRow(
          keyMsg    = "some.key",
          amount    = BigDecimal(123.45),
          changeUrl = Some(string),
          hiddenMsg = Some(string)
        )

        result.key.content mustBe Text("Some key")
        result.value.content mustBe HtmlContent(CurrencyFormatter.formattedAmountHtml(BigDecimal(123.45)))
        result.actions mustBe None
      }
    )
  }

  ".textRow" - {
    "must build a row with the given text and a change action when changeUrl and hiddenMsg are provided" in {
      val result = CheckYourAnswersHelpers.textRow(
        keyMsg    = "some.key",
        answer    = "10",
        changeUrl = Some("/change-url"),
        hiddenMsg = Some("some.hidden")
      )

      result.key.content mustBe Text("Some key")
      result.value.content mustBe Text("10")
      result.actions mustBe Some(
        Actions(items = Seq(ActionItem(href = "/change-url", content = Text("Change"), visuallyHiddenText = Some("Some hidden text"))))
      )
    }

    "must build a read-only row with no actions when changeUrl and hiddenMsg are omitted" in {
      val result = CheckYourAnswersHelpers.textRow(keyMsg = "some.key", answer = "10")

      result.key.content mustBe Text("Some key")
      result.value.content mustBe Text("10")
      result.actions mustBe None
    }

    List(
      ("white spaces", " "),
      ("empty", "")
    ).foreach((scenario, string) =>
      s"must build a read-only row with no actions when changeUrl and hiddenMsg are $scenario" in {
        val result = CheckYourAnswersHelpers.textRow(
          keyMsg    = "some.key",
          answer    = "10",
          changeUrl = Some(string),
          hiddenMsg = Some(string)
        )

        result.key.content mustBe Text("Some key")
        result.value.content mustBe Text("10")
        result.actions mustBe None
      }
    )
  }
}
