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
import config.FrontendAppConfig
import models.{Regime, SelectedReturn, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.SelectReturnPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ContactHmrcView

import java.time.LocalDate

class ContactHmrcControllerSpec extends SpecBase with MockitoSugar {

  val backUrl: Option[String] = Some("/manage-gambling-tax/returns/")

  val selectedReturn: SelectedReturn =
    SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  def userAnswersWithSelectedReturn: UserAnswers =
    UserAnswers(userAnswersId)
      .set(SelectReturnPage, selectedReturn)
      .success
      .value

  lazy val contactHmrcRoute: String =
    routes.ContactHmrcController.onPageLoad().url

  "ContactHmrc Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswersWithSelectedReturn),
          regime = Regime.MGD
        ).build()

      running(application) {
        val request = FakeRequest(GET, contactHmrcRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ContactHmrcView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            backLink = backUrl,
            selectedReturn = selectedReturn,
            contactHmrcUrl = appConfig.contactHmrcUrl
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to AccessDeniedController on a GET when regime is not MGD" in {

      val regimesExcludingMGD = Seq("gbd", "pbd", "rgd")

      regimesExcludingMGD.foreach { code =>
        val application =
          applicationBuilder(
            regime = Regime.fromString(code).get
          ).build()

        running(application) {
          val request = FakeRequest(GET, contactHmrcRoute)
          val result = route(application, request).value
          
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
        }
      }
    }

    "must redirect to SelectReturnController on a GET when no SelectedReturn is found in the session" in {

      val application =
        applicationBuilder(
          userAnswers = None,
          regime = Regime.MGD
        ).build()

      running(application) {
        val request = FakeRequest(GET, contactHmrcRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SelectReturnController.onPageLoad().url
      }
    }

    "must redirect to SelectReturnController when UserAnswers exist but SelectedReturn is missing" in {

      val application =
        applicationBuilder(
          userAnswers = Some(UserAnswers(userAnswersId)),
          regime = Regime.MGD
        ).build()

      running(application) {
        val request = FakeRequest(GET, contactHmrcRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SelectReturnController.onPageLoad().url
      }
    }
  }
}