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
import forms.MgdHigherRateFormProvider
import models.{NormalMode, SelectedReturn, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{MgdHigherRatePage, NetTakingsHigherPage, SelectReturnPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.MgdHigherRateView

import java.time.LocalDate
import scala.concurrent.Future

class MgdHigherRateControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new MgdHigherRateFormProvider()

  val netTakings: BigDecimal = BigDecimal("2000")
  val validAnswer: BigDecimal = BigDecimal("1000")
  val invalidAmount: BigDecimal = BigDecimal("2500")
  val form = formProvider(netTakings)

  def onwardRoute = Call("GET", "/foo")

  val selectedReturn: SelectedReturn = SelectedReturn(1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  def userAnswersWithSelectedReturn: UserAnswers = UserAnswers(userAnswersId)
    .set(SelectReturnPage, selectedReturn)
    .success
    .value
    .set(NetTakingsHigherPage, netTakings)
    .success
    .value

  private lazy val mgdHigherRateRoute = routes.MgdHigherRateController.onPageLoad(NormalMode).url

  "MgdHigherRate Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn)).build()

      running(application) {
        val request = FakeRequest(GET, mgdHigherRateRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MgdHigherRateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, selectedReturn)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = userAnswersWithSelectedReturn.set(MgdHigherRatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, mgdHigherRateRoute)

        val view = application.injector.instanceOf[MgdHigherRateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, selectedReturn)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, mgdHigherRateRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn)).build()

      running(application) {
        val request =
          FakeRequest(POST, mgdHigherRateRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[MgdHigherRateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, selectedReturn)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when no existing data is found" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn)).build()

      running(application) {
        val request = FakeRequest(GET, mgdHigherRateRoute)

        val view = application.injector.instanceOf[MgdHigherRateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, selectedReturn)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted and no existing data is found" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, mgdHigherRateRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to SelectReturnController on a GET when no SelectedReturn is found in the session" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, mgdHigherRateRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }

    "must redirect to SelectReturnController on a POST when no SelectedReturn is found in the session" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request =
          FakeRequest(POST, mgdHigherRateRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }

    "must return Bad Request when MGD Higher Rate exceeds net takings" in {
      val userAnswers = userAnswersWithSelectedReturn
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, mgdHigherRateRoute).withFormUrlEncodedBody(("value", invalidAmount.toString))
        val result = route(application, request).value
        val boundForm = formProvider(netTakings).bind(Map("value" -> invalidAmount.toString))
        val view = application.injector.instanceOf[MgdHigherRateView]

        status(result) mustEqual BAD_REQUEST

        boundForm.errors.exists(_.message == "mgdHigherRate.error.mustBeGreaterThanNetTakings") mustBe true
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, selectedReturn)(request, messages(application)).toString
      }
    }

    "must redirect when Higher Level net takings are missing" in {
      val userAnswers =
        UserAnswers(userAnswersId)
          .set(SelectReturnPage, selectedReturn)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, mgdHigherRateRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }
  }
}
