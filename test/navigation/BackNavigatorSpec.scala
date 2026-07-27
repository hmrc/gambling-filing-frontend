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

      "MachinesAvailablePage" - {

        "must go from MachinesAvailablePage to SelectReturnPage" in {

          navigator.backPage(
            MachinesAvailablePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.SelectReturnController
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

      "CalculationLowerCheckPage" - {

        "must go from CalculationLowerCheckPage to NetTakingsLowerRateController" in {

          navigator.backPage(
            CalculationLowerCheckPage,
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

        "must go from MgdLowerRatePage to CalculationLowerCheckPage" in {

          navigator.backPage(
            MgdLowerRatePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.CalculationLowerCheckController
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

      "NetTakingsHigherPage" - {

        "must go from NetTakingsHigherPage to NetTakingsHigherRatePage" in {

          navigator.backPage(
            NetTakingsHigherPage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(
            routes.NetTakingsHigherRateController
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

      "must go from NetTakingsHigherRatePage to CalculatedMGDStandardRateController when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, true)
            .flatMap(_.set(CalculatedMGDStandardRatePage, true))
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(NetTakingsHigherRatePage, NormalMode, request) mustBe
          Some(routes.CalculatedMGDStandardRateController.onPageLoad(NormalMode).url)
      }

      "must go from NetTakingsHigherRatePage to CalculatedMGDStandardRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, true)
            .flatMap(_.set(CalculatedMGDStandardRatePage, false))
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
            .set(NetTakingsStandardRatePage, false)
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(
          NetTakingsHigherRatePage,
          NormalMode,
          request
        ) mustBe Some(routes.NetTakingsStandardRateController.onPageLoad(NormalMode).url)
      }

      "must go from NetTakingsStandardRatePage to LowerRateCalculationCheckController when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true)
            .flatMap(_.set(CalculationLowerCheckPage, true))
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(NetTakingsStandardRatePage, NormalMode, request) mustBe
          Some(routes.CalculationLowerCheckController.onPageLoad(NormalMode).url)
      }

      "must go from NetTakingsStandardRatePage to Mgd LowerRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true)
            .flatMap(_.set(CalculationLowerCheckPage, false))
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

      "CalculatedMGDHigherRatePage" - {

        "must go from CalculatedMGDHigherRatePage to NetTakingsHigherPage" in {

          navigator.backPage(
            CalculatedMGDHigherRatePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(routes.NetTakingsHigherController.onPageLoad(NormalMode).url)
        }
      }

      "UnderDeclaredDutyLimitsPage" - {

        "must go from UnderDeclaredDutyLimitsPage to UnderDeclaredDutyReasonableCarePage" in {

          navigator.backPage(
            UnderDeclaredDutyLimitsPage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(routes.UnderDeclaredDutyReasonableCareController.onPageLoad(NormalMode).url)
        }
      }

      "UnderDeclaredDutyReasonableCarePage" - {

        "must go from UnderDeclaredDutyReasonableCarePage to UnderDeclaredDutyPage" in {
          navigator.backPage(
            UnderDeclaredDutyReasonableCarePage,
            NormalMode,
            optionalDataRequest
          ) mustBe Some(routes.UnderDeclaredDutyController.onPageLoad(NormalMode).url)
        }
      }

      "must go from UnderDeclaredDutyPage to CalculatedMGDHigherRateController when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, true)
            .flatMap(_.set(CalculatedMGDHigherRatePage, true))
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(UnderDeclaredDutyPage, NormalMode, request) mustBe
          Some(routes.CalculatedMGDHigherRateController.onPageLoad(NormalMode).url)
      }

      "must go from UnderDeclaredDutyPage to MgdHigherRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, true)
            .flatMap(_.set(CalculatedMGDHigherRatePage, false))
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(
          UnderDeclaredDutyPage,
          NormalMode,
          request
        ) mustBe Some(routes.MgdHigherRateController.onPageLoad(NormalMode).url)
      }

      "must go from UnderDeclaredDutyPage to NetTakingsHigherRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, false)
            .success
            .value

        val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

        navigator.backPage(
          UnderDeclaredDutyPage,
          NormalMode,
          request
        ) mustBe Some(routes.NetTakingsHigherRateController.onPageLoad(NormalMode).url)
      }

      "must go from MgdHigherRatePage to CalculatedMGDHigherRatePage" in {
        navigator.backPage(
          MgdHigherRatePage,
          NormalMode,
          optionalDataRequest
        ) mustBe Some(
          routes.CalculatedMGDHigherRateController
            .onPageLoad(NormalMode)
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

    "must go from NetTakingsHigherRatePage to CalculatedMGDStandardRateController when answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(NetTakingsStandardRatePage, true)
          .flatMap(_.set(CalculatedMGDStandardRatePage, true))
          .success
          .value

      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

      val result = navigator.backPage(NetTakingsHigherRatePage, CheckMode, request)
      result mustBe Some(routes.CalculatedMGDStandardRateController.onPageLoad(CheckMode).url)
    }

    "must go from NetTakingsHigherRatePage to Mgd StandardRateController when answer is No" in {
      val answers =
        emptyUserAnswers
          .set(NetTakingsStandardRatePage, true)
          .flatMap(_.set(CalculatedMGDStandardRatePage, false))
          .success
          .value

      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

      val result = navigator.backPage(NetTakingsHigherRatePage, CheckMode, request)
      result mustBe Some(routes.MgdStandardRateController.onPageLoad(CheckMode).url)
    }

    "must go from NetTakingsHigherRatePage to CheckYourAnswers when NetTakingsStandardRatePage is Yes and CalculatedMGDStandardRatePage answer is missing" in {
      val answers = emptyUserAnswers.set(NetTakingsStandardRatePage, true).success.value

      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

      val result = navigator.backPage(NetTakingsHigherRatePage, CheckMode, request)
      result mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

    "must go from NetTakingsHigherRatePage to NetTakingsStandardRateController when answer is No" in {
      val answers = emptyUserAnswers.set(NetTakingsStandardRatePage, false).success.value

      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

      val result = navigator.backPage(NetTakingsHigherRatePage, CheckMode, request)
      result mustBe Some(routes.NetTakingsStandardRateController.onPageLoad(CheckMode).url)
    }

    "must go from NetTakingsHigherRatePage to CheckYourAnswers when no answer exists" in {
      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(emptyUserAnswers))

      val result = navigator.backPage(NetTakingsHigherRatePage, CheckMode, request)
      result mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

    "must go from CalculatedMGDStandardRatePage to NetTakingsStandardController regardless of previous answers" in {
      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(emptyUserAnswers))

      val result = navigator.backPage(CalculatedMGDStandardRatePage, CheckMode, request)
      result mustBe Some(routes.NetTakingsStandardController.onPageLoad(CheckMode).url)
    }
    "must go from UnderDeclaredDutyPage to CalculatedMGDHigherRateController when answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(NetTakingsHigherRatePage, true)
          .flatMap(_.set(CalculatedMGDHigherRatePage, true))
          .success
          .value

      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

      navigator.backPage(UnderDeclaredDutyPage, CheckMode, request) mustBe
        Some(routes.CalculatedMGDHigherRateController.onPageLoad(CheckMode).url)
    }

    "must go from UnderDeclaredDutyPage to MgdHigherRatePage when answer is No" in {
      val answers =
        emptyUserAnswers
          .set(NetTakingsHigherRatePage, true)
          .flatMap(_.set(CalculatedMGDHigherRatePage, false))
          .success
          .value

      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

      navigator.backPage(
        UnderDeclaredDutyPage,
        CheckMode,
        request
      ) mustBe Some(routes.MgdHigherRateController.onPageLoad(CheckMode).url)
    }

    "must go from UnderDeclaredDutyPage to NetTakingsHigherRateController when answer is No" in {
      val answers =
        emptyUserAnswers
          .set(NetTakingsHigherRatePage, false)
          .success
          .value

      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(answers))

      navigator.backPage(
        UnderDeclaredDutyPage,
        CheckMode,
        request
      ) mustBe Some(routes.NetTakingsHigherRateController.onPageLoad(CheckMode).url)
    }

    "must go from UnderDeclaredDutyLimitsPage to CheckYourAnswer" in {
      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(emptyUserAnswers))

      val result = navigator.backPage(UnderDeclaredDutyLimitsPage, CheckMode, request)
      result mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

    "must go from UnderDeclaredDutyReasonableCarePage to CheckYourAnswers regardless of previous answers" in {
      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(emptyUserAnswers))

      val result = navigator.backPage(UnderDeclaredDutyReasonableCarePage, CheckMode, request)
      result mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

    "must go from MgdHigherRatePage to CheckYourAnswers when no answer exists" in {
      val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest(), "reg123", Regime.MGD, Some(emptyUserAnswers))

      val result = navigator.backPage(MgdHigherRatePage, CheckMode, request)
      result mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

  }
}
