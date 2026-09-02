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
import models.{CheckMode, SelectedReturn, UserAnswers}
import pages.{MachinesAvailablePage, SelectReturnPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.CheckYourAnswersHelpers
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val selectedReturn: SelectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  def userAnswersWithSelectedReturn: UserAnswers = emptyUserAnswers.set(SelectReturnPage, selectedReturn).success.value

  def userAnswersWithMachinesAvailable: UserAnswers =
    userAnswersWithSelectedReturn.set(MachinesAvailablePage, 10).success.value

  "Check Your Answers Controller" - {

    def setValueRow(keyMsg: String, url: String)(implicit msgs: play.api.i18n.Messages) = CheckYourAnswersHelpers.yesNoOrActionLinkRow(
      keyMsg        = keyMsg,
      answer        = None,
      showValueLink = true,
      linkTextMsg   = "checkYourAnswers.setValue",
      url           = url,
      hiddenMsg     = keyMsg
    )

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithMachinesAvailable)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val machines = SummaryListViewModel(
          Seq(
            CheckYourAnswersHelpers.textRow(
              keyMsg    = "submittedReturn.noOfMachines",
              answer    = "10",
              changeUrl = Some(routes.MachinesAvailableController.onPageLoad(CheckMode).url),
              hiddenMsg = Some("submittedReturn.noOfMachines")
            )
          )
        )

        val lowerRate = SummaryListViewModel(
          Seq(setValueRow("netTakingsLowerRate.question", routes.NetTakingsLowerRateController.onPageLoad(CheckMode).url))
        )
        val standardRate = SummaryListViewModel(
          Seq(setValueRow("netTakingsStandardRate.question", routes.NetTakingsStandardRateController.onPageLoad(CheckMode).url))
        )
        val higherRate = SummaryListViewModel(
          Seq(setValueRow("netTakingsHigherRate.question", routes.NetTakingsHigherRateController.onPageLoad(CheckMode).url))
        )
        val underDeclaredDuty = SummaryListViewModel(
          Seq(setValueRow("underDeclaredDuty.heading", routes.UnderDeclaredDutyController.onPageLoad(CheckMode).url))
        )
        val dutyBroughtForward = SummaryListViewModel(
          Seq(setValueRow("negativeDuty.question", routes.NegativeDutyController.onPageLoad(CheckMode).url))
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          selectedReturn,
          machines,
          lowerRate,
          standardRate,
          higherRate,
          underDeclaredDuty,
          dutyBroughtForward
        )(request, msgs).toString
      }
    }

    "must return OK with a 'Enter number' link for machines available when it is unanswered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithSelectedReturn)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        implicit val msgs: play.api.i18n.Messages = messages(application)
        val machinesUrl = routes.MachinesAvailableController.onPageLoad(CheckMode).url

        status(result) mustEqual OK

        val doc = org.jsoup.Jsoup.parse(contentAsString(result))
        val link = doc.select(s"""a[href="$machinesUrl"]""").first()
        link.text() mustEqual msgs("checkYourAnswers.enterNumber")
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

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
