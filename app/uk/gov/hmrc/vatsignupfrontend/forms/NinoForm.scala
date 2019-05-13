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
import play.api.data.validation.Constraint
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.ninoRegex
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._

object NinoForm {

  val nino = "nino"

  val length = 9

  def isEntered(isAgent: Boolean): Constraint[String] = Constraint("nino.entered")(
    ninoValue => validate(
      constraint = ninoValue.isEmpty,
      principalErrMsg = "error.principal.no_entry_nino",
      agentErrMsg = Some("error.agent.no_entry_nino"),
      isAgent = isAgent
    )
  )

  val checkLength: Constraint[String] = Constraint("nino.length")(
    ninoValue => validateNot(
      constraint = ninoValue.length.equals(length),
      principalErrMsg = "error.character_limit_nino"
    )
  )

  val checkFormat: Constraint[String] = Constraint("nino.format")(
    ninoValue => validateNot(
      constraint = ninoValue.matches(ninoRegex),
      principalErrMsg = "error.invalid_nino"
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  private def ninoValidationForm(isAgent: Boolean) = Form(
    single(
      nino -> text.verifying(isEntered(isAgent) andThen checkLength andThen checkFormat)
    )
  )

  def ninoForm(isAgent: Boolean): PrevalidationAPI[String] = PreprocessedForm(
    validation = ninoValidationForm(isAgent),
    trimRules = Map(nino -> all),
    caseRules = Map(nino -> upper)
  )

}
