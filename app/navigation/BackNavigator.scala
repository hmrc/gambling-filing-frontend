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

import controllers.routes
import models.*
import models.requests.OptionalDataRequest
import pages.*
import play.api.mvc.{AnyContent, Call}

import javax.inject.{Inject, Singleton}

@Singleton
class BackNavigator @Inject() () {

  private val normalBackRoutes: Page => UserAnswers => Call = {

    case OpenReturnPeriodsPage =>
      _ => routes.IndexController.onPageLoad()

    case IntroductionPage =>
      _ => routes.SelectReturnController.onPageLoad()

    case MachinesAvailablePage =>
      _ => routes.IntroductionController.onPageLoad()

    case NetTakingsLowerRatePage =>
      _ => routes.MachinesAvailableController.onPageLoad(NormalMode)

    case NetTakingsLowerPage =>
      _ => routes.NetTakingsLowerRateController.onPageLoad(NormalMode)

    case CalculatedMGDLowerRatePage =>
      _ => routes.NetTakingsLowerController.onPageLoad(NormalMode)

    case MgdLowerRatePage =>
      _ => routes.CalculatedMGDLowerRateController.onPageLoad(NormalMode)

    case NetTakingsStandardPage =>
      _ => routes.NetTakingsStandardRateController.onPageLoad(NormalMode)

    case CalculatedMGDStandardRatePage =>
      _ => routes.NetTakingsStandardController.onPageLoad(NormalMode)

    case NetTakingsHigherPage =>
      _ => routes.NetTakingsHigherRateController.onPageLoad(NormalMode)

    case MgdStandardRatePage =>
      _ => routes.CalculatedMGDStandardRateController.onPageLoad(NormalMode)

    case NetTakingsHigherRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsStandardRatePage) match {
          case Some(true) =>
            userAnswers.get(CalculatedMGDStandardRatePage) match {
              case Some(true)  => routes.CalculatedMGDStandardRateController.onPageLoad(NormalMode)
              case Some(false) => routes.MgdStandardRateController.onPageLoad(NormalMode)
              case None        => routes.IndexController.onPageLoad()
            }
          case Some(false) => routes.NetTakingsStandardRateController.onPageLoad(NormalMode)
          case None        => routes.IndexController.onPageLoad()
        }

    case NetTakingsStandardRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsLowerRatePage) match {
          case Some(true) =>
            userAnswers.get(CalculatedMGDLowerRatePage) match {
              case Some(true)  => routes.CalculatedMGDLowerRateController.onPageLoad(NormalMode)
              case Some(false) => routes.MgdLowerRateController.onPageLoad(NormalMode)
              case None        => routes.IndexController.onPageLoad()
            }
          case Some(false) => routes.NetTakingsLowerRateController.onPageLoad(NormalMode)
          case None        => routes.IndexController.onPageLoad()
        }

    case CalculatedMGDHigherRatePage =>
      _ => routes.NetTakingsHigherController.onPageLoad(NormalMode)

    case MgdHigherRatePage =>
      _ => routes.CalculatedMGDHigherRateController.onPageLoad(NormalMode)

    case NegativeDutyBroughtForwardInputPage =>
      _ => routes.NegativeDutyController.onPageLoad(NormalMode)

    case NegativeDutyPage =>
      userAnswers =>
        userAnswers.get(UnderDeclaredDutyPage) match {

          case Some(false) =>
            routes.UnderDeclaredDutyController.onPageLoad(NormalMode)

          case Some(true) =>
            (userAnswers.get(UnderDeclaredDutyReasonableCarePage), userAnswers.get(UnderDeclaredDutyLimitsPage)) match {

              case (Some(false), Some(true)) =>
                routes.TotalUnderDeclaredDutyController.onPageLoad(NormalMode)

              case (Some(true), _) | (_, Some(false)) =>
                routes.ContactHmrcController.onPageLoad(NormalMode)

              case _ =>
                routes.IndexController.onPageLoad()
            }

          case None =>
            routes.IndexController.onPageLoad()
        }

    case DeclareAndSubmitPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private val checkBackRouteMap: Page => UserAnswers => Call = {

    case OpenReturnPeriodsPage =>
      _ => routes.IndexController.onPageLoad()

    case IntroductionPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case MachinesAvailablePage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case NetTakingsLowerRatePage =>
      _ => routes.MachinesAvailableController.onPageLoad(CheckMode)

    case NetTakingsLowerPage =>
      _ => routes.NetTakingsLowerRateController.onPageLoad(CheckMode)

    case CalculatedMGDLowerRatePage =>
      _ => routes.NetTakingsLowerController.onPageLoad(CheckMode)

    case MgdLowerRatePage =>
      _ => routes.CalculatedMGDLowerRateController.onPageLoad(CheckMode)

    case MgdStandardRatePage =>
      _ => routes.CalculatedMGDStandardRateController.onPageLoad(CheckMode)

    case CalculatedMGDStandardRatePage =>
      _ => routes.NetTakingsStandardController.onPageLoad(CheckMode)

    case NetTakingsHigherRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsStandardRatePage) match {
          case Some(true) =>
            userAnswers.get(CalculatedMGDStandardRatePage) match {
              case Some(true)  => routes.CalculatedMGDStandardRateController.onPageLoad(CheckMode)
              case Some(false) => routes.MgdStandardRateController.onPageLoad(CheckMode)
              case None        => routes.CheckYourAnswersController.onPageLoad()
            }
          case Some(false) => routes.NetTakingsStandardRateController.onPageLoad(CheckMode)
          case None        => routes.CheckYourAnswersController.onPageLoad()
        }

    case NetTakingsStandardRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsLowerRatePage) match {
          case Some(true) =>
            userAnswers.get(CalculatedMGDLowerRatePage) match {
              case Some(true)  => routes.CalculatedMGDLowerRateController.onPageLoad(CheckMode)
              case Some(false) => routes.MgdLowerRateController.onPageLoad(CheckMode)
              case None        => routes.CheckYourAnswersController.onPageLoad()
            }
          case Some(false) => routes.NetTakingsLowerRateController.onPageLoad(CheckMode)
          case None        => routes.CheckYourAnswersController.onPageLoad()
        }

    case CalculatedMGDHigherRatePage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case NetTakingsHigherPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case MgdHigherRatePage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case NegativeDutyBroughtForwardInputPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case NegativeDutyPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case _ =>
      _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def backPage(page: Page, mode: Mode, request: OptionalDataRequest[AnyContent]): Option[String] = {
    val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
    mode match {
      case NormalMode => Some(normalBackRoutes(page)(userAnswers).url)
      case CheckMode  => Some(checkBackRouteMap(page)(userAnswers).url)
    }
  }
}
