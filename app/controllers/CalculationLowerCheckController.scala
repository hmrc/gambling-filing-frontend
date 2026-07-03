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
import navigation.Navigator
import pages.{CalculationLowerCheckPage, DutyLowerPage, NetTakingsLowerPage}
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

        val netTakings = Some(BigDecimal(10000)) match {
          case Some(value) => value
//          case None        => throw new Exception("NetTakingsLowerPage not found")
        }

        val duty = netTakings * BigDecimal(frontendAppConfig.lowerRateDutyPercentage)

        Future.successful(Ok(view(radioAnswer, netTakings, duty, mode)))
      case _ =>
        logger.info(s"[onPageLoad] regime ${request.regime} is not Authorised")
        Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.regime match {
      case Regime.MGD =>
        val netTakings = Some(BigDecimal(10000)) match {
          case Some(value) => value
//            case None        => throw new Exception("NetTakingsLowerPage not found")
        }

        val duty = netTakings * BigDecimal(frontendAppConfig.lowerRateDutyPercentage)

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, netTakings, duty, mode))),
            value => {
              val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
              for {
                answersRadioCheck <- Future.fromTry(userAnswers.set(CalculationLowerCheckPage, value))

                finalAnswers <- if (value) { Future.fromTry(answersRadioCheck.set(DutyLowerPage, duty)) }
                                else { Future.successful(answersRadioCheck) }

                _ <- sessionRepository.set(finalAnswers)
              } yield Redirect(navigator.nextPage(CalculationLowerCheckPage, mode, finalAnswers))
            }
          )
      case _ =>
        logger.info(s"[onSubmit] regime ${request.regime} is not Authorised")
        Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
    }
  }
}
