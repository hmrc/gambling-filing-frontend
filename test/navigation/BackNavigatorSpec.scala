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

package navigation

import base.SpecBase
import controllers.routes
import models.*
import models.requests.OptionalDataRequest
import pages.*
import play.api.mvc.AnyContent
import play.api.test.FakeRequest

class BackNavigatorSpec extends SpecBase {

  val navigator = new BackNavigator()

  "BackNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(UserAnswers("id")))
        navigator.backPage(UnknownPage, NormalMode, request) mustBe
          Some(routes.IndexController.onPageLoad().url)
      }

      "must go from NetTakingsHigherRatePage to StandardRateCalculationCheckController when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true) // TODO NetTakingsStandardRatePage
            .flatMap(_.set(CalculationLowerCheckPage, true)) // TODO StandardRateCalculationCheckPage
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(NetTakingsHigherRatePage, NormalMode, request) mustBe
          Some(routes.PageNotFoundController.onPageLoad().url) // TODO StandardRateCalculationCheckController
      }

      "must go from NetTakingsHigherRatePage to MgdStandardRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true) // TODO NetTakingsStandardRatePage
            .flatMap(_.set(CalculationLowerCheckPage, false)) // TODO StandardRateCalculationCheckPage
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(
          NetTakingsHigherRatePage,
          NormalMode,
          request
        ) mustBe Some(routes.MgdStandardRateController.onPageLoad(NormalMode).url)
      }

      "must go from NetTakingsHigherRatePage to NetTakingsStandardRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, false) // TODO NetTakingsStandardRatePage
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(
          NetTakingsHigherRatePage,
          NormalMode,
          request
        ) mustBe Some(routes.PageNotFoundController.onPageLoad().url) // TODO: NetTakingsStandardRateController
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(UserAnswers("id")))
        navigator.backPage(UnknownPage, CheckMode, request) mustBe
          Some(routes.CheckYourAnswersController.onPageLoad().url)
      }

    }
  }
}
