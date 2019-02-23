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
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.{boxFiveValueRegex, numericRegex}

object BoxFiveValueForm {

  val boxFiveValue = "boxFiveValue"

  val maxLength = 14

  val isEntered: Constraint[String] = Constraint("box_five_value.entered")(
    boxFiveValue => validate(
      constraint = boxFiveValue.isEmpty,
      principalErrMsg = "error.principal.box_five_value_not_entered"
    )
  )

  val checkFormat: Constraint[String] = Constraint("box_five_value.invalid_format")(
    boxFiveValue => validateNot(
      constraint = boxFiveValue matches boxFiveValueRegex,
      principalErrMsg = "error.principal.box_five_value_invalid_format"
    )
  )

  val checkLength: Constraint[String] = Constraint("box_five_value.invalid_length")(
    boxFiveValue => validateNot(
      constraint = boxFiveValue.length <= maxLength,
      principalErrMsg = "error.principal.box_five_value_invalid_length"
    )
  )

  private def boxFiveValidationForm() = Form(
    single(
     boxFiveValue -> text.verifying(isEntered andThen checkLength andThen checkFormat)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  def boxFiveValueForm: PrevalidationAPI[String] = PreprocessedForm(
    validation = boxFiveValidationForm(),
    trimRules = Map(boxFiveValue -> all),
    caseRules = Map(boxFiveValue -> upper)
  )

}
