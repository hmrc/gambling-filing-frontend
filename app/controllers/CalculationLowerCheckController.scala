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
import forms.CalculationLowerCheckFormProvider
import models.{Mode, Regime, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{CalculationLowerCheckPage, DutyLowerRatePage, NetTakingsLowerPage, SelectReturnPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CalculationLowerCheckView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculationLowerCheckController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  formProvider: CalculationLowerCheckFormProvider,
  frontendAppConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: CalculationLowerCheckView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.regime match {
      case Regime.MGD =>
        val radioAnswer = request.userAnswers.flatMap(_.get(CalculationLowerCheckPage)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        (request.userAnswers.flatMap(_.get(NetTakingsLowerPage)), request.userAnswers.flatMap(_.get(SelectReturnPage))) match {
          case (Some(netTakings), Some(selectedReturn)) =>
            val duty = netTakings * BigDecimal(frontendAppConfig.lowerRateDutyPercentage)

            val percentage = if (netTakings == 0) 0 else frontendAppConfig.lowerRateDutyPercentage * 100

            Future.successful(
              Ok(
                view(radioAnswer,
                     netTakings,
                     duty,
                     percentage,
                     mode,
                     backNavigator.backPage(CalculationLowerCheckPage, mode, request),
                     selectedReturn
                    )
              )
            )

          case _ =>
            Future.successful(
              Redirect(routes.JourneyRecoveryController.onPageLoad())
            )
        }

      case _ =>
        logger.info(s"[onPageLoad] regime ${request.regime} is not Authorised")
        Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.regime match {
      case Regime.MGD =>
        (request.userAnswers.flatMap(_.get(NetTakingsLowerPage)), request.userAnswers.flatMap(_.get(SelectReturnPage))) match {
          case (Some(netTakings), Some(selectedReturn)) =>
            val duty = netTakings * BigDecimal(frontendAppConfig.lowerRateDutyPercentage)

            val percentage = if (netTakings == 0) 0 else frontendAppConfig.lowerRateDutyPercentage * 100

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
                           backNavigator.backPage(CalculationLowerCheckPage, mode, request),
                           selectedReturn
                          )
                    )
                  ),
                value => {
                  val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
                  for {
                    updatedAnswers <- Future.fromTry(userAnswers.set(CalculationLowerCheckPage, value))

                    finalAnswers <- if (value) {
                                      Future.fromTry(updatedAnswers.set(DutyLowerRatePage, duty))
                                    } else {
                                      Future.successful(updatedAnswers)
                                    }

                    _ <- sessionRepository.set(finalAnswers)
                  } yield Redirect(navigator.nextPage(CalculationLowerCheckPage, mode, finalAnswers))
                }
              )

          case _ =>
            Future.successful(
              Redirect(routes.JourneyRecoveryController.onPageLoad())
            )
        }

      case _ =>
        logger.info(s"[onSubmit] regime ${request.regime} is not Authorised")
        Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
    }
  }
}
