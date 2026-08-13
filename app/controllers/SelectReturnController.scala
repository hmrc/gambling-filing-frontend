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

package controllers

import controllers.SelectReturnController.SortBy
import controllers.actions.{AuthorisedAction, DataRetrievalAction}

import models.{NormalMode, SelectedReturn, UserAnswers}
import navigation.BackNavigator
import pages.OpenReturnPeriodsPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.GamblingService
import utils.DateTimeFormats
import utils.QueryParameters.OrderBy
import views.html.SelectReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectReturnController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  backNavigator: BackNavigator,
  sessionRepository: SessionRepository,
  gamblingService: GamblingService,
  openReturnsView: SelectReturnView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>
      val regNum = request.regNum
      val logTxt = s"[onPageLoad] for regNum=$regNum"

      gamblingService
        .getOpenReturnPeriods(request.regime.code, regNum, SortBy.DueDate, OrderBy.Ascending)
        .flatMap { openReturnPeriods =>
          val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
          Future
            .fromTry(userAnswers.set(OpenReturnPeriodsPage, openReturnPeriods))
            .flatMap(sessionRepository.set)
            .map(_ =>
              Ok(
                openReturnsView(regNum,
                                openReturnPeriods,
                                backNavigator.backPage(
                                  OpenReturnPeriodsPage,
                                  NormalMode,
                                  request
                                )
                               )
              )
            )
        }
        .recover { case ex =>
          logger.error(s"$logTxt CALL to gamblingService.getOpenReturnPeriods FAILED", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }

  def selectOpenPeriod(consecNo: Int): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>
      request.userAnswers
        .flatMap(_.get(OpenReturnPeriodsPage))
        .flatMap(_.openPeriods.find(_.consecNo == consecNo))
        .flatMap(openPeriod => DateTimeFormats.parseMgdPeriod(openPeriod.period))
        .fold(Future.successful(Redirect(routes.SelectReturnController.onPageLoad()))) { (periodStart, periodEnd) =>
          val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
          Future
            .fromTry(userAnswers.selectPeriod(SelectedReturn(periodStart, periodEnd)))
            .flatMap(sessionRepository.set)
            .map(_ => Redirect(routes.MachinesAvailableController.onPageLoad(NormalMode)))
        }
    }
}

private object SelectReturnController {
  object SortBy {
    val Period = 1
    val DueDate = 2
    val Status = 3
  }
}
