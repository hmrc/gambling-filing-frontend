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

import org.scalatest.{OptionValues, TryValues}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.*

import java.time.LocalDate

class SubmitReturnRequestSpec extends AnyWordSpec with Matchers with OptionValues with TryValues {

  private val selectedReturn = SelectedReturn(12345, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31))

  private def userAnswersWithData: UserAnswers = UserAnswers("regNum")
    .set(SelectReturnPage, selectedReturn)
    .success
    .value
    .set(MachinesAvailablePage, 3L)
    .success
    .value
    .set(NetTakingsHigherPage, BigDecimal(100))
    .success
    .value
    .set(MgdHigherRatePage, BigDecimal(-83.33))
    .success
    .value
    .set(NetTakingsStandardPage, BigDecimal(200))
    .success
    .value
    .set(MgdStandardRatePage, BigDecimal(-44.44))
    .success
    .value
    .set(NetTakingsLowerPage, BigDecimal(300))
    .success
    .value
    .set(MgdLowerRatePage, BigDecimal(-5.56))
    .success
    .value
    .set(TotalUnderDeclaredDutyPage, BigDecimal(7.77))
    .success
    .value
    .set(NegativeDutyBroughtForwardInputPage, BigDecimal(1.99))
    .success
    .value

  "SubmitReturnRequest.from" should {
    "return None when no SelectedReturn is present in UserAnswers" in {
      SubmitReturnRequest.from("regNum", "sessionId", "ENG", UserAnswers("regNum")) mustBe None
    }

    "build a SubmitReturnRequest from UserAnswers, deriving totals and defaulting missing values to zero" in {
      val result = SubmitReturnRequest.from("XGM00003122200", "sessionId", "ENG", userAnswersWithData)

      result mustBe Some(
        SubmitReturnRequest(
          mgdRegNumber                     = "XGM00003122200",
          consecNo                         = 12345,
          sessionId                        = "sessionId",
          periodStart                      = LocalDate.of(2025, 1, 1),
          periodEnd                        = LocalDate.of(2025, 3, 31),
          language                         = "ENG",
          numberOfMachines                 = 3L,
          netTakingsAtHigherRate           = BigDecimal(100),
          dueAtHigherRate                  = BigDecimal(-83.33),
          netTakingsAtStandardRate         = BigDecimal(200),
          dueAtStandardRate                = BigDecimal(-44.44),
          netTakingsAtLowerRate            = BigDecimal(300),
          dueAtLowerRate                   = BigDecimal(-5.56),
          dueBeforeAdjustments             = BigDecimal(-133.33),
          underDeclaredFromPreviousPeriods = BigDecimal(7.77),
          broughtForward                   = BigDecimal(1.99),
          carryForward                     = BigDecimal(127.55),
          netPayable                       = BigDecimal(0)
        )
      )
    }

    "build a SubmitReturnRequest from UserAnswers use absolute value for broughtForward" in {
      val userAnswersWithNegative =
        userAnswersWithData
          .set(NegativeDutyBroughtForwardInputPage, BigDecimal(-1.99))
          .success
          .value

      val result = SubmitReturnRequest.from("XGM00003122200", "sessionId", "ENG", userAnswersWithNegative)

      result.map(_.broughtForward) mustBe Some(BigDecimal(1.99))
    }

    "default missing rate/duty answers to zero and compute a positive netPayable with zero carryForward" in {
      val userAnswers = UserAnswers("regNum").set(SelectReturnPage, selectedReturn).success.value
      val result = SubmitReturnRequest.from("XGM00003122200", "sessionId", "CYM", userAnswers)

      result.value.numberOfMachines mustBe 0L
      result.value.netTakingsAtHigherRate mustBe BigDecimal(0)
      result.value.dueBeforeAdjustments mustBe BigDecimal(0)
      result.value.netPayable mustBe BigDecimal(0)
      result.value.carryForward mustBe BigDecimal(0)
      result.value.language mustBe "CYM"
    }
  }

  "netPayable" should {
    "equal netMGDPayableOnThisReturn when it is positive" in {
      val result = SubmitReturnRequest.from("regNum", "sessionId", "ENG", userAnswersWithNetMGDPayableOnThisReturn(BigDecimal(50)))
      result.value.netPayable mustBe BigDecimal(50)
      result.value.carryForward mustBe BigDecimal(0)
    }

    "be zero when netMGDPayableOnThisReturn is exactly zero" in {
      val result = SubmitReturnRequest.from("regNum", "sessionId", "ENG", userAnswersWithNetMGDPayableOnThisReturn(BigDecimal(0)))
      result.value.netPayable mustBe BigDecimal(0)
      result.value.carryForward mustBe BigDecimal(0)
    }

    "be zero when netMGDPayableOnThisReturn is negative" in {
      val result = SubmitReturnRequest.from("regNum", "sessionId", "ENG", userAnswersWithNetMGDPayableOnThisReturn(BigDecimal(-127.55)))
      result.value.netPayable mustBe BigDecimal(0)
      result.value.carryForward mustBe BigDecimal(127.55)
    }
  }

  "carryForward" should {
    "be zero when netMGDPayableOnThisReturn is positive" in {
      val result = SubmitReturnRequest.from("regNum", "sessionId", "ENG", userAnswersWithNetMGDPayableOnThisReturn(BigDecimal(50)))
      result.value.carryForward mustBe BigDecimal(0)
      result.value.netPayable mustBe BigDecimal(50)
    }

    "be zero when netMGDPayableOnThisReturn is exactly zero" in {
      val result = SubmitReturnRequest.from("regNum", "sessionId", "ENG", userAnswersWithNetMGDPayableOnThisReturn(BigDecimal(0)))
      result.value.carryForward mustBe BigDecimal(0)
      result.value.netPayable mustBe BigDecimal(0)
    }

    "equal the absolute value of netMGDPayableOnThisReturn when it is negative" in {
      val result = SubmitReturnRequest.from("regNum", "sessionId", "ENG", userAnswersWithNetMGDPayableOnThisReturn(BigDecimal(-127.55)))
      result.value.carryForward mustBe BigDecimal(127.55)
      result.value.netPayable mustBe BigDecimal(0)
    }
  }

  private def userAnswersWithNetMGDPayableOnThisReturn(value: BigDecimal): UserAnswers =
    UserAnswers("regNum")
      .set(SelectReturnPage, selectedReturn)
      .success
      .value
      .set(MgdLowerRatePage, value)
      .success
      .value

}
