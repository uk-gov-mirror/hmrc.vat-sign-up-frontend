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
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.box5FigureRegex
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._

object Box5FigureForm {

  val box5Figure = "box5Figure"

  val defaultMaxLength = 14

  val negativeValueMaxLength = 15

  val isEntered: Constraint[String] = Constraint("box_5_figure.entered")(
    boxFiveValue => validate(
      constraint = boxFiveValue.isEmpty,
      principalErrMsg = "error.principal.box_5_figure_not_entered"
    )
  )

  val checkFormat: Constraint[String] = Constraint("box_5_figure.invalid_format")(
    boxFiveValue => validateNot(
      constraint = boxFiveValue matches box5FigureRegex,
      principalErrMsg = "error.principal.box_5_figure_invalid_format"
    )
  )

  val checkLength: Constraint[String] = Constraint("box_5_figure.invalid_length")(
    boxFiveValue => validateNot(
      constraint = if (boxFiveValue.contains("-")) boxFiveValue.length <= negativeValueMaxLength else boxFiveValue.length <= defaultMaxLength,
      principalErrMsg = "error.principal.box_5_figure_invalid_length"
    )
  )

  private def box5FigureValidationForm() = Form(
    single(
      box5Figure -> text.verifying(isEntered andThen checkLength andThen checkFormat)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  def box5FigureForm: PrevalidationAPI[String] = PreprocessedForm(
    validation = box5FigureValidationForm(),
    trimRules = Map(box5Figure -> all),
    caseRules = Map(box5Figure -> upper)
  )

}
