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
import models.OpenReturnPeriodsTestData.{validResponseOpenReturns, zeroResponseOpenReturns}
import models.{FileReturn, NormalMode, Regime, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.FileReturnPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.GamblingService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.OpenReturnsView

import java.time.LocalDate
import scala.concurrent.Future

class OpenReturnsControllerSpec extends SpecBase with MockitoSugar {

  private val regNumber = "XWM00003102200"

  lazy val OpenReturnsRoute: String = routes.OpenReturnsController.onPageLoad().url

  "OpenReturnsController" - {

    "must return OK and the correct view for a GET" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getOpenReturnPeriods(any[String], any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(scala.concurrent.Future.successful(validResponseOpenReturns))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, OpenReturnsRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OpenReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regNumber, validResponseOpenReturns)(request, messages(application)).toString
      }
    }

    "must redirect to AccessDeniedController when regime is not MGD" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getOpenReturnPeriods(any[String], any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(scala.concurrent.Future.successful(validResponseOpenReturns))

      val regimesExcludingMGD = Seq("gbd", "pbd", "rgd")
      regimesExcludingMGD.foreach { code =>
        val application = applicationBuilder(regime = Regime.fromString(code).get).overrides(bind[GamblingService].toInstance(mockService)).build()

        running(application) {
          val request = FakeRequest(GET, OpenReturnsRoute).withSession("regNum" -> regNumber)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
        }
      }
    }

    "must return OK and the correct view for a GET when no existing data is found" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getOpenReturnPeriods(any[String], any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(scala.concurrent.Future.successful(zeroResponseOpenReturns))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, OpenReturnsRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OpenReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regNumber, zeroResponseOpenReturns)(request, messages(application)).toString
      }
    }

    "must redirect to the system error page when the service call fails" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getOpenReturnPeriods(any[String], any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(scala.concurrent.Future.failed(new RuntimeException("upstream error")))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, OpenReturnsRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }

  "onSubmit" - {

    "must parse the period, store it under fileReturn and redirect to MachinesAvailableController.onPageLoad" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(POST, routes.OpenReturnsController.onSubmit().url)
          .withSession("regNum" -> regNumber)
          .withFormUrlEncodedBody("period" -> "01/07/2025 - 30/09/2025")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MachinesAvailableController.onPageLoad(NormalMode).url

        val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(FileReturnPage).value mustEqual FileReturn(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 9, 30))
      }
    }

    "must redirect back to onPageLoad without updating session when the period cannot be parsed" in {

      val mockSessionRepository = mock[SessionRepository]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(POST, routes.OpenReturnsController.onSubmit().url)
          .withSession("regNum" -> regNumber)
          .withFormUrlEncodedBody("period" -> "not-a-valid-period")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.OpenReturnsController.onPageLoad().url

        verify(mockSessionRepository, org.mockito.Mockito.never()).set(any[UserAnswers])
      }
    }
  }
}
