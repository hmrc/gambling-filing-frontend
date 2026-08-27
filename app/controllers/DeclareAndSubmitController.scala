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
import models.{DeclaredSubmission, Mode, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.*
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.DeclareAndSubmitView

import javax.inject.Inject
import scala.concurrent.Future

class DeclareAndSubmitController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  validate: ValidateAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareAndSubmitView
) extends BaseFilingController {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    request.userAnswers.flatMap(_.get(SelectReturnPage)) match {
      case None =>
        logger.info(s"[onPageLoad] no selectedReturn found for regNum=${request.regNum}")
        Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))
      case Some(selectedReturn) =>
        request.userAnswers.flatMap { ua =>
          val mgdLowerRate = ua.get(MgdLowerRatePage).getOrElse(BigDecimal(0.00))
          val mgdStandardRate = ua.get(MgdStandardRatePage).getOrElse(BigDecimal(0.00))
          val mgdHigherRate = ua.get(MgdHigherRatePage).getOrElse(BigDecimal(0.00))
          val underDeclaredTaxFromPreviousPeriods = ua.get(TotalUnderDeclaredDutyPage).getOrElse(BigDecimal(0.00))
          val amountBroughtForward = ua.get(NegativeDutyBroughtForwardInputPage).getOrElse(BigDecimal(0.00))

          Some(
            DeclaredSubmission(
              mgdLowerRate + mgdStandardRate + mgdHigherRate,
              underDeclaredTaxFromPreviousPeriods,
              amountBroughtForward
            )
          )
        } match {
          case Some(declaredSubmission) =>
            Future.successful(Ok(view(mode, backNavigator.backPage(DeclareAndSubmitPage, mode, request), selectedReturn, declaredSubmission)))
          case _ =>
            logger.info(s"[onPageLoad] Unable to calculate DeclaredSubmission for regNum=${request.regNum}")
            Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))
        }
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    request.userAnswers.flatMap(_.get(SelectReturnPage)) match {
      case None =>
        logger.info(s"[onSubmit] no selectedReturn found for regNum=${request.regNum}")
        Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))
      case Some(selectedReturn) =>
        // TODO:  should submit the form to the iForms and once we get a successful response we redirect to /confirmation page
        val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
        Future.successful(Redirect(navigator.nextPage(DeclareAndSubmitPage, mode, userAnswers)))
    }
  }
}
