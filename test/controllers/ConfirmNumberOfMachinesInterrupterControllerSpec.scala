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
import org.scalatestplus.mockito.MockitoSugar
import pages.MachinesAvailablePage
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ConfirmNumberOfMachinesInterrupterView

class ConfirmNumberOfMachinesInterrupterControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/manage-gambling-tax/returns/declare-submit")

  private lazy val confirmNumberOfMachinesInterrupterRoute = routes.ConfirmNumberOfMachinesInterrupterController.onPageLoad().url

  "ConfirmNumberOfMachinesInterrupter Controller" - {

    "onPageLoad must return OK and the correct view for a GET when MachinesAvailablePage is 0" in {

      def userAnswersWithData: UserAnswers = UserAnswers(userAnswersId).set(MachinesAvailablePage, 0L).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(GET, confirmNumberOfMachinesInterrupterRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmNumberOfMachinesInterrupterView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "onPageLoad must return SEE_OTHER and redirect to the correct page for a GET when MachinesAvailablePage is 1" in {

      def userAnswersWithData: UserAnswers = UserAnswers(userAnswersId).set(MachinesAvailablePage, 1L).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(GET, confirmNumberOfMachinesInterrupterRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "onPageLoad must return SEE_OTHER and redirect to the correct page for a GET when MachinesAvailablePage is None" in {

      def userAnswersWithData: UserAnswers = UserAnswers(userAnswersId)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(GET, confirmNumberOfMachinesInterrupterRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }
  }
}
