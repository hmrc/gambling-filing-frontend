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
import models.*
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

  private val certificate: MgdCertificate =
    MgdCertificate(
      mgdRegNumber         = "MGD123",
      registrationDate     = Some(LocalDate.parse("2026-01-01")),
      individualName       = Some("John Doe"),
      businessName         = Some("Test Business Ltd"),
      tradingName          = None,
      repMemName           = None,
      busAddrLine1         = Some("Line 1"),
      busAddrLine2         = Some("Line 2"),
      busAddrLine3         = None,
      busAddrLine4         = None,
      busPostcode          = Some("AB1 2CD"),
      busCountry           = None,
      busAdi               = None,
      repMemLine1          = None,
      repMemLine2          = None,
      repMemLine3          = None,
      repMemLine4          = None,
      repMemPostcode       = None,
      repMemAdi            = None,
      typeOfBusiness       = Some("Corporate Body"), // important: matches controller
      businessTradeClass   = Some(1),
      noOfPartners         = None,
      groupReg             = "N",
      noOfGroupMems        = None,
      dateCertIssued       = Some(LocalDate.parse("2026-01-02")),
      partMembers          = Seq.empty,
      groupMembers         = Seq.empty,
      returnPeriodEndDates = Seq.empty
    )

  private val managementUrl = "http://localhost:10400/gambling/"

  "ViewRegistrationCertificateController" - {

    "must return OK and render certificate view when service succeeds" in {

      val mockService = mock[MgdCertificateService]

      when(
        mockService.retrieveCertificate(any[String])(
          any[HeaderCarrier],
          any[play.api.mvc.Request[?]]
        )
      ).thenReturn(Future.successful(certificate))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[MgdCertificateService].toInstance(mockService))
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ViewRegistrationCertificateController.onPageLoad().url)
            .withSession("mgdRefNum" -> "MGD123")

        val result = route(application, request).value
        val view = application.injector.instanceOf[ViewRegistrationCertificateView]

        val (displayName, displayLabelKey) =
          certificate.typeOfBusiness.map(_.trim.toLowerCase) match {

            case Some("sole proprietor") =>
              certificate.individualName.getOrElse("") ->
                "viewRegistrationCertificate.label.soleProprietor"

            case Some("unincorporated body") =>
              certificate.businessName.getOrElse("") ->
                "viewRegistrationCertificate.label.unincorporatedBody"

            case Some("corporate body") =>
              certificate.businessName.getOrElse("") ->
                "viewRegistrationCertificate.label.corporateBody"

            case Some("partnership") =>
              certificate.partMembers.headOption.map(_.namesOfPartMems).getOrElse("") ->
                "viewRegistrationCertificate.label.partnership"

            case Some("limited liability partnership") =>
              certificate.businessName.getOrElse("") ->
                "viewRegistrationCertificate.label.limitedLiabilityPartnership"

            case _ =>
              certificate.businessName.getOrElse("") ->
                "viewRegistrationCertificate.label.default"
          }

        val formattedAddress =
          Seq(
            certificate.busAddrLine1,
            certificate.busAddrLine2,
            certificate.busAddrLine3,
            certificate.busAddrLine4,
            certificate.busPostcode
          ).flatten.filter(_.nonEmpty).mkString("<br>")

        status(result) mustBe OK

        contentAsString(result) mustBe
          view(
            certificate,
            managementUrl,
            displayName,
            displayLabelKey,
            formattedAddress
          )(request, messages(application)).toString
      }
    }

    "must redirect to system error page when service fails" in {

      val mockService = mock[MgdCertificateService]

      when(
        mockService.retrieveCertificate(any[String])(
          any[HeaderCarrier],
          any[play.api.mvc.Request[?]]
        )
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[MgdCertificateService].toInstance(mockService))
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ViewRegistrationCertificateController.onPageLoad().url)
            .withSession("mgdRefNum" -> "MGD123")

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
