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
import models.DeclaredSubmissionTestData.validResponseDeclaredSubmission
import models.{NormalMode, SelectedReturn, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{DeclareAndSubmitPage, MgdHigherRatePage, MgdLowerRatePage, MgdStandardRatePage, NegativeDutyBroughtForwardInputPage, SelectReturnPage, TotalUnderDeclaredDutyPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.DeclareAndSubmitView

import java.time.LocalDate
import scala.concurrent.Future

class DeclareAndSubmitControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: Long = 10

  val selectedReturn: SelectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  val backUrl = Some(
    routes.SelectReturnController.onPageLoad().url
  )

  def userAnswersWithData: UserAnswers = UserAnswers(userAnswersId)
    .set(SelectReturnPage, selectedReturn)
    .success
    .value
    .set(MgdLowerRatePage, BigDecimal(-5.56))
    .success
    .value
    .set(MgdStandardRatePage, BigDecimal(-44.44))
    .success
    .value
    .set(MgdHigherRatePage, BigDecimal(-83.33))
    .success
    .value
    .set(TotalUnderDeclaredDutyPage, BigDecimal(7.77))
    .success
    .value
    .set(NegativeDutyBroughtForwardInputPage, BigDecimal(1.99))
    .success
    .value

  lazy val declareAndSubmitRoute: String = routes.DeclareAndSubmitController.onPageLoad().url

  "DeclareAndSubmit Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(GET, declareAndSubmitRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclareAndSubmitView]

        status(result) mustEqual OK
//        System.out.println("JBS11:"+contentAsString(result))
        contentAsString(result) mustEqual view(NormalMode, backUrl, selectedReturn, validResponseDeclaredSubmission)(request,
                                                                                                                     messages(application)
                                                                                                                    ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersWithData.set(DeclareAndSubmitPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, declareAndSubmitRoute)

        val view = application.injector.instanceOf[DeclareAndSubmitView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode, backUrl, selectedReturn, validResponseDeclaredSubmission)(request,
                                                                                                                     messages(application)
                                                                                                                    ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithData))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, declareAndSubmitRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when a negative number is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request =
          FakeRequest(POST, declareAndSubmitRoute)
            .withFormUrlEncodedBody(("value", "-1"))

        val view = application.injector.instanceOf[DeclareAndSubmitView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(NormalMode, backUrl, selectedReturn, validResponseDeclaredSubmission)(request,
                                                                                                                     messages(application)
                                                                                                                    ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request =
          FakeRequest(POST, declareAndSubmitRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val view = application.injector.instanceOf[DeclareAndSubmitView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(NormalMode, backUrl, selectedReturn, validResponseDeclaredSubmission)(request,
                                                                                                                     messages(application)
                                                                                                                    ).toString
      }
    }

    "must redirect to PageNotFoundController on a GET when no SelectedReturn is found in the session" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, declareAndSubmitRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PageNotFoundController.onPageLoad().url
      }
    }

    "must redirect to PageNotFoundController on a POST when no SelectedReturn is found in the session" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, declareAndSubmitRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PageNotFoundController.onPageLoad().url
      }
    }
  }
}
