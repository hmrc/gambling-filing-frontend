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
import models.{Regime, SelectedReturn, SubmissionResult, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.{SelectReturnPage, SubmissionResultPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ConfirmationView

import java.time.LocalDate

class ConfirmationControllerSpec extends SpecBase with MockitoSugar {

  val selectedReturn: SelectedReturn =
    SelectedReturn(1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  val submissionResult: SubmissionResult =
    SubmissionResult("4JTF BAXM GJXS TKM", "2025-03-31T10:15:30Z")

  def userAnswersWithSelectedReturn: UserAnswers =
    UserAnswers(userAnswersId)
      .set(SelectReturnPage, selectedReturn)
      .success
      .value
      .set(SubmissionResultPage, submissionResult)
      .success
      .value

  lazy val confirmationRoute: String =
    routes.ConfirmationController.onPageLoad().url

  "Confirmation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSelectedReturn),
          regime      = Regime.MGD
        ).build()

      running(application) {
        val request = FakeRequest(GET, confirmationRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ConfirmationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            acknowledgementReference = submissionResult.acknowledgementReference,
            submissionDateTime       = submissionResult.submissionTimestamp,
            periodStartDate          = selectedReturn.periodStart,
            periodEndDate            = selectedReturn.periodEnd
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to DeclareAndSubmitController on a GET when no SelectedReturn is found in the session" in {

      val application =
        applicationBuilder(
          userAnswers = None,
          regime      = Regime.MGD
        ).build()

      running(application) {
        val request = FakeRequest(GET, confirmationRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.DeclareAndSubmitController.onPageLoad().url
      }
    }

    "must redirect to DeclareAndSubmitController when UserAnswers exist but SelectedReturn is missing" in {

      val application =
        applicationBuilder(
          userAnswers = Some(UserAnswers(userAnswersId)),
          regime      = Regime.MGD
        ).build()

      running(application) {
        val request = FakeRequest(GET, confirmationRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.DeclareAndSubmitController.onPageLoad().url
      }
    }

    "must redirect to DeclareAndSubmitController when SelectedReturn exists but SubmissionResult is missing" in {

      val application =
        applicationBuilder(
          userAnswers = Some(UserAnswers(userAnswersId).set(SelectReturnPage, selectedReturn).success.value),
          regime      = Regime.MGD
        ).build()

      running(application) {
        val request = FakeRequest(GET, confirmationRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.DeclareAndSubmitController.onPageLoad().url
      }
    }
  }
}
