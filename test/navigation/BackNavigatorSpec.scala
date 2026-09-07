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

      "NetTakingsLowerRatePage" - {

        "must go from NetTakingsLowerRatePage to MachinesAvailablePage" in {

          navigator.backPage(
            NetTakingsLowerRatePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.MachinesAvailableController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "NetTakingsLowerPage" - {

        "must go from NetTakingsLowerPage to NetTakingsLowerRatePage" in {

          navigator.backPage(
            NetTakingsLowerPage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.NetTakingsLowerRateController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "CalculatedMGDLowerRatePage" - {

        "must go from CalculatedMGDLowerRatePage to NetTakingsLowerRateController" in {

          navigator.backPage(
            CalculatedMGDLowerRatePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.NetTakingsLowerController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "MgdLowerRatePage" - {

        "must go from MgdLowerRatePage to CalculatedMGDLowerRatePage" in {

          navigator.backPage(
            MgdLowerRatePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.CalculatedMGDLowerRateController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "NetTakingsStandardPage" - {

        "must go from NetTakingsStandardPage to NetTakingsStandardRatePage" in {

          navigator.backPage(
            NetTakingsStandardPage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.NetTakingsStandardRateController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "CalculatedMGDStandardRatePage" - {

        "must go from CalculatedMGDStandardRatePage to NetTakingsStandardPage" in {

          navigator.backPage(
            CalculatedMGDStandardRatePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.NetTakingsStandardController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "MgdStandardRatePage" - {

        "must go from MgdStandardRatePage to CalculatedMGDStandardRatePage" in {

          navigator.backPage(
            MgdStandardRatePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.CalculatedMGDStandardRateController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "must go from NetTakingsStandardRatePage to LowerRateCalculationCheckController when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true)
            .flatMap(_.set(CalculatedMGDLowerRatePage, true))
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(NetTakingsStandardRatePage, NormalMode, request) mustBe
          Some(routes.CalculatedMGDLowerRateController.onPageLoad(NormalMode).url)
      }

      "must go from NetTakingsStandardRatePage to Mgd LowerRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true)
            .flatMap(_.set(CalculatedMGDLowerRatePage, false))
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(
          NetTakingsStandardRatePage,
          NormalMode,
          request
        ) mustBe Some(routes.MgdLowerRateController.onPageLoad(NormalMode).url)
      }

      "must go from NetTakingsStandardRatePage to NetTakingsLowerRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, false)
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(
          NetTakingsStandardRatePage,
          NormalMode,
          request
        ) mustBe Some(routes.NetTakingsLowerRateController.onPageLoad(NormalMode).url)
      }

      "must go from CalculatedMGDStandardRatePage to NetTakingsStandardController regardless of previous answers" in {
        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(emptyUserAnswers))

        val result = navigator.backPage(CalculatedMGDStandardRatePage, NormalMode, request)
        result mustBe Some(routes.NetTakingsStandardController.onPageLoad(NormalMode).url)
      }

      "NegativeDutyBroughtForwardInputPage" - {

        "must go from NegativeDutyBroughtForwardInputPage to NegativeDutyController" in {

          navigator.backPage(
            NegativeDutyBroughtForwardInputPage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.NegativeDutyController
              .onPageLoad(NormalMode)
              .url
          )
        }
      }

      "NegativeDutyPage" - {

        "must go from NegativeDutyPage to TotalUnderDeclaredDutyController when reasonable care is No and limits is Yes" in {
          val userAnswers =
            emptyUserAnswers
              .set(UnderDeclaredDutyPage, true)
              .success
              .value
              .set(UnderDeclaredDutyReasonableCarePage, false)
              .success
              .value
              .set(UnderDeclaredDutyLimitsPage, true)
              .success
              .value

          val request =
            OptionalDataRequest[AnyContent](
              FakeRequest(),
              "reg123",
              Regime.MGD,
              Some(userAnswers)
            )

          navigator.backPage(
            NegativeDutyPage,
            NormalMode,
            request
          ) mustBe Some(
            routes.TotalUnderDeclaredDutyController.onPageLoad(NormalMode).url
          )
        }

        "must go from NegativeDutyPage to ContactHmrcController when reasonable care is Yes" in {
          val userAnswers =
            emptyUserAnswers
              .set(UnderDeclaredDutyPage, true)
              .success
              .value
              .set(UnderDeclaredDutyReasonableCarePage, true)
              .success
              .value

          val request: OptionalDataRequest[AnyContent] =
            OptionalDataRequest(
              FakeRequest(),
              "reg123",
              Regime.MGD,
              Some(userAnswers)
            )

          navigator.backPage(
            NegativeDutyPage,
            NormalMode,
            request
          ) mustBe Some(
            routes.ContactHmrcController.onPageLoad(NormalMode).url
          )
        }

        "must go from NegativeDutyPage to ContactHmrcController when limits is No" in {
          val userAnswers =
            emptyUserAnswers
              .set(UnderDeclaredDutyPage, true)
              .success
              .value
              .set(UnderDeclaredDutyReasonableCarePage, false)
              .success
              .value
              .set(UnderDeclaredDutyLimitsPage, false)
              .success
              .value

          val request: OptionalDataRequest[AnyContent] =
            OptionalDataRequest(
              FakeRequest(),
              "reg123",
              Regime.MGD,
              Some(userAnswers)
            )

          navigator.backPage(
            NegativeDutyPage,
            NormalMode,
            request
          ) mustBe Some(
            routes.ContactHmrcController.onPageLoad(NormalMode).url
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

      "must go from CalculatedMGDStandardRatePage to NetTakingsStandardController regardless of previous answers" in {
        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(emptyUserAnswers))

        val result = navigator.backPage(CalculatedMGDStandardRatePage, CheckMode, request)
        result mustBe Some(routes.NetTakingsStandardController.onPageLoad(CheckMode).url)
      }

      "must go from NegativeDutyBroughtForwardInputPage to CheckYourAnswersController" in {
        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(emptyUserAnswers))

        val result = navigator.backPage(NegativeDutyBroughtForwardInputPage, CheckMode, request)
        result mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
      }

    }
  }
}
