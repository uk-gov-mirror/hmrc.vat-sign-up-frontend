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
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.postcodeRegex
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._
import uk.gov.hmrc.vatsignupfrontend.models.PostCode

object BusinessPostCodeForm {

  val businessPostCode = "businessPostCode"

  val postCodeInvalid: Constraint[String] = Constraint("businessPostCode.invalid")(
    postCode => validateNot(
      constraint = postCode matches postcodeRegex,
      principalErrMsg = "error.invalid_postcode"
    )
  )

  val businessPostCodeNotEntered: Constraint[String] = Constraint("postcode.notEntered")(
    postCode => validate(
      constraint = postCode.isEmpty,
      principalErrMsg = "error.postcode_not_entered"
    )
  )

  private val businessPostCodeValidationForm = Form(
    mapping(
      businessPostCode -> text.verifying(businessPostCodeNotEntered andThen postCodeInvalid)
    )(PostCode.apply)(PostCode.unapply)
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  val businessPostCodeForm = PreprocessedForm(
    validation = businessPostCodeValidationForm,
    trimRules = Map(businessPostCode -> all),
    caseRules = Map(businessPostCode -> upper)
  )

}