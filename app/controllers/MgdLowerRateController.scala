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
import forms.MgdLowerRateFormProvider
import models.{Mode, Regime, UserAnswers}
import navigation.Navigator
import pages.MgdLowerRatePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.MgdLowerRateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MgdLowerRateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  formProvider: MgdLowerRateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MgdLowerRateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.regime match {
      case Regime.MGD =>
        val preparedForm = request.userAnswers.flatMap(_.get(MgdLowerRatePage)).fold(form)(form.fill)
        Future.successful(Ok(view(preparedForm, mode)))
      case _ =>
        logger.info(s"[onPageLoad] regime ${request.regime} is not Authorised")
        Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.regime match {
      case Regime.MGD =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value => {
              val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
              for {
                updatedAnswers <- Future.fromTry(userAnswers.set(MgdLowerRatePage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(MgdLowerRatePage, mode, updatedAnswers))
            }
          )
      case _ =>
        logger.info(s"[onSubmit] regime ${request.regime} is not Authorised")
        Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
    }
  }
}
