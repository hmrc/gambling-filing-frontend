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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.Forms.*
import play.api.data.{Form, FormError}
import play.api.i18n.{Messages, MessagesImpl}
import play.api.test.Helpers.stubMessagesApi

class ViewUtilsSpec extends AnyWordSpec with Matchers {

  private val form: Form[String] =
    Form("value" -> text)

  private val messagesApi = stubMessagesApi(
    Map(
      "en" -> Map(
        "error.title.prefix" -> "Error:",
        "test.title"         -> "Test Page",
        "section.name"       -> "Section",
        "service.name"       -> "Gambling Filing",
        "site.govuk"         -> "GOV.UK"
      )
    )
  )

  implicit val messages: Messages =
    MessagesImpl(play.api.i18n.Lang.defaultLang, messagesApi)

  "ViewUtils.errorPrefix" should {

    "return prefix when form has errors" in {
      val errorForm =
        form.withError(FormError("value", "error"))

      ViewUtils.errorPrefix(errorForm) mustBe "Error:"
    }

    "return empty string when form has no errors" in {
      ViewUtils.errorPrefix(form) mustBe ""
    }
  }

  "ViewUtils.titleNoForm" should {

    "build full title without section" in {
      val result =
        ViewUtils.titleNoForm("test.title")

      result mustBe "Test Page - Gambling Filing - GOV.UK"
    }

    "build full title with section" in {
      val result =
        ViewUtils.titleNoForm("test.title", Some("section.name"))

      result mustBe "Test Page - Section - Gambling Filing - GOV.UK"
    }
  }

  "ViewUtils.title" should {

    "include error prefix when form has errors" in {
      val errorForm =
        form.withError(FormError("value", "error"))

      val result =
        ViewUtils.title(errorForm, "test.title")

      result must include("Error:")
      result must include("Test Page")
    }

    "not include error prefix when form is valid" in {
      val result =
        ViewUtils.title(form, "test.title")

      result must include("Test Page")
      result must include("Gambling Filing")
    }
  }
}
