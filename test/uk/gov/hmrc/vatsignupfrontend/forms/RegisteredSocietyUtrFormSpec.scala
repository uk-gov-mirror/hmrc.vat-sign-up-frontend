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
import play.api.data.FormError
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstantsGenerator

class RegisteredSocietyUtrFormSpec extends PlaySpec {

  import uk.gov.hmrc.vatsignupfrontend.forms.RegisteredSocietyUtrForm._

  "The registeredSocietyUtrForm" should {

    val no_entry_error_key = "error.no_entry_registered_society_utr"
    val exceeds_characters_limit_error_key = "error.character_limit_registered_society_utr"
    val invalid_characters_error_key = "error.invalid_registered_society_utr"

    "validate that data containing 10 digits passes" in {
      val testUtr = TestConstantsGenerator.randomUTRNumeric()
      val actual = registeredSocietyUtrForm.bind(Map(registeredSocietyUtr -> testUtr)).value
      actual shouldBe Some(testUtr)
    }

    "validate that no data has been entered" in {
      val formWithError = registeredSocietyUtrForm.bind(Map(registeredSocietyUtr -> ""))
      formWithError.errors should contain(FormError(registeredSocietyUtr, no_entry_error_key))
    }

    "validate that data containing incorrect format of non numeric data fails" in {
      val formWithError = registeredSocietyUtrForm.bind(Map(registeredSocietyUtr -> "A2345A789A"))
      formWithError.errors should contain(FormError(registeredSocietyUtr, invalid_characters_error_key))
    }

    "validate that data containing more than 10 digits fails" in {
      val formWithError = registeredSocietyUtrForm.bind(Map(registeredSocietyUtr -> "123 456 789 10"))
      formWithError.errors should contain(FormError(registeredSocietyUtr, exceeds_characters_limit_error_key))
    }

    "validate that data containing fewer than 10 digits but greater than 0 fails" in {
      val testUtr = "1"
      val formWithError = registeredSocietyUtrForm.bind(Map(registeredSocietyUtr -> testUtr))
      formWithError.errors should contain(FormError(registeredSocietyUtr, exceeds_characters_limit_error_key))
    }

  }

}
