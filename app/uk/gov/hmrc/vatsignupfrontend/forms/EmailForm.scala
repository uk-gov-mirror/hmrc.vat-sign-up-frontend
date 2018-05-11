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
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.PreprocessedForm
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.emailRegex
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.MappingUtil._

object EmailForm {

  val email = "email"

  val MaxLengthEmail = 132

  val emailMaxLength: Constraint[String] = Constraint("email.maxLength")(
    email => if (email.trim.length > MaxLengthEmail) Invalid("error.exceeds_max_length_email") else Valid
  )
  val emailInvalid: Constraint[String] = Constraint("email.invalid")(
    email => if (email matches emailRegex) Valid else Invalid("error.invalid_email")
  )


  private val emailValidationForm = Form(
    single(
      email -> optText.toText.verifying(emailMaxLength andThen emailInvalid)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  val emailForm = PreprocessedForm(
    validation = emailValidationForm,
    trimRules = Map(email -> all)
  )

}
