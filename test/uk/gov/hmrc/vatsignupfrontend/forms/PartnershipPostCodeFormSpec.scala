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

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import uk.gov.hmrc.vatsignupfrontend.forms.PartnershipPostCodeForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testBusinessPostcode
import uk.gov.hmrc.vatsignupfrontend.models.PostCode

class PartnershipPostCodeFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The PartnershipPostCodeForm" should {

    val invalid_postcode_error_key = "error.partnership_postcode.invalid"
    val postcode_not_entered_error_key = "error.agent.partnership_postcode.not_entered"
    val invalid_characters_key = "error.partnership_postcode.invalid_characters"

    "validate that data containing a valid post code passes" in {
      val actual = partnershipPostCodeForm.bind(Map(partnershipPostCode -> testBusinessPostcode.postCode)).value
      actual shouldBe Some(PostCode(testBusinessPostcode.postCode.replaceAll(" ", "").toUpperCase()))
    }

    "validate that data starts with either 1 or 2 letters" in {
      val formWithError = partnershipPostCodeForm.bind(Map(partnershipPostCode -> "1A11AA"))
      formWithError.errors should contain(FormError(partnershipPostCode, invalid_postcode_error_key))
    }

    "validate that data ends with two letters" in {
      val formWithError = partnershipPostCodeForm.bind(Map(partnershipPostCode -> "A1111A"))
      formWithError.errors should contain(FormError(partnershipPostCode, invalid_postcode_error_key))
    }

    "validate that data is not longer than 7 alphanumeric characters" in {
      val formWithError = partnershipPostCodeForm.bind(Map(partnershipPostCode -> "A1111AAA"))
      formWithError.errors should contain(FormError(partnershipPostCode, invalid_postcode_error_key))
    }

    "validate that data is not less than 5 alphanumeric characters" in {
      val formWithError = partnershipPostCodeForm.bind(Map(partnershipPostCode -> "A1AA"))
      formWithError.errors should contain(FormError(partnershipPostCode, invalid_postcode_error_key))
    }

    "validate that data contains only alphanumeric characters" in {
      val formWithError = partnershipPostCodeForm.bind(Map(partnershipPostCode -> "!£$%^&"))
      formWithError.errors should contain(FormError(partnershipPostCode, invalid_characters_key))
    }

    "validate that data has been entered" in {
      val formWithError = partnershipPostCodeForm.bind(Map(partnershipPostCode -> ""))
      formWithError.errors should contain(FormError(partnershipPostCode, postcode_not_entered_error_key))
    }

  }
}
