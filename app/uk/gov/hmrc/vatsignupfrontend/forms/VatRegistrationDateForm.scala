/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.DateMapping
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._
import uk.gov.hmrc.vatsignupfrontend.models.DateModel

import scala.util.Try

object VatRegistrationDateForm {

  val vatRegistrationDate = "vatRegistrationDate"

  val vatRegistrationDateInvalid: Constraint[DateModel] = constraint[DateModel](
    date => {
      Try[ValidationResult] {
        date.toLocalDate
        Valid
      }.getOrElse(Invalid("error.invalid_vat_registration_date"))
    }
  )

  val vatRegistrationDateCharacters: Constraint[DateModel] = constraint[DateModel](
    date => validateNot(
      constraint = (date.day matches dateRegex) && (date.month matches dateRegex) && (date.year matches dateRegex),
      principalErrMsg = "error.invalid_vat_registration_date_characters"
    )
  )

  val vatRegistrationDateEntered: Constraint[DateModel] = constraint[DateModel](
    date => validateNot(
      constraint = date.day.nonEmpty && date.month.nonEmpty && date.year.nonEmpty,
      principalErrMsg = "error.no_vat_registration_date_entered"
    )
  )

  val vatRegistrationDateIsInPast: Constraint[DateModel] = constraint[DateModel](
    date => validateNot(
      constraint = date.toLocalDate.isBefore(LocalDate.now),
      principalErrMsg = "error.vat_registration_date_future"
    )
  )

  val vatRegistrationDateForm = Form(
    single(
      vatRegistrationDate -> DateMapping.dateMapping.verifying(
        vatRegistrationDateEntered
          andThen vatRegistrationDateCharacters
          andThen vatRegistrationDateInvalid
          andThen vatRegistrationDateIsInPast
      )
    )
  )

}
