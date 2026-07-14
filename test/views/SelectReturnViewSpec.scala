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
import models.SelectReturnTestData.{validResponseOpenReturns, zeroResponseOpenReturns}
import org.jsoup.Jsoup
import play.api.test.FakeRequest

class SelectReturnViewSpec extends SpecBase {

  private val regNumber = "XWM00003102200"

  "OpenReturnsView" - {

    "must render OpenReturns details correctly" in {

      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SelectReturnView]
      val request = FakeRequest()

      val base = validResponseOpenReturns

      val html = view(regNumber, base)(request, messages(app))
      val doc = Jsoup.parse(html.body)

      doc.title() must include(messages(app)("selectReturn.title"))
      val pageText = doc.body().text()

      pageText must include("1 Jul 2025 to 30 Sep 2025")
      pageText must include("31 Oct 2025")
      pageText must include("Open")

      pageText must include("1 Apr 2025 to 30 Jun 2025")
      pageText must include("31 Jul 2025")
      pageText must include("Overdue")

      doc.select(".govuk-table__row").size() mustBe validResponseOpenReturns.openPeriods.size + 1

      val openTag = doc.select("[data-testid=open-returns-status-0] strong")
      openTag.attr("class") mustEqual "govuk-tag"
      openTag.text() mustEqual "Open"

      val overdueTag = doc.select("[data-testid=open-returns-status-1] strong")
      overdueTag.attr("class") mustEqual "govuk-tag govuk-tag--red"
      overdueTag.text() mustEqual "Overdue"

      val links = doc.select(".govuk-table__cell a.govuk-link")
      links.size() mustBe validResponseOpenReturns.openPeriods.size

      links.get(0).attr("href") mustEqual controllers.routes.SelectReturnController
        .selectOpenPeriod(validResponseOpenReturns.openPeriods.head.consecNo)
        .url
      links.get(0).text() mustEqual "1 Jul 2025 to 30 Sep 2025"
    }

    "must render view with correct title, heading and body when no data" in {

      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SelectReturnView]
      val request = FakeRequest()

      val base = zeroResponseOpenReturns

      val html = view(regNumber, base)(request, messages(app))
      val doc = Jsoup.parse(html.body)

      doc.title() must include(messages(app)("selectReturn.empty.title"))
      val pageText = doc.body().text()

      pageText must include(messages(app)("selectReturn.empty.heading"))
      pageText must include(messages(app)("selectReturn.empty.body"))
    }
  }
}
