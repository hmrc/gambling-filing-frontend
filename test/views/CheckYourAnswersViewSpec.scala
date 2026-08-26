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

package views

import base.SpecBase
import models.SelectedReturn
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.test.FakeRequest
import utils.DateTimeFormats
import viewmodels.checkAnswers.CheckYourAnswersHelpers
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

class CheckYourAnswersViewSpec extends SpecBase with SummaryListFluency {

  private val selectedReturn = SelectedReturn(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  private def rowFor(doc: Document, keyText: String): Element =
    doc
      .select(".govuk-summary-list__row")
      .asScala
      .find(_.select(".govuk-summary-list__key").text() == keyText)
      .getOrElse(fail(s"could not find a row with key '$keyText'"))

  "CheckYourAnswersView" - {

    "must render the page title, caption and heading" in {
      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.CheckYourAnswersView]
      val request = FakeRequest()
      implicit val msgs = messages(app)

      val emptyList = SummaryListViewModel(Seq.empty)

      val doc =
        Jsoup.parse(view(selectedReturn, emptyList, emptyList, emptyList, emptyList, emptyList, emptyList)(request, msgs).body)

      doc.title() must include(msgs("checkYourAnswers.title"))

      val expectedCaption = msgs(
        "checkYourAnswers.caption",
        DateTimeFormats.formatDateMMM(Some(selectedReturn.periodStart)),
        DateTimeFormats.formatDateMMM(Some(selectedReturn.periodEnd))
      )
      doc.select(".govuk-caption-l").text() mustEqual expectedCaption

      doc.select("h1").text() mustEqual msgs("checkYourAnswers.heading")
    }

    "must render each section heading" in {
      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.CheckYourAnswersView]
      val request = FakeRequest()
      implicit val msgs = messages(app)

      val emptyList = SummaryListViewModel(Seq.empty)

      val doc =
        Jsoup.parse(view(selectedReturn, emptyList, emptyList, emptyList, emptyList, emptyList, emptyList)(request, msgs).body)

      val h2Texts = doc.select("h2").eachText().asScala

      h2Texts must contain(msgs("checkYourAnswers.lowerRate.heading"))
      h2Texts must contain(msgs("checkYourAnswers.standardRate.heading"))
      h2Texts must contain(msgs("checkYourAnswers.higherRate.heading"))
      h2Texts must contain(msgs("checkYourAnswers.underDeclaredDuty.heading"))
      h2Texts must contain(msgs("checkYourAnswers.dutyBroughtForward.heading"))
    }

    "must render the content of each summary list" in {
      val app = applicationBuilder().build()
      val view = app.injector.instanceOf[views.html.CheckYourAnswersView]
      val request = FakeRequest()
      implicit val msgs = messages(app)

      val machines = SummaryListViewModel(
        Seq(
          CheckYourAnswersHelpers.textRow(
            keyMsg    = "submittedReturn.noOfMachines",
            answer    = "10",
            changeUrl = Some("/change-machines-available"),
            hiddenMsg = Some("submittedReturn.noOfMachines")
          )
        )
      )

      val lowerRate = SummaryListViewModel(
        Seq(
          CheckYourAnswersHelpers.yesNoRow(
            keyMsg    = "netTakingsLowerRate.question",
            answer    = true,
            changeUrl = "/change-net-takings-liable",
            hiddenMsg = "netTakingsLowerRate.question"
          ),
          CheckYourAnswersHelpers.currencyRow(
            keyMsg    = "submittedReturn.netTakingsLowerRate",
            amount    = BigDecimal(1000),
            changeUrl = Some("/change-net-takings"),
            hiddenMsg = Some("submittedReturn.netTakingsLowerRate")
          ),
          CheckYourAnswersHelpers.currencyRow(keyMsg = "submittedReturn.totalDueLowerRate", amount = BigDecimal(50))
        )
      )

      val standardRate = SummaryListViewModel(
        Seq(CheckYourAnswersHelpers.currencyRow(keyMsg = "submittedReturn.totalDueStdRate", amount = BigDecimal(600)))
      )

      val higherRate = SummaryListViewModel(
        Seq(CheckYourAnswersHelpers.currencyRow(keyMsg = "submittedReturn.totalDueHigherRate", amount = BigDecimal(1500)))
      )

      val underDeclaredDuty = SummaryListViewModel(
        Seq(
          CheckYourAnswersHelpers.currencyRow(
            keyMsg    = "submittedReturn.underDeclaredDuty",
            amount    = BigDecimal(1.99),
            changeUrl = Some("/change-under-declared-duty"),
            hiddenMsg = Some("submittedReturn.underDeclaredDuty")
          )
        )
      )

      val dutyBroughtForward = SummaryListViewModel(
        Seq(
          CheckYourAnswersHelpers.currencyRow(
            keyMsg    = "submittedReturn.previousReturnAmount",
            amount    = BigDecimal(2.90),
            changeUrl = Some("/change-duty-brought-forward"),
            hiddenMsg = Some("submittedReturn.previousReturnAmount")
          )
        )
      )

      val doc = Jsoup.parse(
        view(selectedReturn, machines, lowerRate, standardRate, higherRate, underDeclaredDuty, dutyBroughtForward)(
          request,
          msgs
        ).body
      )

      def valueFor(keyText: String): String =
        rowFor(doc, keyText).select(".govuk-summary-list__value").text()

      valueFor(msgs("submittedReturn.noOfMachines")) mustEqual "10"
      valueFor(msgs("netTakingsLowerRate.question")) mustEqual "Yes"
      valueFor(msgs("submittedReturn.netTakingsLowerRate")) mustEqual "£1,000"
      valueFor(msgs("submittedReturn.totalDueLowerRate")) mustEqual "£50"
      valueFor(msgs("submittedReturn.totalDueStdRate")) mustEqual "£600"
      valueFor(msgs("submittedReturn.totalDueHigherRate")) mustEqual "£1,500"
      valueFor(msgs("submittedReturn.underDeclaredDuty")) mustEqual "£1.99"
      valueFor(msgs("submittedReturn.previousReturnAmount")) mustEqual "£2.90"
    }
  }
}
