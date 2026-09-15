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
import models.{DeclaredSubmission, NormalMode, SubmitReturnRequest, UserAnswers}
import navigation.{BackNavigator, Navigator}
import pages.*
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.GamblingService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.DeclareAndSubmitView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclareAndSubmitController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  backNavigator: BackNavigator,
  authorise: AuthorisedAction,
  validate: ValidateAction,
  getData: DataRetrievalAction,
  sessionRepository: SessionRepository,
  gamblingService: GamblingService,
  val controllerComponents: MessagesControllerComponents,
  view: DeclareAndSubmitView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  def onPageLoad(): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    request.userAnswers.flatMap(_.get(SelectReturnPage)) match {
      case None =>
        logger.info(s"[onPageLoad] no selectedReturn found for regNum=${request.regNum}")
        Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))
      case Some(selectedReturn) =>
        request.userAnswers.map(DeclaredSubmission.from) match {
          case Some(declaredSubmission) =>
            Future.successful(Ok(view(backNavigator.backPage(DeclareAndSubmitPage, NormalMode, request), selectedReturn, declaredSubmission)))
          case _ =>
            logger.info(s"[onPageLoad] Unable to calculate DeclaredSubmission for regNum=${request.regNum}")
            Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))
        }
    }
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen validate andThen getData).async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
    val language = if (messagesApi.preferred(request).lang.code == "cy") "CYM" else "ENG"

    hc.sessionId
      .map(_.value)
      .flatMap(SubmitReturnRequest.from(request.regNum, _, language, userAnswers))
      .map(submitReturnRequest =>
        gamblingService
          .submitReturn(request.regNum, submitReturnRequest)
          .flatMap { submissionResult =>
            Future
              .fromTry(
                userAnswers.set(SubmissionResultPage, submissionResult)
              )
              .flatMap(sessionRepository.set)
              .map(_ => Redirect(navigator.nextPage(DeclareAndSubmitPage, NormalMode, userAnswers)))
          }
          .recover { case ex =>
            logger.error(s"Failed to submit mgd return for regNum=${request.regNum}", ex)
            Redirect(controllers.routes.SystemErrorController.onPageLoad())
          }
      )
      .getOrElse {
        logger.info(s"Unable to build SubmitReturnRequest for regNum=${request.regNum}")
        Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))
      }
  }
}
