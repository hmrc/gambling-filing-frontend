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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Lang
import utils.DateTimeFormats.{dateTimeFormat, dateTimeFormatMMM, parseMgdPeriod}

import java.time.LocalDate

class DateTimeFormatsSpec extends AnyFreeSpec with Matchers {

  ".dateTimeFormat" - {

    "must format dates in English" in {
      val formatter = dateTimeFormat()(Lang("en"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }

    "must format dates in Welsh" in {
      val formatter = dateTimeFormat()(Lang("cy"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 Ionawr 2023"
    }

    "must default to English format" in {
      val formatter = dateTimeFormat()(Lang("de"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }
  }

  ".dateTimeFormatMMM" - {

    "must format dates in English" in {
      val formatter = dateTimeFormatMMM()(Lang("en"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 Jan 2023"
    }

    "must format dates in Welsh" in {
      val formatter = dateTimeFormatMMM()(Lang("cy"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 Ion 2023"
    }

    "must default to English format" in {
      val formatter = dateTimeFormatMMM()(Lang("de"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 Jan 2023"
    }
  }

  ".parseMgdPeriod" - {

    "must parse a valid period and return the correct start and end dates" in {
      val result = parseMgdPeriod("01/03/2025 - 30/06/2025")
      result mustBe Some((LocalDate.of(2025, 3, 1), LocalDate.of(2025, 6, 30)))
    }

    "must return None when the separator is missing" in {
      val result = parseMgdPeriod("01/03/2025 30/06/2025")
      result mustBe None
    }

    "must return None when the start date is invalid" in {
      val result = parseMgdPeriod("99/03/2025 - 30/06/2025")
      result mustBe None
    }

    "must return None when the end date is invalid" in {
      val result = parseMgdPeriod("01/03/2025 - 99/06/2025")
      result mustBe None
    }

    "must return None for an empty string" in {
      val result = parseMgdPeriod("")
      result mustBe None
    }

    "must return None when the date format is wrong" in {
      val result = parseMgdPeriod("2025-03-01 - 2025-06-30")
      result mustBe None
    }
  }
}
