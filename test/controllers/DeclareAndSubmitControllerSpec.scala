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
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.DeclareAndSubmitView

import java.time.LocalDate

class DeclareAndSubmitControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val selectedReturn: SelectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  lazy val backUrl = Some(routes.CheckYourAnswersController.onPageLoad().url)

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
        contentAsString(result) mustEqual view(NormalMode, backUrl, selectedReturn, validResponseDeclaredSubmission)(request,
                                                                                                                     messages(application)
                                                                                                                    ).toString
      }
    }

    "must redirect to the next page when page is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

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

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to SelectReturnController on a GET when no SelectedReturn is found in the session" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, declareAndSubmitRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }

    "must redirect to SelectReturnController on a POST when no SelectedReturn is found in the session" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, declareAndSubmitRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }
  }
}
