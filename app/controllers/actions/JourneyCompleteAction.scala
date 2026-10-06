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

import models.Regime
import models.requests.AuthorisedRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import java.util.regex.Pattern
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyCompleteActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends JourneyCompleteAction with Logging {

  override protected def filter[A](request: AuthorisedRequest[A]): Future[Option[Result]] = {
    if (GRNValidator.validateRegNoRegime(request.regime, request.regNum)) {
      Future.successful(None)
    } else {
      Future.successful(Some(Redirect(controllers.routes.AccessDeniedController.onPageLoad())))
    }
  }
}

trait JourneyCompleteAction extends ActionFilter[AuthorisedRequest]
