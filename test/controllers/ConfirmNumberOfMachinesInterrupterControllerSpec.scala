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
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{ConfirmNumberOfMachinesInterrupterPage, MachinesAvailablePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ConfirmNumberOfMachinesInterrupterView

import scala.concurrent.Future

class ConfirmNumberOfMachinesInterrupterControllerSpec extends SpecBase with MockitoSugar {

  private lazy val confirmNumberOfMachinesInterrupterRoute = routes.ConfirmNumberOfMachinesInterrupterController.onPageLoad().url

  "ConfirmNumberOfMachinesInterrupter Controller" - {

    "onPageLoad must return OK and the correct view for a GET when MachinesAvailablePage is 0" in {

      val userAnswers = UserAnswers(userAnswersId).set(MachinesAvailablePage, 0L).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, confirmNumberOfMachinesInterrupterRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmNumberOfMachinesInterrupterView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "onSubmit must save the confirmation and redirect to Declare and Submit" in {

      val userAnswers = UserAnswers(userAnswersId).set(MachinesAvailablePage, 0L).success.value
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(POST, routes.ConfirmNumberOfMachinesInterrupterController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.DeclareAndSubmitController.onPageLoad().url

        val expectedAnswers = userAnswers.set(ConfirmNumberOfMachinesInterrupterPage, true).success.value
        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "onSubmit must redirect to Select Return when no user answers are present" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, routes.ConfirmNumberOfMachinesInterrupterController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }
  }
}
