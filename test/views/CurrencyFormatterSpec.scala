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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class CurrencyFormatterSpec extends AnyFreeSpec with Matchers {

  import CurrencyFormatter.*

  "currencyFormat" - {

    "must drop .00 for whole amounts" in {
      currencyFormat(BigDecimal("12000")) mustEqual "£12,000"
    }

    "must keep pence and thousands separators" in {
      currencyFormat(BigDecimal("1234567.89")) mustEqual "£1,234,567.89"
    }

    "must show the absolute value" in {
      currencyFormat(BigDecimal("-50.5")) mustEqual "£50.50"
    }

    "must round ties to the even penny (HALF_EVEN)" in {
      currencyFormat(BigDecimal("12000.005")) mustEqual "£12,000"      // HALF_UP would give £12,000.01
      currencyFormat(BigDecimal("12000.015")) mustEqual "£12,000.02"
      currencyFormat(BigDecimal("0.025")) mustEqual "£0.02"
    }

    "must round non-ties to the nearest penny" in {
      currencyFormat(BigDecimal("12345.6789")) mustEqual "£12,345.68"
      currencyFormat(BigDecimal("12345.6712")) mustEqual "£12,345.67"
    }
  }

  "formattedAmountHtml" - {

    "must prefix negatives with a minus sign" in {
      formattedAmountHtml(BigDecimal("-5")) must include("&#8722;£5")
    }

    "must not add a minus sign for positives" in {
      formattedAmountHtml(BigDecimal("5")) must not include "&#8722;"
    }
  }
}
