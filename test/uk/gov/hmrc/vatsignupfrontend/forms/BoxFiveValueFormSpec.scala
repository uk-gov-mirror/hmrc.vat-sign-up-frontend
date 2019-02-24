/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.vatsignupfrontend.forms.BoxFiveValueForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testBoxFiveValue

class BoxFiveValueFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The boxFiveValueForm" should {
    val not_entered_error_key = "error.principal.box_five_value_not_entered"
    val invalid_format_error_key = "error.principal.box_five_value_invalid_format"
    val invalid_length_error_key = "error.principal.box_five_value_invalid_length"

    "validate that valid data within the character limit passes" in {
      val actual = boxFiveValueForm.bind(Map(boxFiveValue -> testBoxFiveValue)).value
      actual shouldBe Some(testBoxFiveValue)
    }

    "validate that valid data outside the character limit passes" in {
      val outsideLimitNum = "123456789012.00"
      val formWithError = boxFiveValueForm.bind(Map(boxFiveValue -> outsideLimitNum))
      formWithError.errors should contain(FormError(boxFiveValue, invalid_length_error_key))
    }

    "validate that data has been entered in the form" in {
      val formWithError = boxFiveValueForm.bind(Map(boxFiveValue -> ""))
      formWithError.errors should contain(FormError(boxFiveValue, not_entered_error_key))
    }

    "validate that data containing any non numeric data fails" in {
      val formWithError = boxFiveValueForm.bind(Map(boxFiveValue -> (testBoxFiveValue.drop(1) + "A")))
      formWithError.errors should contain(FormError(boxFiveValue, invalid_format_error_key))
    }

  }
}