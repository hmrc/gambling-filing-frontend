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
import pages.*

class BackNavigatorSpec extends SpecBase {

  val navigator = new BackNavigator()

  "BackNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page

        navigator.backPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe
          routes.IndexController.onPageLoad()
      }

      "must go from NetTakingsHigherRatePage to StandardRateCalculationCheckController when answer is Yes" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true) //TODO NetTakingsStandardRatePage
            .flatMap(a=>a.set(CalculationLowerCheckPage, true))  // TODO StandardRateCalculationCheckPage
            .success
            .value

        navigator.backPage(
          NetTakingsHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.PageNotFoundController.onPageLoad() // TODO StandardRateCalculationCheckController
      }

      "must go from NetTakingsHigherRatePage to MgdStandardRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, true) //TODO NetTakingsStandardRatePage
            .flatMap(a=>a.set(CalculationLowerCheckPage, false))  // TODO StandardRateCalculationCheckPage
            .success
            .value

        navigator.backPage(
          NetTakingsHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.MgdStandardRateController.onPageLoad(NormalMode)
      }

      "must go from NetTakingsHigherRatePage to NetTakingsStandardRateController when answer is No" in {
        val answers =
          emptyUserAnswers
            .set(NetTakingsLowerRatePage, false) //TODO NetTakingsStandardRatePage
            .success
            .value

        navigator.backPage(
          NetTakingsHigherRatePage,
          NormalMode,
          answers
        ) mustBe routes.PageNotFoundController.onPageLoad() // TODO: NetTakingsStandardRateController
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page

        navigator.backPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe
          routes.CheckYourAnswersController.onPageLoad()
      }


    }
  }
}
