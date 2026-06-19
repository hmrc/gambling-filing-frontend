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
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.GamblingService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SubmittedReturnsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmittedReturnsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  gamblingService: GamblingService,
  view: SubmittedReturnsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(sortBy: Option[Int] = Some(3), orderBy: Option[String] = Some("ASC")): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>
      val regNumber = request.mgdRefNum
      val logTxt = s"[SubmittedReturnsController][onPageLoad] for regNumber=$regNumber : sortBy=$sortBy : orderBy=$orderBy :"

      sortBy match {
        case s @ (Some(1) | Some(2) | Some(3)) =>
          orderBy.map(_.trim.toUpperCase()) match {
            case o @ (Some("ASC") | Some("DESC")) =>
              gamblingService
                .getSubmittedReturns(regNumber, sortBy.get, orderBy.get)
                .map(submittedReturns => Ok(view(regNumber, submittedReturns, sortBy.get, orderBy.get)))
                .recover { case _ =>
                  logger.error(s"$logTxt CALL to gamblingService.getSubmittedReturns FAILED")
                  Redirect(controllers.routes.SystemErrorController.onPageLoad())
                }
            case _ =>
              logger.error(s"$logTxt INVALID orderBy")
              Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          }
        case _ =>
          logger.error(s"$logTxt INVALID sortBy")
          Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
      }
    }
}
