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
import models.SubmittedReturnSingle
import org.jsoup.Jsoup
import play.api.test.FakeRequest

import java.time.LocalDate

class SubmittedReturnViewSpec extends SpecBase {

  private val filedReturn = SubmittedReturnSingle(
    consecNo                     = 12345,
    mgdPeriod                    = "01/01/2025 - 30/03/2025",
    submittedDate                = LocalDate.of(2025, 4, 1),
    ackRef                       = "123456789012345",
    noOfMachines                 = 10,
    netTakingsHigherRate         = BigDecimal("5000.00"),
    netTakingsStdRate            = BigDecimal("3000.00"),
    netTakingsLowerRate          = BigDecimal("1000.00"),
    totalDueHigherRate           = BigDecimal("1500.00"),
    totalDueStdRate              = BigDecimal("600.00"),
    totalDueLowerRate            = BigDecimal("50.00"),
    dutyPayable                  = BigDecimal("2150.00"),
    underDeclaredDuty            = BigDecimal("1.99"),
    previousReturnAmount         = BigDecimal("2.90"),
    negativeAmountCarriedForward = BigDecimal("3.80"),
    totalNetDutyPayable          = BigDecimal("2150.00")
  )

  "SubmittedReturnView" - {

    "must render the page title" in {
      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SubmittedReturnView]
      val request = FakeRequest()

      val doc = Jsoup.parse(view(filedReturn)(request, messages(app)).body)

      doc.title() must include(messages(app)("submittedReturn.title"))
    }

    "must render the header text correctly" in {
      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SubmittedReturnView]
      val request = FakeRequest()

      val doc = Jsoup.parse(view(filedReturn)(request, messages(app)).body)
      val pageText = doc.body().text()

      pageText must include(messages(app)("submittedReturn.heading"))
      pageText must include("Return details for 1 Jan 2025 to 30 Mar 2025")
      pageText must include(messages(app)("submittedReturn.submissionDetails"))
      pageText must include(messages(app)("submittedReturn.returnDetails"))

      doc.select("[data-testid=submitted-return-heading]").text() mustEqual messages(app)("submittedReturn.heading")
    }

    "must render the submission details table with date submitted and acknowledgement reference" in {
      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SubmittedReturnView]
      val request = FakeRequest()

      val doc = Jsoup.parse(view(filedReturn)(request, messages(app)).body)
      val pageText = doc.body().text()

      pageText must include(messages(app)("submittedReturn.submittedDate"))
      pageText must include("1 Apr 2025")
      pageText must include(messages(app)("submittedReturn.ackRef"))
      pageText must include(filedReturn.ackRef)

      doc.select(s"[data-testid=submission-details-table]").text() must include(filedReturn.ackRef)
    }

    "must render the return details table with all fields" in {
      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.SubmittedReturnView]
      val request = FakeRequest()

      val doc = Jsoup.parse(view(filedReturn)(request, messages(app)).body)
      val pageText = doc.body().text()

      pageText must include(messages(app)("submittedReturn.noOfMachines"))
      pageText must include(filedReturn.noOfMachines.toString)
      pageText must include(messages(app)("submittedReturn.netTakingsLowerRate"))
      pageText must include(s"£${filedReturn.netTakingsLowerRate}")
      pageText must include(messages(app)("submittedReturn.totalDueLowerRate"))
      pageText must include(s"£${filedReturn.totalDueLowerRate}")
      pageText must include(messages(app)("submittedReturn.netTakingsStdRate"))
      pageText must include(s"£${filedReturn.netTakingsStdRate}")
      pageText must include(messages(app)("submittedReturn.totalDueStdRate"))
      pageText must include(s"£${filedReturn.totalDueStdRate}")
      pageText must include(messages(app)("submittedReturn.netTakingsHigherRate"))
      pageText must include(s"£${filedReturn.netTakingsHigherRate}")
      pageText must include(messages(app)("submittedReturn.totalDueHigherRate"))
      pageText must include(s"£${filedReturn.totalDueHigherRate}")
      pageText must include(messages(app)("submittedReturn.dutyPayable"))
      pageText must include(s"£${filedReturn.dutyPayable}")
      pageText must include(messages(app)("submittedReturn.underDeclaredDuty"))
      pageText must include(s"£${filedReturn.underDeclaredDuty}")
      pageText must include(messages(app)("submittedReturn.previousReturnAmount"))
      pageText must include(s"£${filedReturn.previousReturnAmount}")
      pageText must include(messages(app)("submittedReturn.negativeAmountCarriedForward"))
      pageText must include(s"£${filedReturn.negativeAmountCarriedForward}")
      pageText must include(messages(app)("submittedReturn.totalNetDutyPayable"))
      pageText must include(s"£${filedReturn.totalNetDutyPayable}")

      doc.select("[data-testid=return-details-table]").text() must include(s"£${filedReturn.totalNetDutyPayable}")
    }
  }
}
