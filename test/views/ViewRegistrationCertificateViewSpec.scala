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
import models.{GroupMember, MgdCertificate, PartnerMember, ReturnPeriodEndDate}
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import java.time.LocalDate

class ViewRegistrationCertificateViewSpec extends SpecBase {

  private val managementUrl = "http://localhost:10400/gambling/"

  private val certificate = MgdCertificate(
    mgdRegNumber       = "MGD123",
    registrationDate   = Some(LocalDate.parse("2026-01-01")),
    individualName     = Some("John Doe"),
    businessName       = Some("Test Business Ltd"),
    tradingName        = None,
    repMemName         = None,
    busAddrLine1       = Some("Line 1"),
    busAddrLine2       = Some("Line 2"),
    busAddrLine3       = None,
    busAddrLine4       = None,
    busPostcode        = Some("AB1 2CD"),
    busCountry         = None,
    busAdi             = None,
    repMemLine1        = None,
    repMemLine2        = None,
    repMemLine3        = None,
    repMemLine4        = None,
    repMemPostcode     = None,
    repMemAdi          = None,
    typeOfBusiness     = Some("Limited Company"),
    businessTradeClass = Some(1),
    noOfPartners       = None,
    groupReg           = "N",
    noOfGroupMems      = None,
    dateCertIssued     = Some(LocalDate.parse("2026-01-02")),
    partMembers        = Seq.empty[PartnerMember],
    groupMembers       = Seq.empty[GroupMember],
    returnPeriodEndDates = Seq(
      ReturnPeriodEndDate(LocalDate.parse("2026-12-31"))
    )
  )

  "ViewRegistrationCertificateView" - {

    "must render page correctly with dynamic summary list rows" in {

      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.ViewRegistrationCertificateView]
      val request = FakeRequest()

      val displayName = certificate.individualName.getOrElse("-")
      val displayLabelKey = "viewRegistrationCertificate.soleProprietorName"
      val formattedAddress = Seq(
        certificate.busAddrLine1,
        certificate.busAddrLine2,
        certificate.busPostcode
      ).flatten.mkString("<br>")

      val html = view(certificate, managementUrl, displayName, displayLabelKey, formattedAddress)(request, messages(app))
      val doc = Jsoup.parse(html.body)

      doc.title                             must include(messages(app)("viewRegistrationCertificate.title"))
      doc.select(".govuk-panel__body").text must include("MGD123")

      val pageText = doc.body().text()
      pageText must include(displayName)
      pageText must include("Limited Company")
      pageText must include("Line 1")
      pageText must include("Line 2")
      pageText must include("AB1 2CD")

      doc.select(".govuk-list li").text.trim must include("31 Dec 2026")

      val dashboardLink = doc.select(".dashboard-page-link a")
      dashboardLink.size() mustBe 1
      dashboardLink.attr("href") mustBe managementUrl

      val changeLink = doc.select(".change-registration-page-link a")
      changeLink.size() mustBe 1
      changeLink.attr("href") mustBe "#"

      if (certificate.businessTradeClass.isDefined) {
        pageText must include(certificate.businessTradeClass.get.toString)
      } else {
        pageText must not include "Trade class"
      }
    }
  }
}
