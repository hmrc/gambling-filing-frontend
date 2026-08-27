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

class NegativeDutyPageSpec extends SpecBase {

  "NegativeDutyPage cleanup" - {

    "must remove the amount brought forward answer when set to false" in {
      val answers = emptyUserAnswers
        .set(NegativeDutyPage, true)
        .success
        .value
        .set(NegativeDutyBroughtForwardInputPage, BigDecimal(123.45))
        .success
        .value

      val result = answers.set(NegativeDutyPage, false).success.value

      result.get(NegativeDutyBroughtForwardInputPage) mustBe None
    }

    "must leave the amount brought forward answer untouched when set to true" in {
      val answers = emptyUserAnswers
        .set(NegativeDutyPage, true)
        .success
        .value
        .set(NegativeDutyBroughtForwardInputPage, BigDecimal(123.45))
        .success
        .value

      val result = answers.set(NegativeDutyPage, true).success.value

      result.get(NegativeDutyBroughtForwardInputPage) mustBe Some(BigDecimal(123.45))
    }
  }
}
