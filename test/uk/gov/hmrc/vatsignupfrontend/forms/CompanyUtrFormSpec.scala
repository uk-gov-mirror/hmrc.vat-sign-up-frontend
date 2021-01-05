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

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstantsGenerator
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstantsGenerator._

class CompanyUtrFormSpec extends PlaySpec {

  import uk.gov.hmrc.vatsignupfrontend.forms.CompanyUtrForm._

  "The companyUtrForm" should {

    val no_entry_error_key = "error.no_entry_company_utr"
    val exceeds_characters_limit_error_key = "error.character_limit_company_utr"
    val invalid_characters_error_key = "error.invalid_company_utr"

    "validate that data containing 10 digits passes" in {
      val testUtr = TestConstantsGenerator.randomUTRNumeric()
      val actual = companyUtrForm.bind(Map(companyUtr -> testUtr)).value
      actual shouldBe Some(testUtr)
    }

    "validate that no data has been entered" in {
      val formWithError = companyUtrForm.bind(Map(companyUtr -> ""))
      formWithError.errors should contain(FormError(companyUtr, no_entry_error_key))
    }

    "validate that data containing incorrect format of non numeric data fails" in {
      val formWithError = companyUtrForm.bind(Map(companyUtr -> "A2345A789A"))
      formWithError.errors should contain(FormError(companyUtr, invalid_characters_error_key))
    }

    "validate that data containing more than 10 digits fails" in {
      val formWithError = companyUtrForm.bind(Map(companyUtr -> "123 456 789 10"))
      formWithError.errors should contain(FormError(companyUtr, exceeds_characters_limit_error_key))
    }

    "validate that data containing fewer than 10 digits but greater than 0 fails" in {
      val testUtr = "1"
      val formWithError = companyUtrForm.bind(Map(companyUtr -> testUtr))
      formWithError.errors should contain(FormError(companyUtr, exceeds_characters_limit_error_key))
    }

  }

}
