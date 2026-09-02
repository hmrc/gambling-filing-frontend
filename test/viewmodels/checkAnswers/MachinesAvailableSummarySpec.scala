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

package viewmodels.checkAnswers

import base.SpecBase
import controllers.routes
import models.CheckMode
import pages.MachinesAvailablePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

class MachinesAvailableSummarySpec extends SpecBase {

  private def machinesUrl = routes.MachinesAvailableController.onPageLoad(CheckMode).url

  "MachinesAvailableSummary" - {

    "must show a 'Set value' link with no change action when unanswered" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val rows = MachinesAvailableSummary.rows(emptyUserAnswers)
      val row = rows.find(_.key.content == Text(msgs("submittedReturn.noOfMachines"))).value

      row.value.content mustBe HtmlContent(s"""<a class="govuk-link" href="$machinesUrl">${msgs("checkYourAnswers.enterNumber")}</a>""")
      row.actions mustBe None
    }

    "must show the normal value with a change action when answered" in {
      implicit val msgs: Messages = messages(applicationBuilder().build())

      val answers = emptyUserAnswers.set(MachinesAvailablePage, 10).success.value

      val rows = MachinesAvailableSummary.rows(answers)
      val row = rows.find(_.key.content == Text(msgs("submittedReturn.noOfMachines"))).value

      row.value.content mustBe Text("10")
      row.actions.value.items.head.content mustBe Text(msgs("site.change"))
      row.actions.value.items.head.href mustBe machinesUrl
    }
  }
}
