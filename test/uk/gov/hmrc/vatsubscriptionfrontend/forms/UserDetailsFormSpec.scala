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
import uk.gov.hmrc.vatsubscriptionfrontend.forms.UserDetailsForm._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._

class UserDetailsFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The userDetailsForm" when {

    "validating the first name" should {
      val invalid_error_key = "error.invalid_first_name"
      val max_length_error_key = "error.exceeds_max_first_name"

      "ensure the firstName exists" in {
        val actual = userDetailsForm.bind(Map(userFirstName -> "")).errors
        actual should contain(FormError(userFirstName, invalid_error_key))
      }

      "ensure the firstName does not contain illegal characters" in {
        val actual = userDetailsForm.bind(Map(userFirstName -> "α")).errors
        actual should contain(FormError(userFirstName, invalid_error_key))
      }

      "ensure the firstName does not exceeds the maxmium length" in {
        val exceed = userDetailsForm.bind(Map(userFirstName -> ("a" * (nameMaxLength + 1)))).errors
        exceed should contain(FormError(userFirstName, max_length_error_key))
        val withinLimit = userDetailsForm.bind(Map(userFirstName -> ("a" * nameMaxLength))).errors
        withinLimit should not contain FormError(userFirstName, max_length_error_key)
      }

      "ensure a valid scenario does not generate errors" in {
        val form = userDetailsForm.bind(Map(userFirstName -> ("a" * nameMaxLength)))
        form.errors(userFirstName) shouldBe Seq.empty
      }
    }

    "validating the last name" should {
      val invalid_error_key = "error.invalid_last_name"
      val max_length_error_key = "error.exceeds_max_last_name"

      "ensure the lastName exists" in {
        val actual = userDetailsForm.bind(Map(userLastName -> "")).errors
        actual should contain(FormError(userLastName, invalid_error_key))
      }

      "ensure the lastName does not contain illegal characters" in {
        val actual = userDetailsForm.bind(Map(userLastName -> "α")).errors
        actual should contain(FormError(userLastName, invalid_error_key))
      }

      "ensure the lastName does not exceeds the maxmium length" in {
        val exceed = userDetailsForm.bind(Map(userLastName -> ("a" * (nameMaxLength + 1)))).errors
        exceed should contain(FormError(userLastName, max_length_error_key))
        val withinLimit = userDetailsForm.bind(Map(userLastName -> ("a" * nameMaxLength))).errors
        withinLimit should not contain FormError(userLastName, max_length_error_key)
      }

      "ensure a valid scenario does not generate errors" in {
        val form = userDetailsForm.bind(Map(userLastName -> ("a" * nameMaxLength)))
        form.errors(userLastName) shouldBe Seq.empty
      }
    }

    "validating the nino" should {
      val invalid_error_key = "error.invalid_nino"

      "ensure the nino exists" in {
        val actual = userDetailsForm.bind(Map(userNino -> "")).errors
        actual should contain(FormError(userNino, invalid_error_key))
      }

      "ensure the nino does not contain illegal characters" in {
        val actual = userDetailsForm.bind(Map(userNino -> "α")).errors
        actual should contain(FormError(userNino, invalid_error_key))
      }
      "ensure a valid scenario does not generate errors" in {
        val form = userDetailsForm.bind(Map(userNino -> testNino))
        form.errors(userNino) shouldBe Seq.empty
      }
    }

    "validating the date of birth" should {
      val invalid_error_key = "error.invalid_dob"

      def dateMap(day: String, month: String, year: String) =
        Map(
          userDateOfBirth + ".dateDay" -> day,
          userDateOfBirth + ".dateMonth" -> month,
          userDateOfBirth + ".dateYear" -> year
        )

      "ensure the dob exists" in {
        val actual = userDetailsForm.bind(dateMap("", "", "")).errors
        actual should contain(FormError(userDateOfBirth, invalid_error_key))
      }

      "ensure the dob is a valid date" in {
        val actual = userDetailsForm.bind(dateMap("29", "2", "2018")).errors
        actual should contain(FormError(userDateOfBirth, invalid_error_key))
      }
      "ensure a valid scenario does not generate errors" in {
        val form = userDetailsForm.bind(dateMap("6", "4", "2018"))
        form.errors(userDateOfBirth) shouldBe Seq.empty
      }
    }

  }
}