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

package controllers
import base.SpecBase
import models.{GroupMember, MgdCertificate, PartnerMember, ReturnPeriodEndDate}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.MgdCertificateService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ViewRegistrationCertificateView

import java.time.LocalDate
import scala.concurrent.Future

class ViewRegistrationCertificateControllerSpec extends SpecBase with MockitoSugar {

  private val certificate = MgdCertificate(
    mgdRegNumber         = "MGD123",
    registrationDate     = Some(LocalDate.parse("2026-01-01")),
    individualName       = None,
    businessName         = Some("Test Business Ltd"),
    tradingName          = None,
    repMemName           = None,
    busAddrLine1         = None,
    busAddrLine2         = None,
    busAddrLine3         = None,
    busAddrLine4         = None,
    busPostcode          = None,
    busCountry           = None,
    busAdi               = None,
    repMemLine1          = None,
    repMemLine2          = None,
    repMemLine3          = None,
    repMemLine4          = None,
    repMemPostcode       = None,
    repMemAdi            = None,
    typeOfBusiness       = Some("Limited Company"),
    businessTradeClass   = Some(1),
    noOfPartners         = None,
    groupReg             = "N",
    noOfGroupMems        = None,
    dateCertIssued       = Some(LocalDate.parse("2026-01-02")),
    partMembers          = Seq.empty[PartnerMember],
    groupMembers         = Seq.empty[GroupMember],
    returnPeriodEndDates = Seq.empty[ReturnPeriodEndDate]
  )

  "ViewRegistrationCertificateController" - {

    "must return OK and render certificate view when service succeeds" in {

      val mockService = mock[MgdCertificateService]

      when(
        mockService.retrieveCertificate(any[String])(
          any[HeaderCarrier],
          any[play.api.mvc.Request[?]]
        )
      ).thenReturn(Future.successful(certificate))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[MgdCertificateService].toInstance(mockService)
        )
        .build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ViewRegistrationCertificateController.onPageLoad().url)
            .withSession("mgdRefNum" -> "MGD123")

        val result = route(application, request).value
        val view = application.injector.instanceOf[ViewRegistrationCertificateView]

        status(result) mustBe OK
        contentAsString(result) mustBe view(certificate)(request, messages(application)).toString
      }
    }

    "must return INTERNAL_SERVER_ERROR when service fails" in {

      val mockService = mock[MgdCertificateService]

      when(
        mockService.retrieveCertificate(any[String])(
          any[HeaderCarrier],
          any[play.api.mvc.Request[?]]
        )
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[MgdCertificateService].toInstance(mockService)
        )
        .build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ViewRegistrationCertificateController.onPageLoad().url)
            .withSession("mgdRefNum" -> "MGD123")

        val result = route(application, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
