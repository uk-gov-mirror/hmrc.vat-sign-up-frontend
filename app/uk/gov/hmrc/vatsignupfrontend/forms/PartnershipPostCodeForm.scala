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
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._
import uk.gov.hmrc.vatsignupfrontend.models.PostCode

object PartnershipPostCodeForm {

  val partnershipPostCode = "partnershipPostCode"

  val postCodeNotEntered: Constraint[String] = Constraint("postcode.notEntered")(
    postCode => validate(
      constraint = postCode.isEmpty,
      principalErrMsg = "error.agent.partnership_postcode.not_entered"
    )
  )

  val postCodeInvalidCharacters: Constraint[String] = Constraint("postcode.invalid-characters")(
    postCode => validateNot(
      constraint = postCode.toUpperCase matches Patterns.alphanumericRegex,
      principalErrMsg = "error.partnership_postcode.invalid_characters"
    )
  )

  val postCodeInvalid: Constraint[String] = Constraint("postcode.invalid")(
    postCode => validateNot(
      constraint = Patterns.validPostcode(postCode),
      principalErrMsg = "error.partnership_postcode.invalid"
    )
  )

  val businessPostCodeValidationForm = Form(
    mapping(
      partnershipPostCode -> text.verifying(postCodeNotEntered andThen postCodeInvalidCharacters andThen postCodeInvalid)
    )(PostCode.apply)(PostCode.unapply)
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  def partnershipPostCodeForm: PrevalidationAPI[PostCode] = PreprocessedForm(
    validation = businessPostCodeValidationForm,
    trimRules = Map(partnershipPostCode -> all),
    caseRules = Map(partnershipPostCode -> upper)
  )

}
