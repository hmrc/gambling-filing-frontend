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
import models.{SelectedReturn, NormalMode, Regime, UserAnswers}
import pages.SelectReturnPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.GamblingService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import utils.QueryParameters.OrderBy
import views.html.SelectReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectReturnController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  sessionRepository: SessionRepository,
  gamblingService: GamblingService,
  openReturnsView: SelectReturnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>
      val regNum = request.regNum
      val logTxt = s"[onPageLoad] for regNum=$regNum"

      request.regime match {
        case Regime.MGD =>
          gamblingService
            .getOpenReturnPeriods(request.regime.code, regNum, SortBy.DueDate, OrderBy.Ascending)
            .map(openReturns => Ok(openReturnsView(regNum, openReturns)))
            .recover { case ex =>
              logger.error(s"$logTxt CALL to gamblingService.getOpenReturnPeriods FAILED", ex)
              Redirect(controllers.routes.SystemErrorController.onPageLoad())
            }
        case _ =>
          logger.info(s"$logTxt regime ${request.regime} is not Authorised")
          Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
      }
    }

  def onSubmit(): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>
      val period = request.body.asFormUrlEncoded.flatMap(_.get("period").flatMap(_.headOption))

      period.flatMap(DateTimeFormats.parseMgdPeriod) match {
        case Some((periodStart, periodEnd)) =>
          val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
          for {
            updatedAnswers <- Future.fromTry(userAnswers.set(SelectReturnPage, SelectedReturn(periodStart, periodEnd)))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(routes.MachinesAvailableController.onPageLoad(NormalMode))
        case None =>
          logger.warn(s"[onSubmit] unable to parse period=$period")
          Future.successful(Redirect(routes.SelectReturnController.onPageLoad()))
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
