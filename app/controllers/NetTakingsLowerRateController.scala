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
import forms.NetTakingsLowerRateFormProvider
import models.{Mode, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{NetTakingsLowerRatePage, SelectReturnPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.NetTakingsLowerRateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NetTakingsLowerRateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  formProvider: NetTakingsLowerRateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NetTakingsLowerRateView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.userAnswers
      .flatMap(_.get(SelectReturnPage))
      .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
        val preparedForm = request.userAnswers.flatMap(_.get(NetTakingsLowerRatePage)).fold(form)(form.fill)
        Future.successful(Ok(view(preparedForm, mode, backNavigator.backPage(NetTakingsLowerRatePage, mode, request), selectedReturn)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.userAnswers.flatMap(_.get(SelectReturnPage)) match {
      case None =>
        logger.info(s"[onSubmit] no selectedReturn found for regNum=${request.regNum}")
        Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))
      case Some(selectedReturn) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, mode, backNavigator.backPage(NetTakingsLowerRatePage, mode, request), selectedReturn))
              ),
            value => {
              val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
              for {
                updatedAnswers <- Future.fromTry(userAnswers.set(NetTakingsLowerRatePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(NetTakingsLowerRatePage, mode, updatedAnswers))
            }
          )
    }
  }
}
