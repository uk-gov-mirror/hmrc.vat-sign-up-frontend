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
import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.PreprocessedForm
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.MappingUtil._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.companyUtrRegex
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ValidationHelper._


object PartnershipUtrForm {

  val partnershipUtr = "partnershipUtr"

  val partnershipUtrLength = 10

  val partnershipUtrInvalid: Constraint[String] = Constraint("partnershipUtr.invalid")(
    partnershipUtr => validateNot(
      constraint = partnershipUtr matches companyUtrRegex,
      principalErrMsg = "error.invalid_partnership_utr"
    )
  )

  val partnershipUtrNotEntered: Constraint[String] = Constraint("partnershipUtr.not_entered")(
    partnershipUtr => validate(
      constraint = partnershipUtr.isEmpty,
      principalErrMsg = "error.no_entry_partnership_utr"
    )
  )

  val partnershipUtrInvalidLength: Constraint[String] = Constraint("partnershipUtr.invalid")(
    partnerhsipUtr => validateNot(
      constraint = partnerhsipUtr.length == partnershipUtrLength,
      principalErrMsg = "error.character_limit_partnership_utr"
    )
  )

  private val partnershipUtrValidationForm = Form(
    single(
      partnershipUtr -> optText.toText.verifying(partnershipUtrNotEntered andThen partnershipUtrInvalidLength andThen partnershipUtrInvalid)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  val partnershipUtrForm = PreprocessedForm(
    validation = partnershipUtrValidationForm,
    trimRules = Map(partnershipUtr -> all),
    caseRules = Map(partnershipUtr -> upper)
  )

}