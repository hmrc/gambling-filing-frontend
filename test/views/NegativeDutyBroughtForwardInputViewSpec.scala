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

package views

import base.SpecBase
import forms.NegativeDutyBroughtForwardInputFormProvider
import models.{NormalMode, SelectedReturn}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.NegativeDutyBroughtForwardInputView

import java.time.LocalDate

class NegativeDutyBroughtForwardInputViewSpec extends SpecBase {

  "NegativeDutyBroughtForwardInputView" - {

    "must render the page with correct heading, caption, hint and input" in new Setup {

      val selectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))
      val html = view(form, NormalMode, None, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("negativeDutyBroughtForwardInput.title"))
      doc.select("h1").text mustBe messages("negativeDutyBroughtForwardInput.heading")
      doc.select(".govuk-caption-l").text mustBe messages("negativeDutyBroughtForwardInput.caption", "1 Jan 2025", "31 Mar 2025")
      doc.select(".govuk-hint").text mustBe messages("negativeDutyBroughtForwardInput.hint")
      doc.select(".govuk-input__prefix").text mustBe "£"
      doc.select("input.govuk-input").hasClass("govuk-input--width-20") mustBe true
      doc.select("button").text mustBe messages("site.continue")
    }

    "must render error summary and inline error when no amount is entered" in new Setup {

      val boundForm = form.bind(Map("value" -> ""))
      val html = view(boundForm, NormalMode, None, SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)))
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").isEmpty mustBe false
      doc.select(".govuk-error-summary__list a").text must include(messages("negativeDutyBroughtForwardInput.error.required"))
      doc.select("#value-error").text                 must include(messages("negativeDutyBroughtForwardInput.error.required"))
    }

    "must render an invalid format error when a non-numeric amount is entered" in new Setup {

      val boundForm = form.bind(Map("value" -> "not a number"))
      val html = view(boundForm, NormalMode, None, SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)))
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary__list a").text must include(messages("negativeDutyBroughtForwardInput.error.invalid"))
    }

    "must render a range error when a positive amount is entered" in new Setup {

      val boundForm = form.bind(Map("value" -> "100.00"))
      val html = view(boundForm, NormalMode, None, SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)))
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary__list a").text must include(messages("negativeDutyBroughtForwardInput.error.range"))
    }

    "must populate the input when form has a value" in new Setup {

      val boundForm = form.fill(BigDecimal("-123.45"))
      val html = view(boundForm, NormalMode, None, SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)))
      val doc = Jsoup.parse(html.body)

      doc.select("#value").`val` mustBe "-123.45"
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val view = app.injector.instanceOf[NegativeDutyBroughtForwardInputView]
    val form = new NegativeDutyBroughtForwardInputFormProvider()()

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
