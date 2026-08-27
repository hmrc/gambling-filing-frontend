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

import play.api.libs.json.{Json, OFormat}

case class DeclaredSubmission(
  dutyPayableBeforeAdjustments: BigDecimal,
  underDeclaredTaxFromPreviousPeriods: BigDecimal,
  amountBroughtForward: BigDecimal,
  netMGDPayableOnThisReturn: BigDecimal
)

object DeclaredSubmission {
  implicit val format: OFormat[DeclaredSubmission] = Json.format[DeclaredSubmission]

  def apply(dutyPayableBeforeAdjustments: BigDecimal,
            underDeclaredTaxFromPreviousPeriods: BigDecimal,
            amountBroughtForward: BigDecimal
           ): DeclaredSubmission = {
    def amountBroughtForwardAsNegativeValue = amountBroughtForward * -1
    // user always enter positive value in the UI, we store the same in mongoDB,
    // but we pass it as a -ve value to the Declare&Submit page and the iForms

    new DeclaredSubmission(
      dutyPayableBeforeAdjustments,
      underDeclaredTaxFromPreviousPeriods,
      amountBroughtForwardAsNegativeValue,
      dutyPayableBeforeAdjustments + underDeclaredTaxFromPreviousPeriods + amountBroughtForwardAsNegativeValue
    )
  }
}
