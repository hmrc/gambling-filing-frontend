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
import models.DeclaredSubmissionTestData.validResponseDeclaredSubmission
import models.{NormalMode, SelectedReturn}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.DeclareAndSubmitView

import java.time.LocalDate

class DeclareAndSubmitViewSpec extends SpecBase {

  "DeclareAndSubmitView" - {

    "must render the page with correct heading, caption and table" in new Setup {

      private val selectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))
      private val html = view(NormalMode, None, selectedReturn, validResponseDeclaredSubmission)
      private val doc = Jsoup.parse(html.body)

      doc.title must include(messages("declareAndSubmit.title"))
      doc.select("h1").text mustBe messages("declareAndSubmit.heading")
      doc.select(".govuk-caption-l").text mustBe messages(
        "declareAndSubmit.caption",
        "1 Jan 2025",
        "31 Mar 2025"
      )
      doc.select("main.govuk-main-wrapper div div table").hasClass("govuk-table") mustBe true
      doc.select("button").text mustBe messages("declareAndSubmit.button.submit")

      doc.select("main.govuk-main-wrapper div div table tr.govuk-table__row").size() mustBe 4
      val rows: Elements = doc.select("main.govuk-main-wrapper div div table tr.govuk-table__row")
      rows.get(0).select("td").get(0).text mustBe messages("declareAndSubmit.dutyPayableBeforeAdjustments")
      rows.get(0).select("td").get(1).hasClass("govuk-!-font-weight-bold") mustBe false
      rows.get(1).select("td").get(0).text mustBe messages("declareAndSubmit.underDeclaredTaxFromPreviousPeriods")
      rows.get(1).select("td").get(1).hasClass("govuk-!-font-weight-bold") mustBe false
      rows.get(2).select("td").get(0).text mustBe messages("declareAndSubmit.amountBroughtForward")
      rows.get(2).select("td").get(1).hasClass("govuk-!-font-weight-bold") mustBe false
      rows.get(3).select("td").get(0).text mustBe messages("declareAndSubmit.netMGDPayableOnThisReturn")
      rows.get(3).select("td").get(1).hasClass("govuk-!-font-weight-bold") mustBe true

      doc.select("main.govuk-main-wrapper div div p").hasClass("govuk-body") mustBe true
      doc.select("main.govuk-main-wrapper div div p.govuk-body").text mustBe messages("declareAndSubmit.confirmation.p")
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    val view: DeclareAndSubmitView = app.injector.instanceOf[DeclareAndSubmitView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
