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

class NetTakingsLowerRatePageSpec extends SpecBase {

  private def answersWithSubQuestions = emptyUserAnswers
    .set(NetTakingsLowerRatePage, true)
    .success
    .value
    .set(NetTakingsLowerPage, BigDecimal(100))
    .success
    .value
    .set(CalculatedMGDLowerRatePage, true)
    .success
    .value
    .set(MgdLowerRatePage, BigDecimal(5))
    .success
    .value

  "NetTakingsLowerRatePage cleanup" - {

    "must remove all downstream answers when set to false" in {
      val result = answersWithSubQuestions.set(NetTakingsLowerRatePage, false).success.value

      result.get(NetTakingsLowerPage) mustBe None
      result.get(CalculatedMGDLowerRatePage) mustBe None
      result.get(MgdLowerRatePage) mustBe None
    }

    "must leave downstream answers untouched when set to true" in {
      val result = answersWithSubQuestions.set(NetTakingsLowerRatePage, true).success.value

      result.get(NetTakingsLowerPage) mustBe Some(BigDecimal(100))
      result.get(CalculatedMGDLowerRatePage) mustBe Some(true)
      result.get(MgdLowerRatePage) mustBe Some(BigDecimal(5))
    }
  }
}
