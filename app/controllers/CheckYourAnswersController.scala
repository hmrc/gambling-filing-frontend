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

import com.google.inject.Inject
import controllers.actions.*
import models.{CheckMode, NormalMode, UserAnswers}
import scala.util.{Success, Try}
import navigation.CheckYourAnswersValidator
import pages.*
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*
import views.html.CheckYourAnswersView

import scala.concurrent.Future
import config.FrontendAppConfig
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  validate: ValidateAction,
  getData: DataRetrievalAction,
  journeyComplete: JourneyCompleteAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends BaseFilingController {

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen validate andThen getData andThen journeyComplete andThen requireData).async { implicit request =>

      val answers = request.userAnswers

      val answersToUse =
        if (recalculateUnderDeclaredDuty(answers)) {
          Future.fromTry(answers.remove(TotalUnderDeclaredDutyPage)).flatMap { updatedAnswers =>
            sessionRepository.set(updatedAnswers).map(_ => updatedAnswers)
          }
        } else {
          Future.successful(answers)
        }

      answersToUse.map { updatedAnswers =>

        val backLink =
          updatedAnswers.get(NegativeDutyPage) match {
            case Some(false) =>
              Some(routes.NegativeDutyController.onPageLoad(NormalMode).url)
            case _ =>
              Some(routes.NegativeDutyBroughtForwardInputController.onPageLoad(NormalMode).url)
          }

        updatedAnswers.get(SelectReturnPage).fold(Redirect(controllers.routes.SelectReturnController.onPageLoad())) { selectedReturn =>

          val machines = SummaryListViewModel(rows = MachinesAvailableSummary.rows(updatedAnswers))
          val lowerRate = SummaryListViewModel(rows = LowerRateSummary.rows(updatedAnswers))
          val standardRate = SummaryListViewModel(rows = StandardRateSummary.rows(updatedAnswers))
          val higherRate = SummaryListViewModel(rows = HigherRateSummary.rows(updatedAnswers))
          val underDeclaredDuty = SummaryListViewModel(rows = UnderDeclaredDutySummary.rows(updatedAnswers))
          val dutyBroughtForward = SummaryListViewModel(rows = DutyBroughtForwardSummary.rows(updatedAnswers))

          Ok(view(selectedReturn, machines, lowerRate, standardRate, higherRate, underDeclaredDuty, dutyBroughtForward, backLink))
        }
      }
    }

  def onSubmit(): Action[AnyContent] =
    (authorise andThen validate andThen getData andThen journeyComplete andThen requireData).async { implicit request =>

      val answers = request.userAnswers

      CheckYourAnswersValidator.checkForMissingAnswers(answers, CheckMode) match {
        case Some(missingAnswerPage) =>
          Future.successful(Redirect(missingAnswerPage))

        case None =>
          val result =
            answers.get(MachinesAvailablePage) match {
              case None =>
                logger.info(s"no MachinesAvailable found for regNum=${request.regNum}")
                Redirect(controllers.routes.SelectReturnController.onPageLoad())

              case Some(machinesAvailable) if machinesAvailable > 0L =>
                Redirect(controllers.routes.DeclareAndSubmitController.onPageLoad())

              case _ =>
                Redirect(
                  controllers.routes.ConfirmNumberOfMachinesInterrupterController.onPageLoad()
                )
            }

          Future.successful(result)
      }
    }

  private def recalculateUnderDeclaredDuty(answers: UserAnswers): Boolean = {

    val totalNetTakings =
      answers.get(NetTakingsLowerPage).getOrElse(BigDecimal(0)) +
        answers.get(NetTakingsStandardPage).getOrElse(BigDecimal(0)) +
        answers.get(NetTakingsHigherPage).getOrElse(BigDecimal(0))

    val percentageOfTotalNetTakings =
      (totalNetTakings * appConfig.underDeclaredDutyPercentage)
        .setScale(2, BigDecimal.RoundingMode.HALF_EVEN)

    val maximumAllowed =
      appConfig.underDeclaredDutyMinimumLimit.max(
        percentageOfTotalNetTakings.min(appConfig.underDeclaredDutyMaximumLimit)
      )

    answers
      .get(TotalUnderDeclaredDutyPage)
      .exists(_ > maximumAllowed)
  }
}
