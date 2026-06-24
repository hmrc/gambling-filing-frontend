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

import controllers.actions.{AuthorisedAction, DataRetrievalAction}
import models.{OrderBy, SortBy}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.GamblingService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{SubmittedReturnView, SubmittedReturnsView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmittedReturnsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  gamblingService: GamblingService,
  submittedReturnsView: SubmittedReturnsView,
  submittedReturnView: SubmittedReturnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>
      val mgdRefNum = request.mgdRefNum
      val logTxt = s"[SubmittedReturnsController][onPageLoad] for mgdRefNum=$mgdRefNum"

      gamblingService
        .getSubmittedReturns(mgdRefNum, SortBy.PeriodStartDate, OrderBy.Descending)
        .map(submittedReturns => Ok(submittedReturnsView(mgdRefNum, submittedReturns)))
        .recover { case ex =>
          logger.error(s"$logTxt CALL to gamblingService.getSubmittedReturns FAILED", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }

  def viewFiledReturn(consecNo: Int): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>
      val mgdRefNum = request.mgdRefNum

      gamblingService
        .getSubmittedReturn(mgdRefNum, consecNo)
        .map(filedReturn => Ok(submittedReturnView(filedReturn)))
        .recover { case ex =>
          logger.error(s"[SubmittedReturnsController] viewFiledReturn failed for mgdRefNum=$mgdRefNum consecNo=$consecNo", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }
}
