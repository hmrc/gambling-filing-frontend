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

class UnderDeclaredDutyPageSpec extends SpecBase {

  private def answersWithSubQuestions = emptyUserAnswers
    .set(UnderDeclaredDutyPage, true)
    .success
    .value
    .set(UnderDeclaredDutyReasonableCarePage, false)
    .success
    .value
    .set(UnderDeclaredDutyLimitsPage, true)
    .success
    .value
    .set(TotalUnderDeclaredDutyPage, BigDecimal(200))
    .success
    .value

  "UnderDeclaredDutyPage cleanup" - {

    "must remove all downstream answers when set to false" in {
      val result = answersWithSubQuestions.set(UnderDeclaredDutyPage, false).success.value

      result.get(UnderDeclaredDutyReasonableCarePage) mustBe None
      result.get(UnderDeclaredDutyLimitsPage) mustBe None
      result.get(TotalUnderDeclaredDutyPage) mustBe None
    }

    "must leave downstream answers untouched when set to true" in {
      val result = answersWithSubQuestions.set(UnderDeclaredDutyPage, true).success.value

      result.get(UnderDeclaredDutyReasonableCarePage) mustBe Some(false)
      result.get(UnderDeclaredDutyLimitsPage) mustBe Some(true)
      result.get(TotalUnderDeclaredDutyPage) mustBe Some(BigDecimal(200))
    }
  }
}
