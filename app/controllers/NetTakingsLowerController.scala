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
import forms.NetTakingsLowerFormProvider
import models.{Mode, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{NetTakingsLowerPage, SelectReturnPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NetTakingsLowerView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NetTakingsLowerController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  formProvider: NetTakingsLowerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NetTakingsLowerView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData) { implicit request =>
    request.userAnswers
      .flatMap(_.get(SelectReturnPage))
      .fold(Redirect(controllers.routes.SelectReturnController.onPageLoad())) { selectedReturn =>
        val preparedForm = request.userAnswers.flatMap(_.get(NetTakingsLowerPage)).fold(form)(form.fill)
        Ok(view(preparedForm, mode, backNavigator.backPage(NetTakingsLowerPage, mode, request), selectedReturn))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.userAnswers
      .flatMap(_.get(SelectReturnPage))
      .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, backNavigator.backPage(NetTakingsLowerPage, mode, request), selectedReturn))),
            value => {
              val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
              for {
                updatedAnswers <- Future.fromTry(userAnswers.set(NetTakingsLowerPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(NetTakingsLowerPage, mode, updatedAnswers))
            }
          )
      }
  }
}
