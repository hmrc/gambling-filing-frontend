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
import forms.CalculatedMGDHigherRateFormProvider
import models.{Mode, SelectedReturn, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{CalculatedMGDHigherRatePage, MgdHigherRatePage, NetTakingsHigherPage, SelectReturnPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.CalculatedMGDHigherRateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatedMGDHigherRateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  formProvider: CalculatedMGDHigherRateFormProvider,
  frontendAppConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: CalculatedMGDHigherRateView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  private val form = formProvider()
  private val ratePercentage = frontendAppConfig.higherRateDutyPercentage * 100

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    val radioAnswer = request.userAnswers.flatMap(_.get(CalculatedMGDHigherRatePage)).fold(form)(form.fill)

    request.userAnswers
      .flatMap(_.get(SelectReturnPage))
      .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
        request.userAnswers
          .flatMap(_.get(NetTakingsHigherPage))
          .fold(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))) { netTakings =>
            val duty = netTakings * frontendAppConfig.higherRateDutyPercentage
            Future.successful(
              Ok(
                view(radioAnswer,
                     netTakings,
                     duty,
                     ratePercentage,
                     mode,
                     backNavigator.backPage(CalculatedMGDHigherRatePage, mode, request),
                     selectedReturn
                    )
              )
            )
          }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.userAnswers
      .flatMap(_.get(SelectReturnPage))
      .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
        request.userAnswers
          .flatMap(_.get(NetTakingsHigherPage))
          .fold(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))) { netTakings =>
            val duty = netTakings * frontendAppConfig.higherRateDutyPercentage

            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(formWithErrors,
                           netTakings,
                           duty,
                           ratePercentage,
                           mode,
                           backNavigator.backPage(CalculatedMGDHigherRatePage, mode, request),
                           selectedReturn
                          )
                    )
                  ),
                value => {
                  val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
                  for {
                    updatedAnswers <- Future.fromTry(userAnswers.set(CalculatedMGDHigherRatePage, value))
                    finalAnswers <-
                      if (value) Future.fromTry(updatedAnswers.set(MgdHigherRatePage, duty))
                      else Future.successful(updatedAnswers)
                    _ <- sessionRepository.set(finalAnswers)
                  } yield Redirect(navigator.nextPage(CalculatedMGDHigherRatePage, mode, finalAnswers))
                }
              )
          }
      }
  }
}
