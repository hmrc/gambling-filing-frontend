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
import forms.NetTakingsStandardFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.data.Form
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.NetTakingsStandardView

class NetTakingsStandardViewSpec extends SpecBase {

  "NetTakingsStandardView" - {

    "must render the page with correct heading, caption, input and continue button" in {
      
      val app: Application = applicationBuilder().build()
      val view: NetTakingsStandardView = app.injector.instanceOf[NetTakingsStandardView]
      
      val form: Form[BigDecimal] = new NetTakingsStandardFormProvider()()
      
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      implicit val messages: Messages =
        app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)
      
      val html: HtmlFormat.Appendable = view(form, NormalMode)
      val doc: Document = Jsoup.parse(html.body)
      
      doc.title() must include(messages("netTakingsStandard.title"))
      
      doc.select("h1").text must include(messages("netTakingsStandard.heading"))
      
      doc.select(".govuk-caption-l").text must include("1 Jan 2014")
      doc.select(".govuk-caption-l").text must include("31 Dec 2014")
      
      doc.select(".govuk-input__prefix").text must include("£")
      
      doc.select("button").text must include(messages("site.continue"))
    }
  }
}