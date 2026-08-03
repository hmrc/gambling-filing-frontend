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
import forms.MgdStandardRateFormProvider
import models.{Mode, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{MgdStandardRatePage, SelectReturnPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.MgdStandardRateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MgdStandardRateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  validate: ValidateAction,
  getData: DataRetrievalAction,
  formProvider: MgdStandardRateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MgdStandardRateView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    whenMgd {
      request.userAnswers
        .flatMap(_.get(SelectReturnPage))
        .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
          val preparedForm = request.userAnswers.flatMap(_.get(MgdStandardRatePage)).fold(form)(form.fill)
          Future.successful(Ok(view(preparedForm, mode, backNavigator.backPage(MgdStandardRatePage, mode, request), selectedReturn)))
        }
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    whenMgd {
      request.userAnswers
        .flatMap(_.get(SelectReturnPage))
        .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future
                  .successful(BadRequest(view(formWithErrors, mode, backNavigator.backPage(MgdStandardRatePage, mode, request), selectedReturn))),
              value => {
                val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
                for {
                  updatedAnswers <- Future.fromTry(userAnswers.set(MgdStandardRatePage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(MgdStandardRatePage, mode, updatedAnswers))
              }
            )
        }
    }
  }
}
