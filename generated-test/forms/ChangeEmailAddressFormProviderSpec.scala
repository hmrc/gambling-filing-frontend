package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ChangeEmailAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "changeEmailAddress.error.required"
  val lengthKey = "changeEmailAddress.error.length"
  val maxLength = 70

  val form = new ChangeEmailAddressFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
