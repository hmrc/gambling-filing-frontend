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
import forms.TotalUnderDeclaredDutyFormProvider
import models.{NormalMode, SelectedReturn, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{NetTakingsHigherPage, NetTakingsLowerPage, NetTakingsStandardPage, SelectReturnPage, TotalUnderDeclaredDutyPage, UnderDeclaredDutyLimitsPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.TotalUnderDeclaredDutyView

import java.time.LocalDate
import scala.concurrent.Future

class TotalUnderDeclaredDutyControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new TotalUnderDeclaredDutyFormProvider()

  def onwardRoute: Call = Call("GET", "/foo")

  private val underDeclaredDutyConfig = Map(
    "mgd.under-declared-duty-minimum-limit" -> 10000,
    "mgd.under-declared-duty-maximum-limit" -> 50000,
    "mgd.under-declared-duty-percentage"    -> 0.01
  )

  private def configuredApplicationBuilder(
    userAnswers: Option[UserAnswers] = None
  ) =
    applicationBuilder(userAnswers = userAnswers)
      .configure(underDeclaredDutyConfig)

  val lowerNetTakings: BigDecimal = BigDecimal("900000")
  val standardNetTakings: BigDecimal = BigDecimal("900000")
  val higherNetTakings: BigDecimal = BigDecimal("900000")
  val maximumAllowed: BigDecimal = BigDecimal("27000")
  val validAnswer: BigDecimal = BigDecimal("8000")
  val invalidAnswer: BigDecimal = BigDecimal("27001")

  val selectedReturn: SelectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  val backUrl = Some(
    routes.UnderDeclaredDutyLimitsController
      .onPageLoad(NormalMode)
      .url
  )

  def userAnswersWithNetTakings: UserAnswers =
    UserAnswers(userAnswersId)
      .set(SelectReturnPage, selectedReturn)
      .success
      .value
      .set(NetTakingsLowerPage, lowerNetTakings)
      .success
      .value
      .set(NetTakingsStandardPage, standardNetTakings)
      .success
      .value
      .set(NetTakingsHigherPage, higherNetTakings)
      .success
      .value
      .set(UnderDeclaredDutyLimitsPage, true)
      .success
      .value

  lazy val totalUnderDeclaredDutyRoute: String =
    routes.TotalUnderDeclaredDutyController.onPageLoad(NormalMode).url

  "TotalUnderDeclaredDuty Controller" - {
    "must return OK and the correct view for a GET" in {
      val application =
        configuredApplicationBuilder(userAnswers = Some(userAnswersWithNetTakings)).build()

      running(application) {
        val request = FakeRequest(GET, totalUnderDeclaredDutyRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[TotalUnderDeclaredDutyView]

        val form = formProvider(maximumAllowed)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode, backUrl, selectedReturn)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        userAnswersWithNetTakings.set(TotalUnderDeclaredDutyPage, validAnswer).success.value
      val application = configuredApplicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, totalUnderDeclaredDutyRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[TotalUnderDeclaredDutyView]
        val form = formProvider(maximumAllowed)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(validAnswer), NormalMode, backUrl, selectedReturn)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data below the calculated maximum is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = configuredApplicationBuilder(
        userAnswers = Some(userAnswersWithNetTakings)
      )
        .overrides(
          bind[Navigator]
            .toInstance(
              new FakeNavigator(onwardRoute)
            ),
          bind[SessionRepository]
            .toInstance(
              mockSessionRepository
            )
        )
        .build()

      running(application) {

        val request = FakeRequest(POST, totalUnderDeclaredDutyRoute).withFormUrlEncodedBody("value" -> validAnswer.toString)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when the submitted value is equal to the calculated maximum" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = configuredApplicationBuilder(userAnswers = Some(userAnswersWithNetTakings))
        .overrides(
          bind[Navigator]
            .toInstance(
              new FakeNavigator(onwardRoute)
            ),
          bind[SessionRepository]
            .toInstance(
              mockSessionRepository
            )
        )
        .build()

      running(application) {

        val request =
          FakeRequest(POST, totalUnderDeclaredDutyRoute).withFormUrlEncodedBody("value" -> maximumAllowed.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          onwardRoute.url
      }
    }

    "must return a Bad Request when the submitted value is greater than the calculated maximum" in {

      val application = configuredApplicationBuilder(userAnswers = Some(userAnswersWithNetTakings)).build()

      running(application) {

        val request = FakeRequest(POST, totalUnderDeclaredDutyRoute).withFormUrlEncodedBody("value" -> invalidAnswer.toString)

        val form = formProvider(maximumAllowed)

        val boundForm = form.bind(Map("value" -> invalidAnswer.toString))

        val view =
          application.injector
            .instanceOf[TotalUnderDeclaredDutyView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            backUrl,
            selectedReturn
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = configuredApplicationBuilder(userAnswers = Some(userAnswersWithNetTakings)).build()

      running(application) {

        val request = FakeRequest(POST, totalUnderDeclaredDutyRoute).withFormUrlEncodedBody("value" -> "invalid value")

        val form = formProvider(maximumAllowed)

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TotalUnderDeclaredDutyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            backUrl,
            selectedReturn
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must use £10,000 as the minimum maximum when 1% of total net takings is less than £10,000" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(SelectReturnPage, selectedReturn)
          .success
          .value
          .set(
            NetTakingsLowerPage,
            BigDecimal("100000")
          )
          .success
          .value
          .set(
            NetTakingsStandardPage,
            BigDecimal("100000")
          )
          .success
          .value
          .set(
            NetTakingsHigherPage,
            BigDecimal("100000")
          )
          .success
          .value

      val application =
        configuredApplicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(POST, totalUnderDeclaredDutyRoute).withFormUrlEncodedBody("value" -> "10001")

        val form = formProvider(BigDecimal("10000"))

        val boundForm = form.bind(Map("value" -> "10001"))

        val view = application.injector.instanceOf[TotalUnderDeclaredDutyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            backUrl,
            selectedReturn
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must cap the calculated maximum at £50,000 when 1% of total net takings is greater than £50,000" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(SelectReturnPage, selectedReturn)
          .success
          .value
          .set(
            NetTakingsLowerPage,
            BigDecimal("3000000")
          )
          .success
          .value
          .set(
            NetTakingsStandardPage,
            BigDecimal("3000000")
          )
          .success
          .value
          .set(
            NetTakingsHigherPage,
            BigDecimal("3000000")
          )
          .success
          .value

      val application =
        configuredApplicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            totalUnderDeclaredDutyRoute
          ).withFormUrlEncodedBody(
            "value" -> "50001"
          )

        val form =
          formProvider(BigDecimal("50000"))

        val boundForm =
          form.bind(
            Map(
              "value" -> "50001"
            )
          )

        val view =
          application.injector
            .instanceOf[TotalUnderDeclaredDutyView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            backUrl,
            selectedReturn
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to SelectReturnController on a GET when no SelectedReturn is found in the session" in {

      val application = configuredApplicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            totalUnderDeclaredDutyRoute
          )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SelectReturnController
            .onPageLoad()
            .url
      }
    }

    "must redirect to SelectReturnController on a POST when no SelectedReturn is found in the session" in {

      val application = configuredApplicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            totalUnderDeclaredDutyRoute
          ).withFormUrlEncodedBody(
            "value" -> validAnswer.toString
          )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SelectReturnController
            .onPageLoad()
            .url
      }
    }
  }
}
