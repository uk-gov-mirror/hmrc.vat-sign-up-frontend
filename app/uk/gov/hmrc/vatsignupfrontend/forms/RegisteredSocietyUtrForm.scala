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

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.PreprocessedForm
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.MappingUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.companyUtrRegex
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._


object RegisteredSocietyUtrForm {

  val registeredSocietyUtr = "registeredSocietyUtr"

  val registeredSocietyUtrLength = 10

  val registeredSocietyUtrInvalid: Constraint[String] = Constraint("registeredSocietyUtr.invalid")(
    registeredSocietyUtr => validateNot(
      constraint = registeredSocietyUtr matches companyUtrRegex,
      principalErrMsg = "error.invalid_registered_society_utr"
    )
  )

  val registeredSocietyUtrNotEntered: Constraint[String] = Constraint("registeredSocietyUtr.not_entered")(
    registeredSocietyUtr => validate(
      constraint = registeredSocietyUtr.isEmpty,
      principalErrMsg = "error.no_entry_registered_society_utr"
    )
  )

  val registeredSocietyUtrInvalidLength: Constraint[String] = Constraint("registeredSocietyUtr.invalid")(
    registeredSocietyUtr => validateNot(
      constraint = registeredSocietyUtr.length == registeredSocietyUtrLength,
      principalErrMsg = "error.character_limit_registered_society_utr"
    )
  )

  private val registeredSocietyUtrValidationForm = Form(
    single(
      registeredSocietyUtr -> optText.toText.verifying(
        registeredSocietyUtrNotEntered andThen registeredSocietyUtrInvalidLength andThen registeredSocietyUtrInvalid)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  lazy val registeredSocietyUtrForm = PreprocessedForm(
    validation = registeredSocietyUtrValidationForm,
    trimRules = Map(registeredSocietyUtr -> all),
    caseRules = Map(registeredSocietyUtr -> upper)
  )

}
