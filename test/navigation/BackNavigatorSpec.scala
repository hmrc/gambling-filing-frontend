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

  private val optionalDataRequest: OptionalDataRequest[AnyContent] =
    OptionalDataRequest(
      FakeRequest(),
      "reg123",
      Regime.MGD,
      None
    )

  "BackNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(UserAnswers("id")))
        navigator.backPage(UnknownPage, NormalMode, request) mustBe
          Some(routes.IndexController.onPageLoad().url)
      }

      "OpenReturnPeriodsPage" - {

        "must go from OpenReturnPeriodsPage to IndexController" in {

          navigator.backPage(
            OpenReturnPeriodsPage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.IndexController
              .onPageLoad()
              .url
          )
        }
      }

      "IntroductionPage" - {

        "must go from IntroductionPage to the SelectReturn page" in {

          navigator.backPage(
            IntroductionPage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.SelectReturnController
              .onPageLoad()
              .url
          )
        }
      }

      "MachinesAvailablePage" - {

        "must go from MachinesAvailablePage to the Introduction page" in {

          navigator.backPage(
            MachinesAvailablePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.IntroductionController
              .onPageLoad()
              .url
          )
        }
      }

      "DeclareAndSubmitPage" - {

        "must go from DeclareAndSubmitPage to the CheckYourAnswers page" in {

          navigator.backPage(
            DeclareAndSubmitPage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.CheckYourAnswersController
              .onPageLoad()
              .url
          )
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(UserAnswers("id")))
        navigator.backPage(UnknownPage, CheckMode, request) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
      }

      "must go from IntroductionPage to CheckYourAnswers" in {
        navigator.backPage(IntroductionPage, CheckMode, optionalDataRequest) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
      }

      "must go from MachinesAvailablePage to CheckYourAnswers" in {
        navigator.backPage(MachinesAvailablePage, CheckMode, optionalDataRequest) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
      }
    }
  }
}
