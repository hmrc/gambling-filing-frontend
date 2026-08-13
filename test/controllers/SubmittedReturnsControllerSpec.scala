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
import models.SubmittedReturnsTestData.{validResponseSubmittedReturns, zeroResponseSubmittedReturns}
import models.SubmittedReturnSingle
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.GamblingService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SubmittedReturnsView

import java.time.LocalDate
import scala.concurrent.Future

class SubmittedReturnsControllerSpec extends SpecBase with MockitoSugar {

  private val regNumber = "XWM00003102200"
  private val consecNo = 12345

  lazy val SubmittedReturnsRoute: String = routes.SubmittedReturnsController.onPageLoad().url
  lazy val ViewFiledReturnRoute: String = routes.SubmittedReturnsController.viewFiledReturn(consecNo).url

  private val filedReturn = SubmittedReturnSingle(
    consecNo                     = consecNo,
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

  "SubmittedReturnsController" - {

    "must return OK and the correct view for a GET" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getSubmittedReturns(any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(validResponseSubmittedReturns))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, SubmittedReturnsRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regNumber, validResponseSubmittedReturns)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when no existing data is found" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getSubmittedReturns(any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(zeroResponseSubmittedReturns))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, SubmittedReturnsRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regNumber, zeroResponseSubmittedReturns)(request, messages(application)).toString
      }
    }
  }

  "viewFiledReturn" - {

    "must return OK and the correct page details" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getSubmittedReturn(any[String], any[Int])(any[HeaderCarrier])
      ).thenReturn(Future.successful(filedReturn))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, ViewFiledReturnRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value
        val content = contentAsString(result)

        status(result) mustEqual OK

        content must include("View filed return")
        content must include("Submission details")
        content must include("Date submitted")
        content must include("1 Apr 2025")
        content must include("Acknowledgement reference")
        content must include(filedReturn.ackRef)
        content must include("Number of machines available for play")
        content must include(filedReturn.noOfMachines.toString)
        content must include("Net takings for lower rate of duty")
        content must include(s"£${filedReturn.netTakingsLowerRate}")
        content must include("MGD due at lower rate")
        content must include(s"£${filedReturn.totalDueLowerRate}")
        content must include("Net takings for standard rate of duty")
        content must include(s"£${filedReturn.netTakingsStdRate}")
        content must include("MGD due at standard rate")
        content must include(s"£${filedReturn.totalDueStdRate}")
        content must include("Net takings for higher rate of duty")
        content must include(s"£${filedReturn.netTakingsHigherRate}")
        content must include("MGD due at higher rate")
        content must include(s"£${filedReturn.totalDueHigherRate}")
        content must include("Duty payable before any adjustments")
        content must include(s"£${filedReturn.dutyPayable}")
        content must include("Under-declared tax from previous returns")
        content must include(s"£${filedReturn.underDeclaredDuty}")
        content must include("Amount brought forward")
        content must include(s"£${filedReturn.previousReturnAmount}")
        content must include("Negative amount of duty to carry forward to next return")
        content must include(s"£${filedReturn.negativeAmountCarriedForward}")
        content must include("Net MGD payable on this return")
        content must include(s"£${filedReturn.totalNetDutyPayable}")
      }
    }

    "must redirect to the system error page when the service call fails" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getSubmittedReturn(any[String], any[Int])(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("upstream error")))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, ViewFiledReturnRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
