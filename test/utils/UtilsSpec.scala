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

package utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

class UtilsSpec extends AnyWordSpec with Matchers {

  "Utils.removeSpaces" should {

    "remove leading, trailing and inner spaces" in {

      Utils.removeSpaces("  1 000   000 ") mustBe "1000000"
    }

    "remove tabs and newlines" in {

      Utils.removeSpaces("1\t000\n") mustBe "1000"
    }

    "remove non-breaking and other Unicode spaces" in {

      Utils.removeSpaces("\u00A01\u00A0000\u2007000\u00A0") mustBe "1000000"
    }

    "return an empty string for whitespace-only input" in {

      Utils.removeSpaces(" \t\u00A0 ") mustBe ""
    }
  }

  "Utils.withIds" should {

    "add sequential ids with default prefix" in {

      val items = Seq(
        RadioItem(content = Text("a"), value = Some("a")),
        RadioItem(content = Text("b"), value = Some("b")),
        RadioItem(content = Text("c"), value = Some("c"))
      )

      val result = Utils.withIds(items)

      result.map(_.id) mustBe Seq(
        Some("value_0"),
        Some("value_1"),
        Some("value_2")
      )
    }

    "add sequential ids with custom prefix" in {

      val items = Seq(
        RadioItem(content = Text("x"), value = Some("x")),
        RadioItem(content = Text("y"), value = Some("y"))
      )

      val result = Utils.withIds(items, prefix = "option")

      result.map(_.id) mustBe Seq(
        Some("option_0"),
        Some("option_1")
      )
    }

    "not modify original items" in {

      val original = Seq(
        RadioItem(content = Text("a"), value = Some("a")),
        RadioItem(content = Text("b"), value = Some("b"))
      )

      val copy = original.map(_.copy())

      Utils.withIds(original)

      original mustBe copy
    }
  }
}
