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
import forms.MachinesAvailableFormProvider
import models.{SelectedReturn, NormalMode}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.MachinesAvailableView

import java.time.LocalDate

class MachinesAvailableViewSpec extends SpecBase {

  "MachinesAvailableView" - {

    "must render the page with correct heading, caption and input" in new Setup {

      val selectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))
      val html = view(form, NormalMode, selectedReturn)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("machinesAvailable.title"))
      doc.select("h1").text mustBe messages("machinesAvailable.heading")
      doc.select(".govuk-caption-l").text mustBe messages(
        "machinesAvailable.caption",
        "1 Jan 2025",
        "31 Mar 2025"
      )
      doc.select("input.govuk-input").hasClass("govuk-input--width-10") mustBe true
      doc.select("button").text mustBe messages("site.continue")
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm = form.bind(Map("value" -> ""))
      val html = view(boundForm, NormalMode, SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)))
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").isEmpty mustBe false
      doc.select(".govuk-error-summary__list a").text must include(messages("machinesAvailable.error.required"))
    }

    "must populate the input when form has a value" in new Setup {

      val boundForm = form.fill(10)
      val html = view(boundForm, NormalMode, SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)))
      val doc = Jsoup.parse(html.body)

      doc.select("#value").`val` mustBe "10"
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val view = app.injector.instanceOf[MachinesAvailableView]
    val form = new MachinesAvailableFormProvider()()

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
