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
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, UserDetailsModel}

import scala.util.Try

object UserDetailsForm {

  val userFirstName = "firstName"
  val userLastName = "lastName"
  val userNino = "nino"
  val userDateOfBirth = "dateOfBirth"

  val nameMaxLength = 105

  val firstNameInvalid: Constraint[String] = Constraint("first_name.invalid")(
    x => if (x.isEmpty || !Patterns.validText(x.trim)) Invalid("error.invalid_first_name") else Valid
  )

  val lastNameInvalid: Constraint[String] = Constraint("last_name.invalid")(
    x => if (x.isEmpty || !Patterns.validText(x.trim)) Invalid("error.invalid_last_name") else Valid
  )

  val firstNameMaxLength: Constraint[String] = Constraint("first_name.maxLength")(
    x => if (x.trim.length > nameMaxLength) Invalid("error.exceeds_max_first_name") else Valid
  )

  val lastNameMaxLength: Constraint[String] = Constraint("last_name.maxLength")(
    x => if (x.trim.length > nameMaxLength) Invalid("error.exceeds_max_last_name") else Valid
  )

  val dobInvalid: Constraint[DateModel] = constraint[DateModel](
    date => {
      Try[ValidationResult] {
        date.toLocalDate
        Valid
      }.getOrElse(Invalid("error.invalid_dob"))
    }
  )

  val userValidationDetailsForm = Form(
    mapping(
      userFirstName -> optText.toText.verifying(firstNameInvalid andThen firstNameMaxLength),
      userLastName -> optText.toText.verifying(lastNameInvalid andThen lastNameMaxLength),
      userNino -> optText.toText.verifying("error.invalid_nino", Patterns.validNino _),
      userDateOfBirth -> DateMapping.dateMapping.verifying(dobInvalid)
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
