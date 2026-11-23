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

import controllers.routes
import models.{CheckMode, Mode}
import models.requests.OptionalDataRequest
import navigation.CheckYourAnswersValidator
import pages.Page
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReachablePageFilter @Inject() (implicit ec: ExecutionContext) {

  def apply(page: Page, mode: Mode): ActionFilter[OptionalDataRequest] =
    new ActionFilter[OptionalDataRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] =
        Future.successful(
          Option.when(mode == CheckMode && request.userAnswers.exists(ua => !CheckYourAnswersValidator.isReachable(page)(ua)))(
            Redirect(routes.CheckYourAnswersController.onPageLoad())
          )
        )
    }
}
