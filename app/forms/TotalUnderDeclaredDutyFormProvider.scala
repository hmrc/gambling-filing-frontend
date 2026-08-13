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

package forms

import views.CurrencyFormatter
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import javax.inject.Inject

class TotalUnderDeclaredDutyFormProvider @Inject() extends Mappings with CurrencyFormatter {

  def apply(maximumAllowed: BigDecimal): Form[BigDecimal] =
    Form(
      "value" ->
        currency(
          "totalUnderDeclaredDuty.error.required",
          "totalUnderDeclaredDuty.error.invalid",
          "totalUnderDeclaredDuty.error.maximum",
          Seq(currencyFormat(maximumAllowed))
        ).verifying(
          maximumValue(maximumAllowed)
        )
    )

  private def maximumValue(
    maximumAllowed: BigDecimal
  ): Constraint[BigDecimal] =
    Constraint[BigDecimal]("totalUnderDeclaredDuty.maximum") { value =>
      if (value <= maximumAllowed) {
        Valid
      } else {
        Invalid(
          ValidationError(
            "totalUnderDeclaredDuty.error.maximum",
            currencyFormat(maximumAllowed)
          )
        )
      }
    }
}
