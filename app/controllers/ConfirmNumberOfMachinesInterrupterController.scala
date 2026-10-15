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

import controllers.actions.*
import models.CheckMode
import pages.ConfirmNumberOfMachinesInterrupterPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.ConfirmNumberOfMachinesInterrupterView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmNumberOfMachinesInterrupterController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  validate: ValidateAction,
  getData: DataRetrievalAction,
  journeyComplete: JourneyCompleteAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmNumberOfMachinesInterrupterView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen validate andThen getData andThen journeyComplete).async { implicit request =>
      Future.successful(Ok(view()))
    }

  def onSubmit(value: Boolean): Action[AnyContent] =
    (authorise andThen validate andThen getData andThen journeyComplete andThen requireData).async { implicit request =>

      Future
        .fromTry(request.userAnswers.set(ConfirmNumberOfMachinesInterrupterPage, value))
        .flatMap(sessionRepository.set)
        .map { _ =>
          if (value) {
            Redirect(controllers.routes.DeclareAndSubmitController.onPageLoad())
          } else {
            Redirect(controllers.routes.MachinesAvailableController.onPageLoad(CheckMode))
          }
        }
    }
}
