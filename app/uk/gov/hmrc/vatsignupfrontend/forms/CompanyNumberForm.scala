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
import play.api.data.validation.Constraint
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.PreprocessedForm
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.MappingUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._

object CompanyNumberForm {

  val companyNumber = "companyNumber"
  private val maxLength = 8
  private val minLength = 1

  def withinMinAndMaxLength: Constraint[String] = Constraint("companyNumber.maxLength")(
    companyNumber => validateNot(
      constraint = (companyNumber.length >= minLength) && (companyNumber.length <= maxLength),
      principalErrMsg = "error.invalid_company_number_length"
    )
  )

  def crnNotEntered(isAgent: Boolean): Constraint[String] = Constraint("companyNumber.maxLength")(
    companyNumber => validate(
      constraint = companyNumber.isEmpty,
      principalErrMsg = "error.principal.company_number_not_entered",
      agentErrMsg = Some("error.agent.company_number_not_entered"),
      isAgent = isAgent
    )
  )

  private def companyNumberValidationForm(isAgent:Boolean) = Form(
    single(
      companyNumber -> optText.toText.verifying(crnNotEntered(isAgent = isAgent) andThen withinMinAndMaxLength)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  def companyNumberForm(isAgent: Boolean) = PreprocessedForm(
    validation = companyNumberValidationForm(isAgent = isAgent),
    trimRules = Map(companyNumber -> all),
    caseRules = Map(companyNumber -> upper)
  )

}
