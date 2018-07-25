/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.PreprocessedForm
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.DateMapping
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.MappingUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns._
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, UserDetailsModel}

import scala.util.Try

object UserDetailsForm {

  val userFirstName = "firstName"
  val userLastName = "lastName"
  val userNino = "nino"
  val userDateOfBirth = "dateOfBirth"

  val nameMaxLength = 105

  val firstNameNotEntered: Constraint[String] = Constraint("first_name.not_entered")(
    firstName => if (firstName.isEmpty) Invalid("error.no_entry_first_name") else Valid
  )

  val firstNameInvalid: Constraint[String] = Constraint("first_name.invalid")(
    firstName => if (!validText(firstName.trim)) Invalid("error.invalid_first_name") else Valid
  )

  val firstNameMaxLength: Constraint[String] = Constraint("first_name.maxLength")(
    firstName => if (firstName.trim.length > nameMaxLength) Invalid("error.exceeds_max_first_name") else Valid
  )

  val lastNameNotEntered: Constraint[String] = Constraint("last_name.not_entered")(
    lastName => if (lastName.isEmpty) Invalid("error.no_entry_last_name") else Valid
  )

  val lastNameInvalid: Constraint[String] = Constraint("last_name.invalid")(
    lastName => if (!validText(lastName.trim)) Invalid("error.invalid_last_name") else Valid
  )

  val lastNameMaxLength: Constraint[String] = Constraint("last_name.maxLength")(
    lastName => if (lastName.trim.length > nameMaxLength) Invalid("error.exceeds_max_last_name") else Valid
  )

  val ninoEntered: Constraint[String] = Constraint("nino.not_entered")(
    nino => if (nino.isEmpty) Invalid("error.no_entry_nino") else Valid
  )

  val invalidNino: Constraint[String] = Constraint("nino.invalid")(
    nino => if (!validNino(nino)) Invalid("error.invalid_nino") else Valid
  )

  val ninoLength: Constraint[String] = Constraint("nino.invalid_length")(
    nino => if (nino.length < 9) Invalid("error.character_limit_nino") else Valid
  )

  val dobInvalid: Constraint[DateModel] = constraint[DateModel](
    date => {
      Try[ValidationResult] {
        date.toLocalDate
        Valid
      }.getOrElse(Invalid("error.invalid_dob"))
    }
  )

  val dobInvalidCharacters: Constraint[DateModel] = constraint[DateModel](
    date => if (
      (date.day matches dateRegex) &&
        (date.month matches dateRegex) &&
        (date.year matches dateRegex)
    ) Valid else Invalid("error.invalid_characters_dob")
  )

  val dobEntered: Constraint[DateModel] = constraint[DateModel](
    date => if (
      date.day.nonEmpty &&
        date.month.nonEmpty &&
        date.year.nonEmpty
    ) Valid else Invalid("error.no_entry_dob")
  )
  val userValidationDetailsForm = Form(
    mapping(
      userFirstName -> optText.toText.verifying(firstNameNotEntered andThen firstNameInvalid andThen firstNameMaxLength),
      userLastName -> optText.toText.verifying(lastNameNotEntered andThen lastNameInvalid andThen lastNameMaxLength),
      userNino -> optText.toText.verifying(ninoEntered andThen ninoLength andThen invalidNino),
      userDateOfBirth -> DateMapping.dateMapping.verifying(dobEntered andThen dobInvalidCharacters andThen dobInvalid)
    )(UserDetailsModel.apply)(UserDetailsModel.unapply)
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  val userDetailsForm = PreprocessedForm(
    validation = userValidationDetailsForm,
    trimRules = Map(
      userFirstName -> both,
      userLastName -> both,
      userNino -> bothAndCompress
    ),
    caseRules = Map(
      userNino -> upper
    )
  )

}
