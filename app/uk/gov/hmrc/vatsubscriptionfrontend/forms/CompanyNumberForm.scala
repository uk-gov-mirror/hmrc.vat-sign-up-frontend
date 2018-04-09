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

package uk.gov.hmrc.vatsubscriptionfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.vatsubscriptionfrontend.forms.prevalidation.PreprocessedForm
import uk.gov.hmrc.vatsubscriptionfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.vatsubscriptionfrontend.forms.validation.utils.MappingUtil._

object CompanyNumberForm {

  val companyNumber = "companyNumber"
  private val allNumbersRegex = "^([0-9]{1,8})$".r
  private val withPrefixRegex = "^([A-Za-z][A-Za-z0-9])([0-9]{0,6})$".r
  private val maxLength = 8

  // https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/426891/uniformResourceIdentifiersCustomerGuide.pdf
  lazy val validCompanyNumberPrefixes = Set(
    "AC", "ZC", "FC", "GE",
    "LP", "OC", "SE", "SA",
    "SZ", "SF", "GS", "SL",
    "SO", "SC", "ES", "NA",
    "NZ", "NF", "GN", "NL",
    "NC", "R0", "NI", "EN",
    "IP", "SP", "IC", "SI",
    "NP", "NV", "RC", "SR",
    "NR", "NO"
  )

  val withinMaxLength: Constraint[String] = Constraint("companyNumber.maxLength")(
    companyNumber => if (companyNumber.length <= maxLength) Valid else Invalid("error.invalid_company_number")
  )

  val allNumbers: Constraint[String] = Constraint("companyNumber.allNumbers") {
    case allNumbersRegex(numbers) if numbers.toInt > 0 => Valid
    case x => Invalid("error.invalid_company_number")
  }

  lazy val numbersWithValidPrefix: Constraint[String] = Constraint("companyNumber.prefix") {
    case withPrefixRegex(prefix, numbers) if validCompanyNumberPrefixes.contains(prefix) && numbers.toInt > 0 => Valid
    case x => Invalid("error.invalid_company_number")
  }

  lazy val companyNumberValidation: Constraint[String] = withinMaxLength andThen (numbersWithValidPrefix or allNumbers)

  private val companyNumberValidationForm = Form(
    single(
      companyNumber -> optText.toText.verifying(companyNumberValidation)
    )
  )

  import uk.gov.hmrc.vatsubscriptionfrontend.forms.prevalidation.CaseOption._
  import uk.gov.hmrc.vatsubscriptionfrontend.forms.prevalidation.TrimOption._

  lazy val companyNumberForm = PreprocessedForm(
    validation = companyNumberValidationForm,
    trimRules = Map(companyNumber -> all),
    caseRules = Map(companyNumber -> upper)
  )

}
