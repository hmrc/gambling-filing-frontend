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

package controllers.actions

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultAuthenticatedAction])
trait AuthenticatedAction extends ActionBuilder[Request, AnyContent] with ActionFunction[Request, Request]

@Singleton
class DefaultAuthenticatedAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends AuthenticatedAction
    with AuthorisedFunctions
    with Logging {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised() {
      block(request)
    }.recover { case ex: AuthorisationException =>
      logger.info(s"Unauthenticated access to ${request.path}: ${ex.getMessage}")
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
    }
  }
}
