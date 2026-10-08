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

package controllers.actions

import base.SpecBase
import models.requests.OptionalDataRequest
import models.{Regime, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, status}
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

class JourneyCompleteActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository) extends JourneyCompleteActionImpl(sessionRepository) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = refine(request)
  }

  "Journey Complete Action" - {

    "when there is no data in the cache" - {

      "must redirect to SelectReturnController when userAnswers is 'None' in the request" in {

        val sessionRepository = mock[SessionRepository]
        val action = new Harness(sessionRepository)

        val resultEither = action.callRefine(OptionalDataRequest(FakeRequest(), "regNum123", Regime.MGD, None)).futureValue

        resultEither match {
          case Left(result) =>
            status(Future.successful(result)) mustBe SEE_OTHER
            redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.SelectReturnController.onPageLoad().url)
            true
          case Right(r) => false
        } mustBe true
      }
    }

    "when there is data in the cache" - {

      "must redirect to SelectReturnController when userAnswers.journeyComplete is true in the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.set(any())) thenReturn Future(true)
        val userAnswers = Some(UserAnswers("id").copy(journeyComplete = true))
        val action = new Harness(sessionRepository)

        val resultEither = action.callRefine(OptionalDataRequest(FakeRequest(), "regNum123", Regime.MGD, userAnswers)).futureValue

        resultEither match {
          case Left(result) =>
            status(Future.successful(result)) mustEqual SEE_OTHER
            redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.SelectReturnController.onPageLoad().url)

            val userAnswersCaptor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(sessionRepository).set(userAnswersCaptor.capture())
            userAnswersCaptor.getValue.journeyComplete.booleanValue() mustEqual false
            userAnswersCaptor.getValue.data mustEqual Json.obj()
            true
          case Right(r) => false
        } mustBe true
      }

      "must forward the original request when userAnswers.journeyComplete is false" in {

        val sessionRepository = mock[SessionRepository]
        val userAnswers = Some(UserAnswers("id").copy(journeyComplete = false))
        val optionalDataRequest = OptionalDataRequest(FakeRequest(), "regNum123", Regime.MGD, userAnswers)
        val action = new Harness(sessionRepository)

        val resultEither = action.callRefine(optionalDataRequest).futureValue

        resultEither match {
          case Right(result) =>
            result mustEqual optionalDataRequest
            val userAnswersCaptor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(sessionRepository, times(0)).set(userAnswersCaptor.capture())
            true
          case Left(r) => false
        } mustBe true
      }
    }
  }
}
