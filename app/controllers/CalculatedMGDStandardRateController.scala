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
import forms.CalculatedMGDStandardRateFormProvider
import models.{Mode, Regime, SelectedReturn, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.{CalculatedMGDStandardRatePage, MgdStandardRatePage, NetTakingsStandardPage, SelectReturnPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CalculatedMGDStandardRateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatedMGDStandardRateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  formProvider: CalculatedMGDStandardRateFormProvider,
  frontendAppConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: CalculatedMGDStandardRateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()
  private val ratePercentage = frontendAppConfig.standardRateDutyPercentage * 100

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.regime match {
      case Regime.MGD =>
        val radioAnswer = request.userAnswers.flatMap(_.get(CalculatedMGDStandardRatePage)).fold(form)(form.fill)

        request.userAnswers
          .flatMap(_.get(SelectReturnPage))
          .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>
            request.userAnswers
              .flatMap(_.get(NetTakingsStandardPage))
              .fold(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))) { netTakings =>
                val duty = netTakings * frontendAppConfig.standardRateDutyPercentage
                Future.successful(
                  Ok(
                    view(radioAnswer,
                         netTakings,
                         duty,
                         ratePercentage,
                         mode,
                         selectedReturn,
                         backNavigator.backPage(CalculatedMGDStandardRatePage, mode, request)
                        )
                  )
                )
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
                    formWithErrors =>
                      Future.successful(
                        BadRequest(
                          view(formWithErrors,
                               netTakings,
                               duty,
                               ratePercentage,
                               mode,
                               selectedReturn,
                               backNavigator.backPage(CalculatedMGDStandardRatePage, mode, request)
                              )
                        )
                      ),
                    value => {
                      val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
                      for {
                        updatedAnswers <- Future.fromTry(userAnswers.set(CalculatedMGDStandardRatePage, value))
                        finalAnswers <-
                          if (value) Future.fromTry(updatedAnswers.set(MgdStandardRatePage, duty))
                          else Future.successful(updatedAnswers)
                        _ <- sessionRepository.set(finalAnswers)
                      } yield Redirect(navigator.nextPage(CalculatedMGDStandardRatePage, mode, finalAnswers))
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
