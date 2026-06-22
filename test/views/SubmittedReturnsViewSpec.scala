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

      val html = view(regNumber, base, 2, "DESC")(request, messages(app))
      val doc = Jsoup.parse(html.body)

      doc.title() must include(messages(app)("selectFiledReturn.title"))
      val pageText = doc.body().text()

      pageText must include("10/02/2024 - 29/04/2024")
      pageText must include("1 May 2024")
      pageText must include("111222111222")

      doc.select(".govuk-table__cell").text must include("111222111222")

      doc.select("a[href='#']").attr("href") mustBe "#"
    }

    "must render no SubmittedReturns details correctly when no data" in {

      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SubmittedReturnsView]
      val request = FakeRequest()

      val base = zeroResponseSubmittedReturns

      val html = view(regNumber, base, 2, "DESC")(request, messages(app))
      val doc = Jsoup.parse(html.body)

      doc.title() must include(messages(app)("selectFiledReturn.title"))
      val pageText = doc.body().text()

      pageText must include("No submitted returns")
      pageText must include("You have not submitted any Machine Game Duty returns.")

    }
  }
}
