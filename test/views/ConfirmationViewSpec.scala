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
import views.html.ConfirmationView

import java.time.LocalDate

class ConfirmationViewSpec extends SpecBase {

  "ConfirmationView" - {
    "must render the page with the correct content" in new Setup {

      val html = view(
        acknowledgementReference = acknowledgementReference,
        submissionDateTime       = submissionDateTime,
        periodStartDate          = selectedReturn.periodStart,
        periodEndDate            = selectedReturn.periodEnd
      )

      val doc = Jsoup.parse(html.body)

      doc.title     must include(messages("confirmation.title"))
      doc.body.text must include(messages("confirmation.heading"))

      doc.body.text must include(messages("confirmation.reference"))

      doc.body.text must include(acknowledgementReference)

      doc.body.text must include(
        messages("confirmation.whatHappensNext.heading")
      )

      doc.body.text must include(
        messages("confirmation.whatHappensNext.directDebit")
      )

      val helpdeskLink =
        doc.select("""a[href="https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"]""")

      helpdeskLink.text mustBe
        messages("confirmation.helpdesk.link")

      val accountLink =
        doc.select("""a[href="/manage-gambling-tax/"]""")
      accountLink.text mustBe
        messages("confirmation.returnToAccount")

    }
  }

  trait Setup {

    val app = applicationBuilder().build()
    val view = app.injector.instanceOf[ConfirmationView]
    val selectedReturn = SelectedReturn(
      LocalDate.of(2025, 1, 1),
      LocalDate.of(2025, 3, 31)
    )

    val acknowledgementReference =
      "4JTF BAXM GJXS TKM"

    val submissionDateTime =
      "6 June 2015 at 11:10 am"

    implicit val request: Request[?] =
      FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[MessagesApi]
        .preferred(request)
  }
}
