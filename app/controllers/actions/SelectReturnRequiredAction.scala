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

import models.requests.OptionalDataRequest
import pages.SelectReturnPage
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectReturnRequiredActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends SelectReturnRequiredAction with Logging {

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] =
    request.userAnswers.flatMap(_.get(SelectReturnPage)) match {
      case None =>
        logger.info(s"no selectedReturn found for regNum=${request.regNum}")
        Future.successful(Some(Redirect(controllers.routes.SelectReturnController.onPageLoad())))
      case Some(_) =>
        Future.successful(None)
    }
}

trait SelectReturnRequiredAction extends ActionFilter[OptionalDataRequest]
