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
import forms.NetTakingsStandardRateFormProvider
import models.{NormalMode, Regime, SelectedReturn, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalactic.Prettifier.default
import org.scalatestplus.mockito.MockitoSugar
import pages.{NetTakingsStandardRatePage, SelectReturnPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.NetTakingsStandardRateView

import java.time.LocalDate
import scala.concurrent.Future

class NetTakingsStandardRateControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new NetTakingsStandardRateFormProvider()
  val form = formProvider()

  def onwardRoute: Call = Call("GET", "/foo")

  val validAnswer: Boolean = true
  val backUrl = Some("/manage-gambling-tax/returns/")

  val selectedReturn: SelectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  lazy val netTakingsStandardRateRoute: String =
    routes.NetTakingsStandardRateController.onPageLoad(NormalMode).url

  def userAnswersWithSelectedReturn: UserAnswers = UserAnswers(userAnswersId).set(SelectReturnPage, selectedReturn).success.value

  "NetTakingsStandardRate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn), regime = Regime.MGD).build()

      running(application) {
        val request = FakeRequest(GET, netTakingsStandardRateRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[NetTakingsStandardRateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, backUrl, selectedReturn)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        userAnswersWithSelectedReturn
          .set(NetTakingsStandardRatePage, validAnswer)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), regime = Regime.MGD).build()

      running(application) {
        val request = FakeRequest(GET, netTakingsStandardRateRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[NetTakingsStandardRateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, backUrl, selectedReturn)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn), regime = Regime.MGD)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, netTakingsStandardRateRoute)
            .withFormUrlEncodedBody("value" -> validAnswer.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn), regime = Regime.MGD).build()

      running(application) {
        val request =
          FakeRequest(POST, netTakingsStandardRateRoute)
            .withFormUrlEncodedBody("value" -> "invalid value")

        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view = application.injector.instanceOf[NetTakingsStandardRateView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, backUrl, selectedReturn)(request, messages(application)).toString
      }
    }

    "must redirect to AccessDeniedController on GET when regime is not MGD" in {

      val regimesExcludingMGD = Seq("gbd", "pbd", "rgd")

      regimesExcludingMGD.foreach { code =>
        val application =
          applicationBuilder(regime = Regime.fromString(code).get).build()

        running(application) {
          val request = FakeRequest(GET, netTakingsStandardRateRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
        }
      }
    }

    "must redirect to AccessDeniedController on POST when regime is not MGD" in {

      val regimesExcludingMGD = Seq("gbd", "pbd", "rgd")

      regimesExcludingMGD.foreach { code =>
        val application =
          applicationBuilder(regime = Regime.fromString(code).get).build()

        running(application) {
          val request =
            FakeRequest(POST, netTakingsStandardRateRoute)
              .withFormUrlEncodedBody("value" -> validAnswer.toString)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
        }
      }
    }

    "must return OK and the correct view for a GET when no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn), regime = Regime.MGD).build()

      running(application) {
        val request = FakeRequest(GET, netTakingsStandardRateRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[NetTakingsStandardRateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, backUrl, selectedReturn)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted and no existing data is found" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn), regime = Regime.MGD)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, netTakingsStandardRateRoute)
            .withFormUrlEncodedBody("value" -> validAnswer.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
