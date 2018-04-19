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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessPostCodeForm._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants.testBusinessPostCode

class BusinessPostCodeFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The BusinessPostCodeForm" should {

    val error_key = "error.invalid_postcode"

    "validate that data containing a valid post code passes" in {
      val actual = businessPostCodeForm.bind(Map(businessPostCode -> testBusinessPostCode)).value
      actual shouldBe Some(testBusinessPostCode)
    }

    "validate that data starts with either 1 or 2 letters" in {
      val formWithError = businessPostCodeForm.bind(Map(businessPostCode -> "1A11AA"))
      formWithError.errors should contain(FormError(businessPostCode, error_key))
    }

    "validate that data ends with two letters" in {
      val formWithError = businessPostCodeForm.bind(Map(businessPostCode -> "A1111A"))
      formWithError.errors should contain(FormError(businessPostCode, error_key))
    }

    "validate that data is not longer than 7 alphanumeric characters" in {
      val formWithError = businessPostCodeForm.bind(Map(businessPostCode -> "A1111AAA"))
      formWithError.errors should contain(FormError(businessPostCode, error_key))
    }

    "validate that data is not less than 5 alphanumeric characters" in {
      val formWithError = businessPostCodeForm.bind(Map(businessPostCode -> "A1AA"))
        formWithError.errors should contain(FormError(businessPostCode, error_key))
    }

    "validate that data has been entered" in {
      val formWithError = businessPostCodeForm.bind(Map(businessPostCode -> ""))
      formWithError.errors should contain(FormError(businessPostCode, error_key))
    }

  }
}