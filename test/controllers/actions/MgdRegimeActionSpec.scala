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
import models.Regime
import models.requests.OptionalDataRequest
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MgdRegimeActionSpec extends SpecBase {

  class Harness extends MgdRegimeActionImpl {
    def callFilter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = filter(request)
  }

  "Mgd Regime Action" - {
    "when the regime is MGD" - {
      "must return None, allowing the request to proceed" in {
        val action = new Harness
        val request = OptionalDataRequest(FakeRequest(), "regNum", Regime.MGD, None)

        val result = action.callFilter(request).futureValue
        result must not be defined
      }
    }

    "when the regime is not MGD" - {
      "must redirect to the Access Denied page" in {
        val action = new Harness
        val request = OptionalDataRequest(FakeRequest(), "regNum", Regime.GBD, None)

        val result = action.callFilter(request).futureValue

        result.value.header.status mustEqual 303
        result.value.header.headers("Location") mustEqual controllers.routes.AccessDeniedController.onPageLoad().url
      }
    }
  }
}
