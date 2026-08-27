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

class NetTakingsStandardRatePageSpec extends SpecBase {

  private def answersWithSubQuestions = emptyUserAnswers
    .set(NetTakingsStandardRatePage, true)
    .success
    .value
    .set(NetTakingsStandardPage, BigDecimal(100))
    .success
    .value
    .set(CalculatedMGDStandardRatePage, true)
    .success
    .value
    .set(MgdStandardRatePage, BigDecimal(20))
    .success
    .value

  "NetTakingsStandardRatePage cleanup" - {

    "must remove all downstream answers when set to false" in {
      val result = answersWithSubQuestions.set(NetTakingsStandardRatePage, false).success.value

      result.get(NetTakingsStandardPage) mustBe None
      result.get(CalculatedMGDStandardRatePage) mustBe None
      result.get(MgdStandardRatePage) mustBe None
    }

    "must leave downstream answers untouched when set to true" in {
      val result = answersWithSubQuestions.set(NetTakingsStandardRatePage, true).success.value

      result.get(NetTakingsStandardPage) mustBe Some(BigDecimal(100))
      result.get(CalculatedMGDStandardRatePage) mustBe Some(true)
      result.get(MgdStandardRatePage) mustBe Some(BigDecimal(20))
    }
  }
}
