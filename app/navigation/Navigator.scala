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

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages.*
import models.*

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {

    case MachinesAvailablePage =>
      _ => routes.NetTakingsLowerRateController.onPageLoad(NormalMode)

    case NetTakingsLowerRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsLowerRatePage) match {
          case Some(true) =>
            routes.NetTakingsLowerController.onPageLoad(NormalMode)

          case Some(false) =>
            routes.NetTakingsStandardController.onPageLoad(NormalMode)

          case None =>
            routes.IndexController.onPageLoad()
        }

    case NetTakingsLowerPage =>
      _ => routes.CalculationLowerCheckController.onPageLoad(NormalMode)

    case CalculationLowerCheckPage =>
      userAnswers =>
        userAnswers.get(CalculationLowerCheckPage) match {
          case Some(true) =>
            routes.NetTakingsStandardController.onPageLoad(NormalMode)

          case Some(false) =>
            routes.MgdLowerRateController.onPageLoad(NormalMode)

          case None =>
            routes.IndexController.onPageLoad()
        }

    case NetTakingsHigherRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsHigherRatePage) match {
          case Some(true) =>
            routes.NetTakingsHigherController.onPageLoad(NormalMode)

          case Some(false) =>
            routes.PageNotFoundController.onPageLoad() // TODO: /manage-gambling-tax/under-declared-duty

          case None =>
            routes.IndexController.onPageLoad()
        }

    case _ =>
      _ => routes.IndexController.onPageLoad()

  }

  private val checkRouteMap: Page => UserAnswers => Call = {

    case MachinesAvailablePage =>
      _ => routes.NetTakingsLowerRateController.onPageLoad(CheckMode)

    case NetTakingsLowerRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsLowerRatePage) match {
          case Some(true) =>
            routes.NetTakingsLowerController.onPageLoad(CheckMode)

          case Some(false) =>
            routes.NetTakingsStandardController.onPageLoad(CheckMode)

          case None =>
            routes.CheckYourAnswersController.onPageLoad()
        }

    case NetTakingsLowerPage =>
      _ => routes.CalculationLowerCheckController.onPageLoad(CheckMode)

    case NetTakingsHigherRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsHigherRatePage) match {
          case Some(true) =>
            routes.NetTakingsHigherController.onPageLoad(CheckMode)

          case Some(false) =>
            routes.PageNotFoundController.onPageLoad() // TODO: /manage-gambling-tax/under-declared-duty

          case None =>
            routes.CheckYourAnswersController.onPageLoad()
        }

    case _ =>
      _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)
      case CheckMode  => checkRouteMap(page)(userAnswers)
    }
}
