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

  val vatRegistrationDateForm = Form(
    single(
      vatRegistrationDate -> DateMapping.dateMapping.verifying(vatRegistrationDateInvalid)
    )
  )

}
