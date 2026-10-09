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
import models.{CheckMode, NormalMode, SelectedReturn, UserAnswers}
import pages.{MachinesAvailablePage, SelectReturnPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.CheckYourAnswersHelpers
import viewmodels.govuk.SummaryListFluency
import pages.*
import viewmodels.checkAnswers.*
import views.html.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val selectedReturn: SelectedReturn = SelectedReturn(1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  def userAnswersWithSelectedReturn: UserAnswers = emptyUserAnswers.set(SelectReturnPage, selectedReturn).success.value

  def userAnswersWithMachinesAvailable: UserAnswers = userAnswersWithSelectedReturn.set(MachinesAvailablePage, 10).success.value

  "Check Your Answers Controller onPageLoad" - {

    def setValueRow(keyMsg: String, url: String)(implicit msgs: play.api.i18n.Messages) = CheckYourAnswersHelpers.yesNoOrActionLinkRow(
      keyMsg        = keyMsg,
      answer        = None,
      showValueLink = true,
      linkTextMsg   = "checkYourAnswers.setValue",
      url           = url,
      hiddenMsg     = keyMsg
    )

    "must return OK and the correct view for a GET" in {

      val completeAnswers =
        userAnswersWithMachinesAvailable
          .set(NetTakingsLowerRatePage, false)
          .success
          .value
          .set(NetTakingsStandardRatePage, false)
          .success
          .value
          .set(NetTakingsHigherRatePage, false)
          .success
          .value
          .set(UnderDeclaredDutyPage, false)
          .success
          .value
          .set(NegativeDutyPage, false)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(completeAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val backLink = Some(routes.NegativeDutyController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[CheckYourAnswersView]
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val machines =
          SummaryListViewModel(rows = MachinesAvailableSummary.rows(completeAnswers))
        val lowerRate =
          SummaryListViewModel(rows = LowerRateSummary.rows(completeAnswers))
        val standardRate =
          SummaryListViewModel(rows = StandardRateSummary.rows(completeAnswers))
        val higherRate =
          SummaryListViewModel(rows = HigherRateSummary.rows(completeAnswers))
        val underDeclaredDuty =
          SummaryListViewModel(rows = UnderDeclaredDutySummary.rows(completeAnswers))
        val dutyBroughtForward =
          SummaryListViewModel(rows = DutyBroughtForwardSummary.rows(completeAnswers))

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            selectedReturn,
            machines,
            lowerRate,
            standardRate,
            higherRate,
            underDeclaredDuty,
            dutyBroughtForward,
            backLink
          )(request, msgs).toString
      }
    }

    "must redirect to Select Return Controller for a GET if no selected return is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }

    "must redirect to SelectReturnController for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }
  }

  "must redirect to Machines Available when machines is unanswered" in {

    val application = applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn)).build()
    running(application) {
      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        routes.MachinesAvailableController.onPageLoad(NormalMode).url
    }
  }

  "must redirect to Lower Rate screener when machines is answered and Lower Rate is unanswered" in {

    val application = applicationBuilder(userAnswers = Some(userAnswersWithMachinesAvailable)).build()
    running(application) {
      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        routes.NetTakingsLowerRateController.onPageLoad(NormalMode).url
    }
  }

  "CheckYourAnswersController onSubmit" - {

    "onSubmit must return SEE_OTHER and redirect to the correct page for a POST when MachinesAvailablePage is 0" in {

      def userAnswersWithData: UserAnswers = UserAnswers(userAnswersId).set(MachinesAvailablePage, 0L).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ConfirmNumberOfMachinesInterrupterController.onPageLoad().url
      }
    }

    "onSubmit must return SEE_OTHER and redirect to the correct page for a POST when MachinesAvailablePage is 1" in {

      def userAnswersWithData: UserAnswers = UserAnswers(userAnswersId).set(MachinesAvailablePage, 1L).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DeclareAndSubmitController.onPageLoad().url
      }
    }

    "onSubmit must return SEE_OTHER and redirect to the correct page for a POST when MachinesAvailablePage is None" in {

      def userAnswersWithData: UserAnswers = UserAnswers(userAnswersId)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData)).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SelectReturnController.onPageLoad().url
      }
    }
  }
}
