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
import forms.NetTakingsLowerRateYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.NetTakingsLowerRateYesNoView

class NetTakingsLowerRateYesNoViewSpec extends SpecBase {

  "NetTakingsLowerRateYesNoView" - {

    "must render the page with the correct content" in new Setup {

      val html = view(form, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("netTakingsLowerRateYesNo.title"))
      doc.select("h1").text mustBe messages("netTakingsLowerRateYesNo.heading")
      doc.select(".govuk-caption-l").text mustBe
        messages("netTakingsLowerRateYesNo.caption", "1 Jan 2014", "31 Dec 2014")

      doc.select("p.govuk-body").text mustBe
        messages("netTakingsLowerRateYesNo.p1")

      val bullets = doc.select("ul.govuk-list--bullet li")
      bullets.get(0).text mustBe messages("netTakingsLowerRateYesNo.bullet1")
      bullets.get(1).text mustBe messages("netTakingsLowerRateYesNo.bullet2")

      doc.select("legend").text must include(messages("netTakingsLowerRateYesNo.question"))

      doc.select("input[value=true]").isEmpty mustBe false
      doc.select("input[value=false]").isEmpty mustBe false

      doc.select("button").text mustBe messages("site.continue")
    }

    "must render an error summary when the form has errors" in new Setup {

      val boundForm = form.bind(Map("value" -> ""))
      val html = view(boundForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").isEmpty mustBe false
      doc.select(".govuk-error-summary__list a").text must include(
        messages("netTakingsLowerRateYesNo.error.required")
      )
    }

    "must select Yes when the form value is true" in new Setup {

      val boundForm = form.fill(true)
      val html = view(boundForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select("input[value=true]").first().hasAttr("checked") mustBe true
    }

    "must select No when the form value is false" in new Setup {

      val boundForm = form.fill(false)
      val html = view(boundForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select("input[value=false]").first().hasAttr("checked") mustBe true
    }
  }

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[NetTakingsLowerRateYesNoView]
    val form = new NetTakingsLowerRateYesNoFormProvider()()

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}