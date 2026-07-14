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

package models

import base.SpecBase
import pages.{MachinesAvailablePage, SelectReturnPage}

import java.time.LocalDate

class UserAnswersSpec extends SpecBase {

  private val periodA = SelectedReturn(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 9, 30))
  private val periodB = SelectedReturn(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30))

  "selectPeriod" - {

    "must set the selected period when none was previously selected" in {
      val result = emptyUserAnswers.selectPeriod(periodA).success.value

      result.get(SelectReturnPage).value mustEqual periodA
    }

    "must preserve existing answers when re-selecting the same period" in {
      val answers = emptyUserAnswers
        .selectPeriod(periodA)
        .flatMap(_.set(MachinesAvailablePage, 12L))
        .success
        .value

      val result = answers.selectPeriod(periodA).success.value

      result.get(SelectReturnPage).value mustEqual periodA
      result.get(MachinesAvailablePage).value mustEqual 12L
    }

    "must clear existing answers when selecting a different period" in {
      val answers = emptyUserAnswers
        .selectPeriod(periodA)
        .flatMap(_.set(MachinesAvailablePage, 12L))
        .success
        .value

      val result = answers.selectPeriod(periodB).success.value

      result.get(SelectReturnPage).value mustEqual periodB
      result.get(MachinesAvailablePage) mustBe None
    }
  }
}
