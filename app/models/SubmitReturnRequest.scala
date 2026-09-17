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

import pages.*
import play.api.libs.json.{Json, OWrites, Writes}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class SubmitReturnRequest(
  mgdRegNumber: String,
  consecNo: Int,
  sessionId: String,
  periodStart: LocalDate,
  periodEnd: LocalDate,
  language: String,
  numberOfMachines: Long,
  netTakingsAtHigherRate: BigDecimal,
  dueAtHigherRate: BigDecimal,
  netTakingsAtStandardRate: BigDecimal,
  dueAtStandardRate: BigDecimal,
  netTakingsAtLowerRate: BigDecimal,
  dueAtLowerRate: BigDecimal,
  dueBeforeAdjustments: BigDecimal,
  underDeclaredFromPreviousPeriods: BigDecimal,
  broughtForward: BigDecimal,
  carryForward: BigDecimal,
  netPayable: BigDecimal
)

object SubmitReturnRequest {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val writes: OWrites[SubmitReturnRequest] = Json.writes[SubmitReturnRequest]

  def from(regNum: String, sessionId: String, language: String, userAnswers: UserAnswers): Option[SubmitReturnRequest] =
    userAnswers.get(SelectReturnPage).map { selectedReturn =>
      val dueAtLowerRate = userAnswers.get(MgdLowerRatePage).getOrElse(BigDecimal(0))
      val dueAtStandardRate = userAnswers.get(MgdStandardRatePage).getOrElse(BigDecimal(0))
      val dueAtHigherRate = userAnswers.get(MgdHigherRatePage).getOrElse(BigDecimal(0))
      val declaredSubmission = DeclaredSubmission.from(userAnswers)

      SubmitReturnRequest(
        mgdRegNumber                     = regNum,
        consecNo                         = selectedReturn.consecNo,
        sessionId                        = sessionId,
        periodStart                      = selectedReturn.periodStart,
        periodEnd                        = selectedReturn.periodEnd,
        language                         = language,
        numberOfMachines                 = userAnswers.get(MachinesAvailablePage).getOrElse(0L),
        netTakingsAtHigherRate           = userAnswers.get(NetTakingsHigherPage).getOrElse(BigDecimal(0)),
        dueAtHigherRate                  = dueAtHigherRate,
        netTakingsAtStandardRate         = userAnswers.get(NetTakingsStandardPage).getOrElse(BigDecimal(0)),
        dueAtStandardRate                = dueAtStandardRate,
        netTakingsAtLowerRate            = userAnswers.get(NetTakingsLowerPage).getOrElse(BigDecimal(0)),
        dueAtLowerRate                   = dueAtLowerRate,
        dueBeforeAdjustments             = declaredSubmission.dutyPayableBeforeAdjustments,
        underDeclaredFromPreviousPeriods = declaredSubmission.underDeclaredTaxFromPreviousPeriods,
        broughtForward                   = declaredSubmission.amountBroughtForward,
        // when netMGDPayableOnThisReturn is positive, this means tax is due and we should submit it as such and ensure carry forward is set to 0.
        // when netMGDPayableOnThisReturn is negative, this means not tax is due and we should submit this value as carry forward and ensure net payable is set to 0.
        carryForward = declaredSubmission.netMGDPayableOnThisReturn.min(BigDecimal(0)).abs,
        netPayable   = declaredSubmission.netMGDPayableOnThisReturn.max(BigDecimal(0))
      )
    }
}
