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
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.vatNumberRegex

object VatNumberForm {

  val vatNumber = "vatNumber"

  val vatNumberLength: Constraint[String] = Constraint("vat_number.length")(
    vatNumber => if (vatNumber.length != 9) Invalid("error.invalid_vat_number_length") else Valid
  )

  def vatNumberEntered(isAgent: Boolean): Constraint[String] = Constraint("vat_number.entered")(
    vatNumber => if (vatNumber.isEmpty && isAgent)
      Invalid("error.agent.no_vat_number_entered")
    else if (vatNumber.isEmpty && !isAgent)
      Invalid("error.principal.no_vat_number_entered")
    else Valid
  )

  val vatNumberFormat: Constraint[String] = Constraint("vat_number.format")(
    vatNumber => if (!(vatNumber matches vatNumberRegex)) Invalid("error.invalid_vat_number") else Valid
  )

  private def vatNumberValidationForm(isAgent: Boolean) = Form(
    single(
      vatNumber -> text.verifying(vatNumberEntered(isAgent = isAgent) andThen vatNumberFormat andThen vatNumberLength)
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
