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


object CompanyUtrForm {

  val companyUtr = "companyUtr"

  val companyUtrLength = 10

  val companyUtrInvalid: Constraint[String] = Constraint("companyUtr.invalid")(
    companyUtr => validateNot(
      constraint = companyUtr matches companyUtrRegex,
      principalErrMsg = "error.invalid_company_utr"
    )
  )

  val companyUtrNotEntered: Constraint[String] = Constraint("companyUtr.not_entered")(
    companyUtr => validate(
      constraint = companyUtr.isEmpty,
      principalErrMsg = "error.no_entry_company_utr"
    )
  )

  val companyUtrInvalidLength: Constraint[String] = Constraint("companyUtr.invalid")(
    companyUtr => validateNot(
      constraint = companyUtr.length == companyUtrLength,
      principalErrMsg = "error.character_limit_company_utr"
    )
  )

  private val companyUtrValidationForm = Form(
    single(
      companyUtr -> optText.toText.verifying(companyUtrNotEntered andThen companyUtrInvalidLength andThen companyUtrInvalid)
    )
  )

  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsignupfrontend.forms.prevalidation.TrimOption._

  lazy val companyUtrForm = PreprocessedForm(
    validation = companyUtrValidationForm,
    trimRules = Map(companyUtr -> all),
    caseRules = Map(companyUtr -> upper)
  )

}
