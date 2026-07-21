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
import forms.CalculatedMGDHigherRateFormProvider
import models.{NormalMode, SelectedReturn}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.CalculatedMGDHigherRateView

import java.time.LocalDate

class CalculatedMGDHigherRateViewSpec extends SpecBase {

  "CalculatedMGDHigherRateView" - {

    "must render the page with correct heading, caption and input" in new Setup {
      val html = view(form, netTakings, BigDecimal(200), percentage, NormalMode, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.title must include("MGD for the higher rate of duty")
      doc.select("h1").text mustBe "We have worked out your MGD at the higher rate to be £200"

      doc.select(".govuk-caption-l").text mustBe "File a return for 1 Jan 2025 to 31 Mar 2025"
      doc.select("p.govuk-body").text mustBe "This is based on 20% of your declared net takings of £1,000"
      doc.select("button").text mustBe "Continue"
    }

    "must render error summary when form has errors" in new Setup {
      val boundForm = form.bind(Map("value" -> ""))
      val html = view(boundForm, netTakings, BigDecimal(200), percentage, NormalMode, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").isEmpty mustBe false
      doc.select(".govuk-error-summary__list a").text must include("Select yes if this calculation is correct")
    }

    "must populate the input when form has a value" in new Setup {
      val boundForm = form.fill(true)
      val html = view(boundForm, netTakings, BigDecimal(200), percentage, NormalMode, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.select("input[value=true]").hasAttr("checked") mustBe true
    }

    "must display a positive duty amount correctly in the heading" in new Setup {
      val html = view(form, netTakings, BigDecimal(200), percentage, NormalMode, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.select("h1").text mustBe "We have worked out your MGD at the higher rate to be £200"
    }

    "must display a negative duty amount correctly in the heading" in new Setup {
      val html = view(form, netTakings, BigDecimal(-200), percentage, NormalMode, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.select("h1").text mustBe "We have worked out your MGD at the higher rate to be −£200"
    }

    "must display a positive net takings amount correctly in the body" in new Setup {
      val html = view(form, BigDecimal(1000), BigDecimal(200), percentage, NormalMode, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.select("p.govuk-body").text mustBe "This is based on 20% of your declared net takings of £1,000"
    }

    "must display a negative net takings amount correctly in the body" in new Setup {
      val html = view(form, BigDecimal(-1000), BigDecimal(200), percentage, NormalMode, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.select("p.govuk-body").text mustBe "This is based on 20% of your declared net takings of −£1,000"
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val view = app.injector.instanceOf[CalculatedMGDHigherRateView]
    val form = new CalculatedMGDHigherRateFormProvider()()
    val selectedReturn: SelectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))
    val percentage: BigDecimal = 20
    val netTakings: BigDecimal = BigDecimal(1000)

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
