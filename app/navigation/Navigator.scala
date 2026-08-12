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
import pages.{TotalUnderDeclaredDutyPage, *}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

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
            routes.NetTakingsStandardRateController.onPageLoad(NormalMode)

          case None =>
            routes.IndexController.onPageLoad()
        }

    case NetTakingsLowerPage =>
      _ => routes.CalculatedMGDLowerRateController.onPageLoad(NormalMode)

    case CalculatedMGDLowerRatePage =>
      userAnswers =>
        userAnswers.get(CalculatedMGDLowerRatePage) match {
          case Some(true) =>
            routes.NetTakingsStandardRateController.onPageLoad(NormalMode)

          case Some(false) =>
            routes.MgdLowerRateController.onPageLoad(NormalMode)

          case None =>
            routes.IndexController.onPageLoad()
        }

    case MgdLowerRatePage =>
      _ => routes.NetTakingsStandardRateController.onPageLoad(NormalMode)

    case NetTakingsStandardRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsStandardRatePage) match {
          case Some(true) =>
            routes.NetTakingsStandardController.onPageLoad(NormalMode)

          case Some(false) =>
            routes.NetTakingsHigherRateController.onPageLoad(NormalMode)

          case None =>
            routes.IndexController.onPageLoad()
        }

    case NetTakingsStandardPage =>
      _ => routes.CalculatedMGDStandardRateController.onPageLoad(NormalMode)

    case CalculatedMGDStandardRatePage =>
      userAnswers =>
        userAnswers
          .get(CalculatedMGDStandardRatePage)
          .map {
            case true => routes.NetTakingsHigherRateController.onPageLoad(NormalMode)
            case _    => routes.MgdStandardRateController.onPageLoad(NormalMode)
          }
          .getOrElse(routes.IndexController.onPageLoad())

    case MgdStandardRatePage =>
      _.get(MgdStandardRatePage)
        .map(_ => routes.NetTakingsHigherRateController.onPageLoad(NormalMode))
        .getOrElse(routes.IndexController.onPageLoad())

    case NetTakingsHigherRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsHigherRatePage) match {
          case Some(true)  => routes.NetTakingsHigherController.onPageLoad(NormalMode)
          case Some(false) => routes.UnderDeclaredDutyController.onPageLoad(NormalMode)
          case None        => routes.IndexController.onPageLoad()
        }

    case NetTakingsHigherPage =>
      _ => routes.CalculatedMGDHigherRateController.onPageLoad(NormalMode)

    case UnderDeclaredDutyPage =>
      userAnswers =>
        userAnswers.get(UnderDeclaredDutyPage) match {
          case Some(true) => routes.UnderDeclaredDutyReasonableCareController.onPageLoad(NormalMode)
          case Some(false) =>
            routes.PageNotFoundController
              .onPageLoad() // TODO: /manage-gambling-tax/returns/duty-brought-forward (FAR-NEG-SCR) NormalMode  20. FAR-NEG-SCR - File a Return - Negative duty brought forward (screener)
          case None => routes.IndexController.onPageLoad()
        }

    case CalculatedMGDHigherRatePage =>
      userAnswers =>
        userAnswers.get(CalculatedMGDHigherRatePage) match {
          case Some(true)  => routes.UnderDeclaredDutyController.onPageLoad(NormalMode)
          case Some(false) => routes.MgdHigherRateController.onPageLoad(NormalMode)
          case None        => routes.IndexController.onPageLoad()
        }

    case MgdHigherRatePage =>
      _ => routes.UnderDeclaredDutyController.onPageLoad(NormalMode)

    case UnderDeclaredDutyLimitsPage =>
      userAnswers =>
        userAnswers.get(UnderDeclaredDutyLimitsPage) match {
          case Some(true) =>
            routes.TotalUnderDeclaredDutyController.onPageLoad(NormalMode)

          case Some(false) =>
            routes.ContactHmrcController.onPageLoad(NormalMode)

          case None =>
            routes.JourneyRecoveryController.onPageLoad()
        }

    case UnderDeclaredDutyReasonableCarePage =>
      _.get(UnderDeclaredDutyReasonableCarePage) match {
        case Some(true)  => routes.ContactHmrcController.onPageLoad(NormalMode)
        case Some(false) => routes.UnderDeclaredDutyLimitsController.onPageLoad(NormalMode)
        case None        => routes.SelectReturnController.onPageLoad()
      }

    case ContactHmrcPage => // TODO: /manage-gambling-tax/returns/duty-brought-forward
      _ => routes.IndexController.onPageLoad()

    case TotalUnderDeclaredDutyPage =>
      _ => routes.IndexController.onPageLoad()

    case NegativeDutyBroughtForwardInputPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case NegativeDutyPage =>
      _.get(NegativeDutyPage) match {
        case Some(true) => routes.NegativeDutyBroughtForwardInputController.onPageLoad(NormalMode)

        case Some(false) => routes.CheckYourAnswersController.onPageLoad()

        case None => routes.IndexController.onPageLoad()
      }

    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {

    case NegativeDutyBroughtForwardInputPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case MachinesAvailablePage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case NetTakingsLowerRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsLowerRatePage) match {
          case Some(true) =>
            routes.NetTakingsLowerController.onPageLoad(CheckMode)

          case Some(false) =>
            routes.NetTakingsStandardRateController.onPageLoad(CheckMode)

          case None =>
            routes.CheckYourAnswersController.onPageLoad()
        }

    case NetTakingsLowerPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case CalculatedMGDLowerRatePage =>
      userAnswers =>
        userAnswers.get(CalculatedMGDLowerRatePage) match {
          case Some(true) =>
            routes.NetTakingsStandardRateController.onPageLoad(CheckMode)

          case Some(false) =>
            routes.MgdLowerRateController.onPageLoad(CheckMode)

          case None =>
            routes.CheckYourAnswersController.onPageLoad()
        }

    case MgdLowerRatePage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case NetTakingsStandardPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case CalculatedMGDStandardRatePage =>
      userAnswers =>
        userAnswers
          .get(CalculatedMGDStandardRatePage)
          .map {
            case true => routes.NetTakingsHigherRateController.onPageLoad(CheckMode)
            case _    => routes.MgdStandardRateController.onPageLoad(CheckMode)
          }
          .getOrElse(routes.CheckYourAnswersController.onPageLoad())

    case NetTakingsStandardRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsStandardRatePage) match {
          case Some(true) =>
            routes.NetTakingsStandardController.onPageLoad(CheckMode)

          case Some(false) =>
            routes.NetTakingsHigherRateController.onPageLoad(CheckMode)

          case None =>
            routes.CheckYourAnswersController.onPageLoad()
        }

    case NetTakingsHigherRatePage =>
      userAnswers =>
        userAnswers.get(NetTakingsHigherRatePage) match {
          case Some(true) =>
            routes.NetTakingsHigherController.onPageLoad(CheckMode)

          case Some(false) =>
            routes.UnderDeclaredDutyController.onPageLoad(CheckMode)

          case None =>
            routes.CheckYourAnswersController.onPageLoad()
        }

    case NetTakingsHigherPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case MgdStandardRatePage =>
      _.get(MgdStandardRatePage)
        .map(_ => routes.NetTakingsHigherRateController.onPageLoad(CheckMode))
        .getOrElse(routes.CheckYourAnswersController.onPageLoad())

    case UnderDeclaredDutyPage =>
      userAnswers =>
        userAnswers.get(UnderDeclaredDutyPage) match {
          case Some(true) => routes.UnderDeclaredDutyReasonableCareController.onPageLoad(CheckMode)
          case Some(false) =>
            routes.PageNotFoundController
              .onPageLoad() // TODO: /manage-gambling-tax/returns/duty-brought-forward (FAR-NEG-SCR)                   CheckMode  20. FAR-NEG-SCR - File a Return - Negative duty brought forward (screener)
          case None => routes.CheckYourAnswersController.onPageLoad()
        }

    case CalculatedMGDHigherRatePage =>
      userAnswers =>
        userAnswers.get(CalculatedMGDHigherRatePage) match {
          case Some(true)  => routes.UnderDeclaredDutyController.onPageLoad(CheckMode)
          case Some(false) => routes.MgdHigherRateController.onPageLoad(CheckMode)
          case None        => routes.CheckYourAnswersController.onPageLoad()
        }

    case MgdHigherRatePage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case UnderDeclaredDutyLimitsPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case UnderDeclaredDutyReasonableCarePage =>
      _.get(UnderDeclaredDutyReasonableCarePage) match {
        case Some(true)  => routes.ContactHmrcController.onPageLoad(CheckMode)
        case Some(false) => routes.UnderDeclaredDutyLimitsController.onPageLoad(CheckMode)
        case None        => routes.CheckYourAnswersController.onPageLoad()
      }

    case ContactHmrcPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case TotalUnderDeclaredDutyPage =>
      _ => routes.CheckYourAnswersController.onPageLoad()

    case NegativeDutyPage =>
      _.get(NegativeDutyPage) match {
        case Some(true)  => routes.NegativeDutyBroughtForwardInputController.onPageLoad(CheckMode)
        case Some(false) => routes.CheckYourAnswersController.onPageLoad()
        case None        => routes.IndexController.onPageLoad()
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
