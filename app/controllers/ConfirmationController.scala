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
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.SelectReturnPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.ConfirmationView

import javax.inject.Inject
import scala.concurrent.Future

class ConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmationView
) extends BaseFilingController {

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>
      request.userAnswers
        .flatMap(_.get(SelectReturnPage))
        .fold(
          Future.successful(
            Redirect(
              controllers.routes.SelectReturnController.onPageLoad()
            )
          )
        ) { selectedReturn =>
          val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.regNum))
          val acknowledgementReference = "4JTF BAXM GJXS TKM"
          val submissionDateTime = "6 June 2015 at 11:10 am"

          Future.successful(
            Ok(
              view(
                acknowledgementReference = acknowledgementReference,
                submissionDateTime       = submissionDateTime,
                periodStartDate          = selectedReturn.periodStart,
                periodEndDate            = selectedReturn.periodEnd
              )
            )
          )
        }
    }
}
