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
import models.SubmittedReturnsTestData.{validResponseSubmittedReturns, zeroResponseSubmittedReturns}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.GamblingService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SubmittedReturnsView

import scala.concurrent.Future

class SubmittedReturnsControllerSpec extends SpecBase with MockitoSugar {

  private val regNumber = "XWM00003102200"

  lazy val SubmittedReturnsRoute: String = routes.SubmittedReturnsController.onPageLoad(Some(2), Some("DESC")).url

  "SubmittedReturnsController" - {

    "must return OK and the correct view for a GET" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getSubmittedReturns(any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(validResponseSubmittedReturns))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, SubmittedReturnsRoute).withSession("mgdRefNum" -> regNumber)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regNumber, validResponseSubmittedReturns, 2, "DESC")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when no existing data is found" in {

      val mockService = mock[GamblingService]
      when(
        mockService.getSubmittedReturns(any[String], any[Int], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(zeroResponseSubmittedReturns))

      val application = applicationBuilder().overrides(bind[GamblingService].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, SubmittedReturnsRoute).withSession("mgdRefNum" -> regNumber)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regNumber, zeroResponseSubmittedReturns, 2, "DESC")(request, messages(application)).toString
      }
    }
  }
}
