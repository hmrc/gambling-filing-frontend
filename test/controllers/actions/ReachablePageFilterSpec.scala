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

import base.SpecBase
import controllers.routes
import models.{CheckMode, NormalMode, SelectedReturn, UserAnswers}
import pages.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import java.time.LocalDate

class ReachablePageFilterSpec extends SpecBase {

  private val selectedReturn = SelectedReturn(1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  private val allNo: UserAnswers =
    Seq(NetTakingsLowerRatePage, NetTakingsStandardRatePage, NetTakingsHigherRatePage, UnderDeclaredDutyPage, NegativeDutyPage)
      .foldLeft(emptyUserAnswers.set(SelectReturnPage, selectedReturn).success.value)((ua, p) => ua.set(p, false).success.value)

  private def childChangeUrls = Seq(
    routes.NetTakingsLowerController.onPageLoad(CheckMode),
    routes.MgdLowerRateController.onPageLoad(CheckMode),
    routes.NetTakingsStandardController.onPageLoad(CheckMode),
    routes.MgdStandardRateController.onPageLoad(CheckMode),
    routes.NetTakingsHigherController.onPageLoad(CheckMode),
    routes.MgdHigherRateController.onPageLoad(CheckMode),
    routes.UnderDeclaredDutyReasonableCareController.onPageLoad(CheckMode),
    routes.UnderDeclaredDutyLimitsController.onPageLoad(CheckMode),
    routes.TotalUnderDeclaredDutyController.onPageLoad(CheckMode),
    routes.ContactHmrcController.onPageLoad(CheckMode),
    routes.NegativeDutyBroughtForwardInputController.onPageLoad(CheckMode)
  ).map(_.url)

  "ReachablePageFilter" - {

    "must bounce every unreachable child change page (GET and POST) back to CYA" in {
      val application = applicationBuilder(userAnswers = Some(allNo)).build()
      running(application) {
        childChangeUrls.foreach { url =>
          val get = route(application, FakeRequest(GET, url)).value
          withClue(s"GET $url: ") {
            status(get) mustEqual SEE_OTHER
            redirectLocation(get).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
          }
        }
        childChangeUrls.filterNot(_.contains("contact-hmrc")).foreach { url =>
          val post = route(application, FakeRequest(POST, url).withFormUrlEncodedBody("value" -> "100")).value
          withClue(s"POST $url: ") {
            status(post) mustEqual SEE_OTHER
            redirectLocation(post).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
          }
        }
      }
    }

    "must allow a reachable child change page" in {
      val ua = allNo.set(NetTakingsLowerRatePage, true).success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        status(route(application, FakeRequest(GET, routes.NetTakingsLowerController.onPageLoad(CheckMode).url)).value) mustEqual OK
      }
    }

    "must bounce the MGD due change page back to CYA when net takings has not been entered" in {
      val ua = allNo.set(NetTakingsLowerRatePage, true).success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val result = route(application, FakeRequest(GET, routes.MgdLowerRateController.onPageLoad(CheckMode).url)).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "must not guard NormalMode pages" in {
      val application = applicationBuilder(userAnswers = Some(allNo)).build()
      running(application) {
        status(route(application, FakeRequest(GET, routes.NetTakingsLowerController.onPageLoad(NormalMode).url)).value) mustEqual OK
      }
    }
  }
}
