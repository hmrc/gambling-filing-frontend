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
import models.SubmittedReturnsTestData.{validResponseSubmittedReturns, zeroResponseSubmittedReturns}
import org.jsoup.Jsoup
import play.api.test.FakeRequest

class SubmittedReturnsViewSpec extends SpecBase {

  private val regNumber = "XWM00003102200"

  "SubmittedReturnsView" - {

    "must render core SubmittedReturns details correctly" in {

      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SubmittedReturnsView]
      val request = FakeRequest()

      val base = validResponseSubmittedReturns

      val html = view(regNumber, base)(request, messages(app))
      val doc = Jsoup.parse(html.body)

      doc.title() must include(messages(app)("selectFiledReturn.title"))
      val pageText = doc.body().text()

      pageText must include("10 Feb 2024 to 29 Apr 2024")
      pageText must include("1 May 2024")
      pageText must include("111222111222")

      doc.select(".govuk-table__cell").text must include("111222111222")

      val links = doc.select(".govuk-table__cell a")
      links.size() mustBe validResponseSubmittedReturns.items.size

      validResponseSubmittedReturns.items.zipWithIndex.foreach { case (item, i) =>
        links.get(i).attr("href") mustEqual controllers.routes.SubmittedReturnsController.viewFiledReturn(item.consec_no).url
      }
    }

    "must render no SubmittedReturns details correctly when no data" in {

      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SubmittedReturnsView]
      val request = FakeRequest()

      val base = zeroResponseSubmittedReturns

      val html = view(regNumber, base)(request, messages(app))
      val doc = Jsoup.parse(html.body)

      doc.title() must include(messages(app)("selectFiledReturn.title"))
      val pageText = doc.body().text()

      pageText must include("No filed returns")
      pageText must include("You have not filed any Machine Game Duty returns.")
    }
  }
}
