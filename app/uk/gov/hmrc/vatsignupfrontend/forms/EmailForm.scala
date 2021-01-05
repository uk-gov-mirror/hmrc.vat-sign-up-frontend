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
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.emailRegex
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._

object EmailForm {

  val email = "email"

  val MaxLengthEmail = 132

  val emailExceedsMaxLength: Constraint[String] = Constraint("email.maxLength")(
    email => validate(
      constraint = email.trim.length > MaxLengthEmail,
      principalErrMsg = "error.exceeds_max_length_email"
    )
  )

  val emailInvalid: Constraint[String] = Constraint("email.invalid")(
    email => validateNot(
      constraint = email matches emailRegex,
      principalErrMsg = "error.invalid_email"
    )
  )

  def emailNotEntered(isAgent: Boolean): Constraint[String] = Constraint("email.not_entered")(
    email => validate(
      constraint = email.isEmpty,
      principalErrMsg = "error.principal.email_no_entry",
      agentErrMsg = Some("error.agent.email_no_entry"),
      isAgent = isAgent
    )
  )

  private def emailValidationForm(isAgent: Boolean) = Form(
    single(
      email -> optText.toText.verifying(emailNotEntered(isAgent = isAgent) andThen emailExceedsMaxLength andThen emailInvalid)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  def emailForm(isAgent: Boolean) = PreprocessedForm(
    validation = emailValidationForm(isAgent = isAgent),
    trimRules = Map(email -> all),
    caseRules = Map(email -> lower)
  )

}
