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
import uk.gov.hmrc.vatsignupfrontend.forms.UserDetailsForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class UserDetailsFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The userDetailsForm" when {

    val validateUserDetailsForm = userDetailsForm(isAgent = false)

    "validating the first name" should {

      val principal_first_name_no_entry_error_key = "error.principal.no_entry_first_name"
      val agent_first_name_no_entry_error_key = "error.agent.no_entry_first_name"
      val first_name_max_length_error_key = "error.exceeds_max_first_name"
      val first_name_invalid_error_key = "error.invalid_first_name"

      "ensure the firstName exists - agent" in {
       val validateUserDetailsForm = userDetailsForm(isAgent = true)
        val actual = validateUserDetailsForm.bind(Map(userFirstName -> "")).errors
        actual should contain(FormError(userFirstName, agent_first_name_no_entry_error_key))
      }

      "ensure the firstName exists - principal" in {
        val actual = validateUserDetailsForm.bind(Map(userFirstName -> "")).errors
        actual should contain(FormError(userFirstName, principal_first_name_no_entry_error_key))
      }

      "ensure the firstName does not contain illegal characters" in {
        val actual = validateUserDetailsForm.bind(Map(userFirstName -> "α")).errors
        actual should contain(FormError(userFirstName, first_name_invalid_error_key))
      }

      "ensure the firstName does not exceeds the maximum length" in {
        val exceed = validateUserDetailsForm.bind(Map(userFirstName -> ("a" * (nameMaxLength + 1)))).errors
        exceed should contain(FormError(userFirstName, first_name_max_length_error_key))
        val withinLimit = validateUserDetailsForm.bind(Map(userFirstName -> ("a" * nameMaxLength))).errors
        withinLimit should not contain FormError(userFirstName, first_name_max_length_error_key)
      }

      "ensure a valid scenario does not generate errors" in {
        val form = validateUserDetailsForm.bind(Map(userFirstName -> ("a" * nameMaxLength)))
        form.errors(userFirstName) shouldBe Seq.empty
      }
    }

    "validating the last name" should {

      val principal_last_name_no_entry_error_key = "error.principal.no_entry_last_name"
      val agent_last_name_no_entry_error_key = "error.agent.no_entry_last_name"
      val last_name_max_length_error_key = "error.exceeds_max_last_name"
      val last_name_invalid_error_key = "error.invalid_last_name"

      "ensure the lastName exists - agent" in {
        val validateUserDetailsForm = userDetailsForm(isAgent = true)
        val actual = validateUserDetailsForm.bind(Map(userLastName -> "")).errors
        actual should contain(FormError(userLastName, agent_last_name_no_entry_error_key))
      }

      "ensure the lastName exists - principal" in {
        val actual = validateUserDetailsForm.bind(Map(userLastName -> "")).errors
        actual should contain(FormError(userLastName, principal_last_name_no_entry_error_key))
      }

      "ensure the lastName does not contain illegal characters" in {
        val actual = validateUserDetailsForm.bind(Map(userLastName -> "α")).errors
        actual should contain(FormError(userLastName, last_name_invalid_error_key))
      }

      "ensure the lastName does not exceeds the maximum length" in {
        val exceed = validateUserDetailsForm.bind(Map(userLastName -> ("a" * (nameMaxLength + 1)))).errors
        exceed should contain(FormError(userLastName, last_name_max_length_error_key))
        val withinLimit = validateUserDetailsForm.bind(Map(userLastName -> ("a" * nameMaxLength))).errors
        withinLimit should not contain FormError(userLastName, last_name_max_length_error_key)
      }

      "ensure a valid scenario does not generate errors" in {
        val form = validateUserDetailsForm.bind(Map(userLastName -> ("a" * nameMaxLength)))
        form.errors(userLastName) shouldBe Seq.empty
      }
    }

    "validating the nino" should {

      val principal_nino_no_entry_error_key = "error.principal.no_entry_nino"
      val agent_nino_no_entry_error_key = "error.agent.no_entry_nino"
      val nino_invalid_error_key = "error.invalid_nino"
      val nino_max_length_error_key = "error.character_limit_nino"

      "ensure the nino exists - principal" in {
        val actual = validateUserDetailsForm.bind(Map(userNino -> "")).errors
        actual should contain(FormError(userNino, principal_nino_no_entry_error_key))
      }

      "ensure the nino exists - agent" in {
        val validateUserDetailsForm = userDetailsForm(isAgent = true)
        val actual = validateUserDetailsForm.bind(Map(userNino -> "")).errors
        actual should contain(FormError(userNino, agent_nino_no_entry_error_key))
      }

      "ensure the nino contains 9 characters" in {
        val actual = validateUserDetailsForm.bind(Map(userNino -> testNino.drop(1))).errors
        actual should contain(FormError(userNino, nino_max_length_error_key))
      }

      "ensure the nino does not contain illegal characters" in {
        val actual = validateUserDetailsForm.bind(Map(userNino -> (testNino.dropRight(3) + "ABC"))).errors
        actual should contain(FormError(userNino, nino_invalid_error_key))
      }

      "ensure a valid scenario does not generate errors" in {
        val form = validateUserDetailsForm.bind(Map(userNino -> testNino))
        form.errors(userNino) shouldBe Seq.empty
      }
    }

    "validating the date of birth" should {

      val principal_dob_no_entry_error_key = "error.principal.no_entry_dob"
      val agent_dob_no_entry_error_key = "error.agent.no_entry_dob"
      val dob_invalid_characters_error_key = "error.invalid_characters_dob"
      val dob_invalid_error_key = "error.invalid_dob"

      def dateMap(day: String, month: String, year: String) =
        Map(
          userDateOfBirth + ".dateDay" -> day,
          userDateOfBirth + ".dateMonth" -> month,
          userDateOfBirth + ".dateYear" -> year
        )

      "ensure the dob exists - principal" in {
        val actual = validateUserDetailsForm.bind(dateMap("", "", "")).errors
        actual should contain(FormError(userDateOfBirth, principal_dob_no_entry_error_key))
      }

      "ensure the dob exists - agent" in {
        val validateUserDetailsForm = userDetailsForm(isAgent = true)
        val actual = validateUserDetailsForm.bind(dateMap("", "", "")).errors
        actual should contain(FormError(userDateOfBirth, agent_dob_no_entry_error_key))
      }

      "ensure the dob is a valid date" in {
        val actual = validateUserDetailsForm.bind(dateMap("29", "2", "2018")).errors
        actual should contain(FormError(userDateOfBirth, dob_invalid_error_key))
      }

      "ensure a valid scenario does not generate errors" in {
        val form = validateUserDetailsForm.bind(dateMap("6", "4", "2018"))
        form.errors(userDateOfBirth) shouldBe Seq.empty
      }

      "ensure the dob does not contain invalid characters" in {
        val actual = validateUserDetailsForm.bind(dateMap("A", "!", "@")).errors
        actual should contain(FormError(userDateOfBirth, dob_invalid_characters_error_key))
      }
    }

  }
}