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
import forms.TotalUnderDeclaredDutyFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{NetTakingsHigherPage, NetTakingsLowerPage, NetTakingsStandardPage, SelectReturnPage, TotalUnderDeclaredDutyPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TotalUnderDeclaredDutyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalUnderDeclaredDutyController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  formProvider: TotalUnderDeclaredDutyFormProvider,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: TotalUnderDeclaredDutyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData) { implicit request =>
    request.userAnswers
      .flatMap(_.get(SelectReturnPage))
      .fold(Redirect(controllers.routes.SelectReturnController.onPageLoad())) { selectedReturn =>

        val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
        val maximumAllowed = calculateMaximumAllowed(userAnswers)
        val form = formProvider(maximumAllowed)

        val preparedForm =
          userAnswers.get(TotalUnderDeclaredDutyPage).fold(form)(form.fill)

        Ok(
          view(preparedForm, mode, selectedReturn)
        )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>
    request.userAnswers
      .flatMap(_.get(SelectReturnPage))
      .fold(Future.successful(Redirect(controllers.routes.SelectReturnController.onPageLoad()))) { selectedReturn =>

        val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
        val maximumAllowed = calculateMaximumAllowed(userAnswers)

        formProvider(maximumAllowed)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, mode, selectedReturn))
              ),
            value =>
              for {
                updatedAnswers <- Future.fromTry(userAnswers.set(TotalUnderDeclaredDutyPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(TotalUnderDeclaredDutyPage, mode, updatedAnswers))
          )
      }
  }

  private def calculateMaximumAllowed(userAnswers: UserAnswers): BigDecimal = {
    val lowerNetTakings =
      userAnswers.get(NetTakingsLowerPage).getOrElse(BigDecimal(0))

    val standardNetTakings =
      userAnswers.get(NetTakingsStandardPage).getOrElse(BigDecimal(0))

    val higherNetTakings =
      userAnswers.get(NetTakingsHigherPage).getOrElse(BigDecimal(0))

    val totalNetTakings =
      lowerNetTakings + standardNetTakings + higherNetTakings

    val percentageOfTotalNetTakings =
      totalNetTakings * appConfig.underDeclaredDutyPercentage

    appConfig.underDeclaredDutyMinimumLimit.max(
      percentageOfTotalNetTakings.min(
        appConfig.underDeclaredDutyMaximumLimit
      )
    )
  }

}
