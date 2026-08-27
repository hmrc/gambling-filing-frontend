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

class NetTakingsHigherRatePageSpec extends SpecBase {

  private def answersWithSubQuestions = emptyUserAnswers
    .set(NetTakingsHigherRatePage, true)
    .success
    .value
    .set(NetTakingsHigherPage, BigDecimal(100))
    .success
    .value
    .set(CalculatedMGDHigherRatePage, true)
    .success
    .value
    .set(MgdHigherRatePage, BigDecimal(25))
    .success
    .value

  "NetTakingsHigherRatePage cleanup" - {

    "must remove all downstream answers when set to false" in {
      val result = answersWithSubQuestions.set(NetTakingsHigherRatePage, false).success.value

      result.get(NetTakingsHigherPage) mustBe None
      result.get(CalculatedMGDHigherRatePage) mustBe None
      result.get(MgdHigherRatePage) mustBe None
    }

    "must leave downstream answers untouched when set to true" in {
      val result = answersWithSubQuestions.set(NetTakingsHigherRatePage, true).success.value

      result.get(NetTakingsHigherPage) mustBe Some(BigDecimal(100))
      result.get(CalculatedMGDHigherRatePage) mustBe Some(true)
      result.get(MgdHigherRatePage) mustBe Some(BigDecimal(25))
    }
  }
}
