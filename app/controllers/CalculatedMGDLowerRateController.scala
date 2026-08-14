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
import forms.CalculatedMGDLowerRateFormProvider
import models.{Mode, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{CalculatedMGDLowerRatePage, DutyLowerRatePage, NetTakingsLowerPage, SelectReturnPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.CalculatedMGDLowerRateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatedMGDLowerRateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  validate: ValidateAction,
  getData: DataRetrievalAction,
  formProvider: CalculatedMGDLowerRateFormProvider,
  frontendAppConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: CalculatedMGDLowerRateView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    val radioAnswer = request.userAnswers.flatMap(_.get(CalculatedMGDLowerRatePage)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    (request.userAnswers.flatMap(_.get(NetTakingsLowerPage)), request.userAnswers.flatMap(_.get(SelectReturnPage))) match {
      case (Some(netTakings), Some(selectedReturn)) =>
        val duty = netTakings * frontendAppConfig.lowerRateDutyPercentage

        val percentage = if (netTakings == 0) BigDecimal(0) else frontendAppConfig.lowerRateDutyPercentage * 100

        Future.successful(
          Ok(
            view(radioAnswer, netTakings, duty, percentage, mode, backNavigator.backPage(CalculatedMGDLowerRatePage, mode, request), selectedReturn)
          )
        )

      case _ =>
        Future.successful(
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        )
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    (request.userAnswers.flatMap(_.get(NetTakingsLowerPage)), request.userAnswers.flatMap(_.get(SelectReturnPage))) match {
      case (Some(netTakings), Some(selectedReturn)) =>
        val duty = netTakings * frontendAppConfig.lowerRateDutyPercentage

        val percentage = if (netTakings == 0) BigDecimal(0) else frontendAppConfig.lowerRateDutyPercentage * 100

        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(formWithErrors,
                       netTakings,
                       duty,
                       percentage,
                       mode,
                       backNavigator.backPage(CalculatedMGDLowerRatePage, mode, request),
                       selectedReturn
                      )
                )
              ),
            value => {
              val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
              for {
                updatedAnswers <- Future.fromTry(userAnswers.set(CalculatedMGDLowerRatePage, value))

                finalAnswers <- if (value) {
                                  Future.fromTry(updatedAnswers.set(DutyLowerRatePage, duty))
                                } else {
                                  Future.successful(updatedAnswers)
                                }

                _ <- sessionRepository.set(finalAnswers)
              } yield Redirect(navigator.nextPage(CalculatedMGDLowerRatePage, mode, finalAnswers))
            }
          )

      case _ =>
        Future.successful(
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        )
    }
  }
}
