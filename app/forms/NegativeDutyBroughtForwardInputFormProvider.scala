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

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

class NegativeDutyBroughtForwardInputFormProvider @Inject() extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "value" ->
        currency(
          "negativeDutyBroughtForwardInput.error.required",
          "negativeDutyBroughtForwardInput.error.invalid",
          "negativeDutyBroughtForwardInput.error.range"
        ).verifying(
          validAmount
        )
    )

  private def validAmount: Constraint[BigDecimal] =
    Constraint { value =>
      if (value == 0) {
        Invalid("negativeDutyBroughtForwardInput.error.required")
      } else if (value > BigDecimal(1000000000)) {
        Invalid("negativeDutyBroughtForwardInput.error.range")
      } else if (value < BigDecimal(-1000000000)) {
        Invalid("negativeDutyBroughtForwardInput.error.range")
      } else {
        Valid
      }
    }
}
