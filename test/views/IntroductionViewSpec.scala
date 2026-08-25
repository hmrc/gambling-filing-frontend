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
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.IntroductionView

import java.time.LocalDate

class IntroductionViewSpec extends SpecBase {

  "IntroductionView" - {

    "must render the heading with the selected return period, intro content and continue button" in new Setup {

      val selectedReturn = SelectedReturn(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 12, 31))
      val html = view(selectedReturn, guidanceUrl, Some("/back"))
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("introduction.title"))
      doc.select("h1").text mustBe messages("introduction.heading", "1 Jan 2014", "31 Dec 2014")
      doc.select("p.govuk-body").first.text mustBe messages("introduction.p1")

      val bullets = doc.select("ul.govuk-list--bullet li")
      bullets.size mustBe 6
      bullets.get(0).text mustBe messages("introduction.bullet.1")
      bullets.get(5).text mustBe messages("introduction.bullet.6")

      doc.select("h2").text mustBe messages("introduction.underDeclared.h2")
      doc.select("button").text mustBe messages("site.continue")
    }

    "must render the guidance link opening in a new tab" in new Setup {

      val selectedReturn = SelectedReturn(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 12, 31))
      val html = view(selectedReturn, guidanceUrl, None)
      val doc = Jsoup.parse(html.body)

      val guidanceLink = doc.select(s"a.govuk-link[href=$guidanceUrl]")
      guidanceLink.isEmpty mustBe false
      guidanceLink.text mustBe messages("introduction.underDeclared.p.link")
      guidanceLink.attr("target") mustBe "_blank"
      guidanceLink.attr("rel") mustBe "noreferrer noopener"
    }

    "must render the back link" in new Setup {

      val selectedReturn = SelectedReturn(LocalDate.of(2014, 1, 1), LocalDate.of(2014, 12, 31))
      val html = view(selectedReturn, guidanceUrl, Some("/back"))
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-back-link").attr("href") mustBe "/back"
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val view = app.injector.instanceOf[IntroductionView]
    val guidanceUrl = "https://www.gov.uk/guidance/machine-games-duty-excise-notice-452#underpayment-of-duty"

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
