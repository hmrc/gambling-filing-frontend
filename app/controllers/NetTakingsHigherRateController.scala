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
import forms.NetTakingsHigherRateFormProvider
import models.{Mode, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{NetTakingsHigherRatePage, SelectReturnPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.NetTakingsHigherRateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NetTakingsHigherRateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  validate: ValidateAction,
  getData: DataRetrievalAction,
  formProvider: NetTakingsHigherRateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NetTakingsHigherRateView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    request.userAnswers.flatMap(_.get(SelectReturnPage)) match {
      case None =>
        logger.info(s"[onPageLoad] no selectedReturn found for regNum=${request.regNum}")
        Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))
      case Some(selectedReturn) =>
        val preparedForm = request.userAnswers.flatMap(_.get(NetTakingsHigherRatePage)).fold(form)(value => form.fill(value))
        Future.successful(Ok(view(preparedForm, mode, backNavigator.backPage(NetTakingsHigherRatePage, mode, request), selectedReturn)))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
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
                BadRequest(view(formWithErrors, mode, backNavigator.backPage(NetTakingsHigherRatePage, mode, request), selectedReturn))
              ),
            value => {
              val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
              for {
                updatedAnswers <- Future.fromTry(userAnswers.set(NetTakingsHigherRatePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(NetTakingsHigherRatePage, mode, updatedAnswers))
            }
          )
    }
  }
}
