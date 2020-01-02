/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.vatNumberRegex
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._

object VatNumberForm {

  val vatNumber = "vatNumber"

  val vatNumberLength = 9

  val vatNumberValidLength: Constraint[String] = Constraint("vat_number.length")(
    vatNumber => validateNot(
      constraint = vatNumber.length == vatNumberLength,
      principalErrMsg = "error.invalid_vat_number_length"
    )
  )

  def vatNumberEntered(isAgent: Boolean): Constraint[String] = Constraint("vat_number.entered")(
    vatNumber => validate(
      constraint = vatNumber.isEmpty,
      principalErrMsg = "error.principal.no_vat_number_entered",
      agentErrMsg = Some("error.agent.no_vat_number_entered"),
      isAgent = isAgent
    )
  )

  val vatNumberFormat: Constraint[String] = Constraint("vat_number.format")(
    vatNumber => validateNot(
      constraint = vatNumber matches vatNumberRegex,
      principalErrMsg = "error.invalid_vat_number"
    )
  )

  private def vatNumberValidationForm(isAgent: Boolean) = Form(
    single(
      vatNumber -> text.verifying(vatNumberEntered(isAgent = isAgent) andThen vatNumberValidLength andThen vatNumberFormat)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  def vatNumberForm(isAgent: Boolean) = PreprocessedForm(
    validation = vatNumberValidationForm(isAgent = isAgent),
    trimRules = Map(vatNumber -> all),
    caseRules = Map(vatNumber -> upper)
  )

}
