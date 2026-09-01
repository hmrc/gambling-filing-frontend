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
import models.SelectedReturn
import org.jsoup.Jsoup
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.ContactHmrcView

import java.time.LocalDate

class ContactHmrcViewSpec extends SpecBase {

  "ContactHmrcView" - {
    "must render the page with the correct content" in new Setup {

      val html = view(selectedReturn, contactHmrcUrl, continueUrl)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("contactHmrc.title"))
      doc.select("h1").text mustBe messages("contactHmrc.heading")

      doc.select(".govuk-caption-l").text mustBe
        messages("contactHmrc.caption", "1 Jan 2025", "31 Mar 2025")

      val paragraphs = doc.select("p.govuk-body")

      paragraphs.get(0).text must include(messages("contactHmrc.p1"))
      paragraphs.get(0).text must include(messages("contactHmrc.link"))
      paragraphs.get(0).text must include(messages("contactHmrc.p2"))

      paragraphs.get(1).text mustBe
        messages("contactHmrc.p3")

      val link = doc.select(s"a.govuk-link[href='$contactHmrcUrl']")

      link.text mustBe messages("contactHmrc.link")
      link.attr("href") mustBe contactHmrcUrl
      link.attr("target") mustBe "_blank"
      link.attr("rel") mustBe "noopener noreferrer"

      val button = doc.select(".govuk-button")

      button.text mustBe messages("site.continue")
      button.attr("href") mustBe continueUrl

    }
  }

  trait Setup {

    val app = applicationBuilder().build()
    val view = app.injector.instanceOf[ContactHmrcView]
    val selectedReturn = SelectedReturn(
      LocalDate.of(2025, 1, 1),
      LocalDate.of(2025, 3, 31)
    )
    val contactHmrcUrl =
      "https://www.gov.uk/find-hmrc-contacts/gambling-duties-enquiries"
    val continueUrl =
      controllers.routes.IndexController.onPageLoad().url

    implicit val request: Request[?] =
      FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[MessagesApi]
        .preferred(request)
  }
}
