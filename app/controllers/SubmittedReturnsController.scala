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

import controllers.actions.{AuthorisedAction, DataRetrievalAction, MgdRegimeAction}
import models.SortBy
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.GamblingService
import utils.QueryParameters.OrderBy
import views.html.{SubmittedReturnView, SubmittedReturnsView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmittedReturnsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireMgd: MgdRegimeAction,
  gamblingService: GamblingService,
  submittedReturnsView: SubmittedReturnsView,
  submittedReturnView: SubmittedReturnView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen getData andThen requireMgd).async { implicit request =>
      val regNum = request.regNum
      val logTxt = s"[onPageLoad] for regNum=$regNum"

      gamblingService
        .getSubmittedReturns(regNum, SortBy.PeriodStartDate, OrderBy.Descending)
        .map(submittedReturns => Ok(submittedReturnsView(regNum, submittedReturns)))
        .recover { case ex =>
          logger.error(s"$logTxt CALL to gamblingService.getSubmittedReturns FAILED", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }

  def viewFiledReturn(consecNo: Int): Action[AnyContent] =
    (authorise andThen getData andThen requireMgd).async { implicit request =>
      val regNum = request.regNum
      val logTxt = s"[viewFiledReturn] for regNum=$regNum"

      gamblingService
        .getSubmittedReturn(regNum, consecNo)
        .map(filedReturn => Ok(submittedReturnView(filedReturn)))
        .recover { case ex =>
          logger.error(s"$logTxt failed for regNum=$regNum consecNo=$consecNo", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }
}
