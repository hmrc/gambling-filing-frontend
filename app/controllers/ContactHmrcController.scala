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
import models.Regime
import navigation.BackNavigator
import pages.{ContactHmrcPage, SelectReturnPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ContactHmrcView

import javax.inject.Inject
import scala.concurrent.Future

class ContactHmrcController @Inject() (
                                        override val messagesApi: MessagesApi,
                                        backNavigator: BackNavigator,
                                        authorise: AuthorisedAction,
                                        getData: DataRetrievalAction,
                                        val controllerComponents: MessagesControllerComponents,
                                        appConfig: FrontendAppConfig,
                                        view: ContactHmrcView
                                      )
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen getData).async { implicit request =>

      request.regime match {

        case Regime.MGD =>
          request.userAnswers
            .flatMap(_.get(SelectReturnPage))
            .fold(
              Future.successful(
                Redirect(
                  controllers.routes.SelectReturnController.onPageLoad()
                )
              )
            ) { selectedReturn =>
              Future.successful(
                Ok(
                  view(
                    backLink = backNavigator.backPage(
                      ContactHmrcPage,
                      request
                    ),
                    selectedReturn = selectedReturn,
                    contactHmrcUrl = appConfig.contactHmrcUrl
                  )
                )
              )
            }

        case _ =>
          logger.info(
            s"[onPageLoad] regime ${request.regime} is not authorised"
          )

          Future.successful(
            Redirect(
              controllers.routes.AccessDeniedController.onPageLoad()
            )
          )
      }
    }
}