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

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstantsGenerator
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstantsGenerator._

class CompanyNumberFormSpec extends PlaySpec {

  import uk.gov.hmrc.vatsubscriptionfrontend.forms.CompanyNumberForm._

  "The companyNumberForm" should {

    val errorKey = "error.invalid_company_number"

    "validate that data containing 8 digits passes" in {
      val testCrn = TestConstantsGenerator.randomCrnNumeric
      val actual = companyNumberForm.bind(Map(companyNumber -> testCrn)).value
      actual shouldBe Some(testCrn)
    }

    "validate that data starting with a valid prefix and followed by 6 digits passes" in {
      val testCrn = TestConstantsGenerator.randomCrnAlphaNumeric
      val actual = companyNumberForm.bind(Map(companyNumber -> testCrn)).value
      actual shouldBe Some(testCrn.toUpperCase)
    }

    "validate that data starting with an invalid prefix fails" in {
      val testCrn = "AA 1"
      val formWithError = companyNumberForm.bind(Map(companyNumber -> testCrn))
      formWithError.errors should contain(FormError(companyNumber, errorKey))
    }

    "validate that no data has been entered" in {
      val formWithError = companyNumberForm.bind(Map(companyNumber -> ""))
      formWithError.errors should contain(FormError(companyNumber, errorKey))
    }

    "validate that data containing incorrect format of non numeric data fails" in {
      val formWithError = companyNumberForm.bind(Map(companyNumber -> "123A456 A"))
      formWithError.errors should contain(FormError(companyNumber, errorKey))
    }

    "validate that data starting with 1 letter and followed by 7 numbers fails" in {
      val formWithError = companyNumberForm.bind(Map(companyNumber -> "A 1234567"))
      formWithError.errors should contain(FormError(companyNumber, errorKey))
    }

    "validate that data starting with a valid prefix and followed by 5 numbers passes" in {
      val testCrn = s"$randomPrefix 123 45"
      val actual = companyNumberForm.bind(Map(companyNumber -> testCrn)).value
      actual shouldBe Some(testCrn.replaceAll(" ","").toUpperCase)
    }

    "validate that data containing more than 8 digits fails" in {
      val formWithError = companyNumberForm.bind(Map(companyNumber -> "123 456 789"))
      formWithError.errors should contain(FormError(companyNumber, errorKey))
    }

    "validate that data containing fewer than 8 digits but greater than 0 passes" in {
      val testCrn = "1"
      val actual = companyNumberForm.bind(Map(companyNumber -> testCrn)).value
      actual shouldBe Some(testCrn)
    }

    "validate that 0 fails" in {
      val formWithError = companyNumberForm.bind(Map(companyNumber -> "0"))
      formWithError.errors should contain(FormError(companyNumber, errorKey))
    }
  }

}
