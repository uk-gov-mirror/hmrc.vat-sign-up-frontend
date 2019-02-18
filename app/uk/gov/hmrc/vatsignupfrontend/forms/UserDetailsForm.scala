/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._

import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, UserDetailsModel}


import scala.util.Try

object UserDetailsForm {

  val userFirstName = "firstName"
  val userLastName = "lastName"
  val nameMaxLength = 105
  val userNino = "nino"
  val userNinoLength = 9
  val userDateOfBirth = "dateOfBirth"


  def firstNameNotEntered(isAgent: Boolean): Constraint[String] = Constraint("first_name.not_entered")(
    firstName => validate(
      constraint = firstName.isEmpty,
      principalErrMsg = "error.principal.no_entry_first_name",
      agentErrMsg = Some("error.agent.no_entry_first_name"),
      isAgent = isAgent
    )
  )

  val firstNameInvalid: Constraint[String] = Constraint("first_name.invalid")(
    firstName => validateNot(
      constraint = validText(firstName.trim),
      principalErrMsg = "error.invalid_first_name"
    )
  )

  val firstNameMaxLength: Constraint[String] = Constraint("first_name.maxLength")(
    firstName => validate(
      constraint = firstName.trim.length > nameMaxLength,
      principalErrMsg = "error.exceeds_max_first_name"
    )
  )

  def lastNameNotEntered(isAgent: Boolean): Constraint[String] = Constraint("last_name.not_entered")(
    lastName => validate(
      constraint = lastName.isEmpty,
      principalErrMsg = "error.principal.no_entry_last_name",
      agentErrMsg = Some("error.agent.no_entry_last_name"),
      isAgent = isAgent
    )
  )

  val lastNameInvalid: Constraint[String] = Constraint("last_name.invalid")(
    lastName => validateNot(
      constraint = validText(lastName.trim),
      principalErrMsg = "error.invalid_last_name"
    )
  )

  val lastNameMaxLength: Constraint[String] = Constraint("last_name.maxLength")(
    lastName => validate(
      constraint = lastName.trim.length > nameMaxLength,
      principalErrMsg = "error.exceeds_max_last_name"
    )
  )

  def ninoNotEntered(isAgent: Boolean): Constraint[String] = Constraint("nino.not_entered")(
    nino => validate(
      constraint = nino.isEmpty,
      principalErrMsg = "error.principal.no_entry_nino",
      agentErrMsg = Some("error.agent.no_entry_nino"),
      isAgent = isAgent
    )
  )

  val invalidNino: Constraint[String] = Constraint("nino.invalid")(
    nino => validateNot(
      constraint = validNino(nino.filterNot(_.isWhitespace)),
      principalErrMsg = "error.invalid_nino"
    )
  )

  val ninoLength: Constraint[String] = Constraint("nino.invalid_length")(
    nino => validate(
      constraint = nino.length < userNinoLength,
      principalErrMsg = "error.character_limit_nino"
    )
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
    date => validateNot(
      constraint = (date.day matches dateRegex) && (date.month matches dateRegex) && (date.year matches dateRegex),
      principalErrMsg = "error.invalid_characters_dob"
    )
  )

  def dobNotEntered(isAgent: Boolean): Constraint[DateModel] = constraint[DateModel](
    date => validate(
      constraint = date.day.isEmpty && date.month.isEmpty && date.year.isEmpty,
      principalErrMsg = "error.principal.no_entry_dob",
      agentErrMsg = Some("error.agent.no_entry_dob"),
      isAgent = isAgent
    )
  )

  def userValidationDetailsForm(isAgent: Boolean) = Form(
    mapping(
      userFirstName -> optText.toText.verifying(firstNameNotEntered(isAgent) andThen firstNameInvalid andThen firstNameMaxLength),
      userLastName -> optText.toText.verifying(lastNameNotEntered(isAgent) andThen lastNameInvalid andThen lastNameMaxLength),
      userNino -> optText.toText.verifying(ninoNotEntered(isAgent) andThen ninoLength andThen invalidNino),
      userDateOfBirth -> DateMapping.dateMapping.verifying(dobNotEntered(isAgent) andThen dobInvalidCharacters andThen dobInvalid)
    )(UserDetailsModel.apply)(UserDetailsModel.unapply)
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  def userDetailsForm(isAgent: Boolean) = PreprocessedForm(
    validation = userValidationDetailsForm(isAgent = isAgent),
    trimRules = Map(
      userFirstName -> both,
      userLastName -> both,
      userNino -> all
    ),
    caseRules = Map(
      userNino -> upper
    )
  )

}
