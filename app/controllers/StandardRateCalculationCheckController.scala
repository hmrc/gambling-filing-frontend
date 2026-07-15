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
import forms.StandardRateCalculationCheckFormProvider
import models.{Mode, Regime, SelectedReturn, UserAnswers}
import navigation.Navigator
import pages.{MgdStandardRatePage, NetTakingsStandardPage, SelectReturnPage, StandardRateCalculationCheckPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.StandardRateCalculationCheckView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StandardRateCalculationCheckController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  formProvider: StandardRateCalculationCheckFormProvider,
  frontendAppConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: StandardRateCalculationCheckView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()
  private val ratePercentage = frontendAppConfig.standardRateDutyPercentage * 100

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.regime match {
      case Regime.MGD =>
        val radioAnswer = request.userAnswers.flatMap(_.get(StandardRateCalculationCheckPage)).fold(form)(form.fill)

        request.userAnswers
          .flatMap(_.get(SelectReturnPage))
          .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
            request.userAnswers
              .flatMap(_.get(NetTakingsStandardPage))
              .fold(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))) { netTakings =>
                val duty = netTakings * frontendAppConfig.standardRateDutyPercentage
                Future.successful(Ok(view(radioAnswer, netTakings, duty, ratePercentage, mode, selectedReturn)))
              }
          }
      case _ =>
        logger.info(s"[onPageLoad] regime ${request.regime} is not Authorised")
        Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.regime match {
      case Regime.MGD =>
        request.userAnswers
          .flatMap(_.get(SelectReturnPage))
          .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
            request.userAnswers
              .flatMap(_.get(NetTakingsStandardPage))
              .fold(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))) { netTakings =>
                val duty = netTakings * frontendAppConfig.standardRateDutyPercentage

                form
                  .bindFromRequest()
                  .fold(
                    formWithErrors => Future.successful(BadRequest(view(formWithErrors, netTakings, duty, ratePercentage, mode, selectedReturn))),
                    value => {
                      val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
                      for {
                        updatedAnswers <- Future.fromTry(userAnswers.set(StandardRateCalculationCheckPage, value))
                        finalAnswers <-
                          if (value) Future.fromTry(updatedAnswers.set(MgdStandardRatePage, duty))
                          else Future.successful(updatedAnswers)
                        _ <- sessionRepository.set(finalAnswers)
                      } yield Redirect(navigator.nextPage(StandardRateCalculationCheckPage, mode, finalAnswers))
                    }
                  )
              }
          }
      case _ =>
        logger.info(s"[onSubmit] regime ${request.regime} is not Authorised")
        Future.successful(Redirect(controllers.routes.AccessDeniedController.onPageLoad()))
    }
  }
}
