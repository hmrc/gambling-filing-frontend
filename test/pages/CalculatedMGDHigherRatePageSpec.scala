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

package pages

import base.SpecBase

class CalculatedMGDHigherRatePageSpec extends SpecBase {

  private def answersWithAmount = emptyUserAnswers
    .set(CalculatedMGDHigherRatePage, true)
    .success
    .value
    .set(MgdHigherRatePage, BigDecimal(25))
    .success
    .value

  "CalculatedMGDHigherRatePage cleanup" - {

    "must remove the corrected duty amount when set to false" in {
      val result = answersWithAmount.set(CalculatedMGDHigherRatePage, false).success.value

      result.get(MgdHigherRatePage) mustBe None
    }

    "must leave the corrected duty amount untouched when set to true" in {
      val result = answersWithAmount.set(CalculatedMGDHigherRatePage, true).success.value

      result.get(MgdHigherRatePage) mustBe Some(BigDecimal(25))
    }
  }
}
