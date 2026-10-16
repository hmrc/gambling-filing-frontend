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
import models.CheckMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.ConfirmNumberOfMachinesInterrupterView

class ConfirmNumberOfMachinesInterrupterViewSpec extends SpecBase {

  "ConfirmNumberOfMachinesInterrupterView" - {

    "must render the page with correct heading, caption and input" in new Setup {

      val html: HtmlFormat.Appendable = view()
      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(messages("confirm.machines.interrupter.title"))
      doc.select("h1").text mustBe messages("confirm.machines.interrupter.heading")
      doc.select(".govuk-body").text mustBe messages("confirm.machines.interrupter.body")
      doc.select(".govuk-button").text mustBe messages("confirm.machines.interrupter.button.yes")
      doc.select("div.govuk-panel__actions div.govuk-button-group a.govuk-link").text mustBe messages("confirm.machines.interrupter.button.no")

      val yesForm = doc.select("div.govuk-panel__actions form")

      yesForm.isEmpty mustBe false
      yesForm.attr("method").equalsIgnoreCase("POST") mustBe true
      yesForm.attr("action") mustBe controllers.routes.ConfirmNumberOfMachinesInterrupterController.onSubmit().url

      val yesButton = yesForm.select("div.govuk-button-group button.govuk-button")

      yesButton.isEmpty mustBe false
      yesButton.text mustBe messages("confirm.machines.interrupter.button.yes")

      val noLink = doc.select("div.govuk-panel__actions div.govuk-button-group a.govuk-link")

      noLink.isEmpty mustBe false
      noLink.text mustBe messages("confirm.machines.interrupter.button.no")
      noLink.attr("href") mustBe controllers.routes.MachinesAvailableController.onPageLoad(CheckMode).url
      noLink.attr("target") mustBe ""
      noLink.attr("rel") mustBe ""
    }

    "must render the back link" in new Setup {

      val html = view()
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-back-link").attr("href") mustBe "#"
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    val view: ConfirmNumberOfMachinesInterrupterView = app.injector.instanceOf[ConfirmNumberOfMachinesInterrupterView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
