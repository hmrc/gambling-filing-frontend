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
import models.SelectReturnTestData.{validResponseOpenReturns, zeroResponseOpenReturns}
import models.{NormalMode, SelectedReturn, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{OpenReturnPeriodsPage, SelectReturnPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.GamblingService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SelectReturnView

import java.time.LocalDate
import scala.concurrent.Future

class SelectReturnControllerSpec extends SpecBase with MockitoSugar {

  private val regNumber = "XWM00003102200"

  lazy val OpenReturnsRoute: String = routes.SelectReturnController.onPageLoad().url

  val backUrl = Some(
    routes.IndexController.onPageLoad().url
  )

  def userAnswersWithCachedPeriods: UserAnswers =
    UserAnswers(userAnswersId).set(OpenReturnPeriodsPage, validResponseOpenReturns).success.value

  "OpenReturnsController" - {

    "must return OK and the correct view for a GET, caching the open periods in session" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val mockService = mock[GamblingService]
      when(
        mockService.getOpenReturnPeriods(any[String], any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(scala.concurrent.Future.successful(validResponseOpenReturns))

      val application =
        applicationBuilder()
          .overrides(
            bind[GamblingService].toInstance(mockService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, OpenReturnsRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regNumber, validResponseOpenReturns, backUrl)(request, messages(application)).toString

        val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(OpenReturnPeriodsPage).value mustEqual validResponseOpenReturns
      }
    }

    "must return OK and the correct view for a GET when no existing data is found" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val mockService = mock[GamblingService]
      when(
        mockService.getOpenReturnPeriods(any[String], any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(scala.concurrent.Future.successful(zeroResponseOpenReturns))

      val application =
        applicationBuilder()
          .overrides(
            bind[GamblingService].toInstance(mockService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, OpenReturnsRoute).withSession("regNum" -> regNumber)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regNumber, zeroResponseOpenReturns, backUrl)(request, messages(application)).toString
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

  "selectOpenPeriod" - {

    "must resolve the consecNo to a cached period, store it under fileReturn and redirect to IntroductionController.onPageLoad" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCachedPeriods))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.SelectReturnController.selectOpenPeriod(12345).url)
          .withSession("regNum" -> regNumber)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IntroductionController.onPageLoad().url

        val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(SelectReturnPage).value mustEqual SelectedReturn(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 9, 30))
      }
    }

    "must redirect back to onPageLoad without updating session when consecNo does not match any cached open period" in {

      val mockSessionRepository = mock[SessionRepository]

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCachedPeriods))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.SelectReturnController.selectOpenPeriod(99999).url)
          .withSession("regNum" -> regNumber)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url

        verify(mockSessionRepository, never()).set(any[UserAnswers])
      }
    }

    "must redirect back to onPageLoad without updating session or calling the backend when no periods are cached" in {

      val mockSessionRepository = mock[SessionRepository]

      val mockService = mock[GamblingService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[GamblingService].toInstance(mockService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.SelectReturnController.selectOpenPeriod(12345).url)
          .withSession("regNum" -> regNumber)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url

        verify(mockSessionRepository, never()).set(any[UserAnswers])
        verify(mockService, never())
          .getOpenReturnPeriods(any[String], any[String], any[Int], any[String])(any[HeaderCarrier])
      }
    }
  }
}
