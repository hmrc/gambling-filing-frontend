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

import config.FrontendAppConfig
import controllers.actions.*
import models.Mode
import navigation.BackNavigator
import pages.{IntroductionPage, SelectReturnPage}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.IntroductionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IntroductionController @Inject() (
  authorise: AuthorisedAction,
  validate: ValidateAction,
  getData: DataRetrievalAction,
  backNavigator: BackNavigator,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: IntroductionView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    request.userAnswers.flatMap(_.get(SelectReturnPage)) match {
      case None =>
        logger.info(s"[onPageLoad] no selectedReturn found for regNum=${request.regNum}")
        Future.successful(Redirect(routes.SelectReturnController.onPageLoad()))
      case Some(selectedReturn) =>
        Future.successful(
          Ok(view(selectedReturn, appConfig.machineGamesDutyGuidanceUrl, backNavigator.backPage(IntroductionPage, mode, request)))
        )
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    request.userAnswers.flatMap(_.get(SelectReturnPage)) match {
      case None =>
        logger.info(s"[onSubmit] no selectedReturn found for regNum=${request.regNum}")
        Future.successful(Redirect(routes.SelectReturnController.onPageLoad()))
      case Some(_) =>
        Future.successful(Redirect(routes.MachinesAvailableController.onPageLoad(mode)))
    }
  }
}
