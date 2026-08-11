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
import forms.MachinesAvailableFormProvider
import models.{Mode, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{MachinesAvailablePage, SelectReturnPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.MachinesAvailableView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MachinesAvailableController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireMgd: MgdRegimeAction,
  requireSelectReturn: SelectReturnRequiredAction,
  formProvider: MachinesAvailableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MachinesAvailableView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireMgd andThen requireSelectReturn).async { implicit request =>
      val selectedReturn = request.userAnswers.flatMap(_.get(SelectReturnPage)).get
      val preparedForm = request.userAnswers.flatMap(_.get(MachinesAvailablePage)).fold(form)(form.fill)
      Future.successful(Ok(view(preparedForm, mode, backNavigator.backPage(MachinesAvailablePage, mode, request), selectedReturn)))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireMgd andThen requireSelectReturn).async { implicit request =>
      val selectedReturn = request.userAnswers.flatMap(_.get(SelectReturnPage)).get
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future
              .successful(BadRequest(view(formWithErrors, mode, backNavigator.backPage(MachinesAvailablePage, mode, request), selectedReturn))),
          value => {
            val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
            for {
              updatedAnswers <- Future.fromTry(userAnswers.set(MachinesAvailablePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(MachinesAvailablePage, mode, updatedAnswers))
          }
        )
    }
}
