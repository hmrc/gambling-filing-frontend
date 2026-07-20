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
import pages.*
import models.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator()

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe
          routes.IndexController.onPageLoad()
      }

      "must go from MachinesAvailablePage to NetTakingsLowerRatePage" in {
        navigator.nextPage(
          MachinesAvailablePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.NetTakingsLowerRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsLowerRatePage to NetTakingsLowerPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsLowerRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsLowerController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsLowerRatePage to NetTakingsStandardRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsLowerRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(NormalMode)
      }

      "must go from Mgd LowerRatePage to NetTakingsStandardRatePage" in {
        navigator.nextPage(
          MgdLowerRatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsStandardRatePage to NetTakingsStandardPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsStandardRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsStandardController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsStandardPage to CalculatedMGDStandardRatePage" in {
        navigator.nextPage(
          NetTakingsStandardPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CalculatedMGDStandardRateController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDStandardRatePage to NetTakingsHigherPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDStandardRatePage, true)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDStandardRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsHigherController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDStandardRatePage to MgdStandardRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(CalculatedMGDStandardRatePage, false)
            .success
            .value

        navigator.nextPage(
          CalculatedMGDStandardRatePage,
          NormalMode,
          answers
        ) mustBe routes.MgdStandardRateController.onPageLoad(NormalMode)
      }

      "must go from CalculatedMGDStandardRatePage to Index when no answer exists" in {
        navigator.nextPage(
          CalculatedMGDStandardRatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.IndexController.onPageLoad()
      }

      "must go from NetTakingsStandardRatePage to NetTakingsHigherRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsStandardRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsHigherRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsHigherRatePage to NetTakingsHigherPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.NetTakingsHigherController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsHigherRatePage to under-declared-duty page when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.PageNotFoundController.onPageLoad() // TODO: /manage-gambling-tax/under-declared-duty
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from MachinesAvailablePage to NetTakingsLowerRatePage" in {
        navigator.nextPage(
          MachinesAvailablePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.NetTakingsLowerRateController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsLowerRatePage to NetTakingsLowerPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsLowerRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsLowerController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsLowerRatePage to NetTakingsStandardPage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsLowerRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsStandardPage to CalculatedMGDStandardRatePage" in {
        navigator.nextPage(
          NetTakingsStandardPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CalculatedMGDStandardRateController.onPageLoad(CheckMode)
      }

      "must go from Mgd LowerRatePage to NetTakingsStandardRatePage" in {
        navigator.nextPage(
          MgdLowerRatePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.NetTakingsStandardRateController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsStandardRatePage to NetTakingsStandardPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsStandardRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsStandardController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsStandardRatePage to NetTakingsHigherRatePage when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsStandardRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsStandardRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsHigherRateController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsHigherRatePage to NetTakingsHigherPage when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, true)
            .success
            .value

        navigator.nextPage(
          NetTakingsHigherRatePage,
          CheckMode,
          answers
        ) mustBe routes.NetTakingsHigherController.onPageLoad(CheckMode)
      }

      "must go from NetTakingsHigherRatePage to under-declared-duty page when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsHigherRatePage, false)
            .success
            .value

        navigator.nextPage(
          NetTakingsHigherRatePage,
          CheckMode,
          answers
        ) mustBe routes.PageNotFoundController.onPageLoad() // TODO: /manage-gambling-tax/under-declared-duty
      }
    }
  }
}
