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
import forms.CalculationLowerCheckFormProvider
import models.{NormalMode, Regime, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{CalculationLowerCheckPage, NetTakingsLowerPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CalculationLowerCheckView

import scala.concurrent.Future

class CalculationLowerCheckControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new CalculationLowerCheckFormProvider()
  val form = formProvider()

  val validAnswer: Boolean = true

  val netTakings = BigDecimal(1000)

  val duty = netTakings * BigDecimal(0.05)

  val percentage = 5

  val userAnswersWithNetTakings =
    emptyUserAnswers
      .set(NetTakingsLowerPage, netTakings)
      .success
      .value

  val userAnswers =
    userAnswersWithNetTakings
      .set(CalculationLowerCheckPage, validAnswer)
      .success
      .value

  def onwardRoute = Call("GET", "/foo")

  lazy val calculationLowerCheckRoute = routes.CalculationLowerCheckController.onPageLoad(NormalMode).url

  "CalculationLowerCheck Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNetTakings)).build()

      running(application) {
        val request = FakeRequest(GET, calculationLowerCheckRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CalculationLowerCheckView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, netTakings, duty, percentage, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, calculationLowerCheckRoute)

        val view = application.injector.instanceOf[CalculationLowerCheckView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), netTakings, duty, percentage, NormalMode)(request,
                                                                                                                 messages(application)
                                                                                                                ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithNetTakings))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, calculationLowerCheckRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNetTakings)).build()

      running(application) {
        val request =
          FakeRequest(POST, calculationLowerCheckRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CalculationLowerCheckView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, netTakings, duty, percentage, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to AccessDeniedController on GET when regime is not MGD" in {

      val regimesExcludingMGD = Seq("gbd", "pbd", "rgd")
      regimesExcludingMGD.foreach { code =>
        val application = applicationBuilder(regime = Regime.fromString(code).get).build()

        running(application) {
          val request = FakeRequest(GET, calculationLowerCheckRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
        }
      }
    }

    "must redirect to AccessDeniedController on POST when regime is not MGD" in {

      val regimesExcludingMGD = Seq("gbd", "pbd", "rgd")
      regimesExcludingMGD.foreach { code =>
        val application = applicationBuilder(regime = Regime.fromString(code).get).build()

        running(application) {
          val request =
            FakeRequest(POST, calculationLowerCheckRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
        }
      }
    }
  }
}
