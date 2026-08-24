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
import models.{NormalMode, SelectedReturn, UserAnswers}
import pages.SelectReturnPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.IntroductionView

import java.time.LocalDate

class IntroductionControllerSpec extends SpecBase {

  val selectedReturn: SelectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  def userAnswersWithSelectedReturn: UserAnswers = UserAnswers(userAnswersId).set(SelectReturnPage, selectedReturn).success.value

  lazy val introductionRoute = routes.IntroductionController.onPageLoad().url

  "Introduction Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn)).build()

      running(application) {
        val request = FakeRequest(GET, introductionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IntroductionView]
        val backUrl = Some(routes.SelectReturnController.onPageLoad().url)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(selectedReturn, backUrl)(request, messages(application)).toString
      }
    }

    "must redirect to PageNotFoundController on a GET when no SelectedReturn is found in the session" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, introductionRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PageNotFoundController.onPageLoad().url
      }
    }

    "must redirect to MachinesAvailableController on a POST when a SelectedReturn is present" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn)).build()

      running(application) {
        val request = FakeRequest(POST, routes.IntroductionController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MachinesAvailableController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to SelectReturnController on a POST when no SelectedReturn is found in the session" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, routes.IntroductionController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }
  }
}
