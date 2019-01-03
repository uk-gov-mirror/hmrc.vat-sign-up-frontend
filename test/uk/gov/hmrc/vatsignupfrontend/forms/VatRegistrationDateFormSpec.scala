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

import java.time.LocalDate

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import uk.gov.hmrc.vatsignupfrontend.forms.VatRegistrationDateForm._

class VatRegistrationDateFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The vatRegistrationDateForm" when {

    "validating the vat registration date" should {
      val no_date_entered_error_key = "error.no_vat_registration_date_entered"
      val invalid_characters_error_key = "error.invalid_vat_registration_date_characters"
      val invalid_date_error_key = "error.invalid_vat_registration_date"
      val date_in_future_key = "error.vat_registration_date_future"

      def dateMap(day: String, month: String, year: String) =
        Map(
          vatRegistrationDate + ".dateDay" -> day,
          vatRegistrationDate + ".dateMonth" -> month,
          vatRegistrationDate + ".dateYear" -> year
        )

      "ensure the vat registration date exists" in {
        val actual = vatRegistrationDateForm.bind(Map.empty[String, String]).errors
        actual should contain(FormError(vatRegistrationDate, no_date_entered_error_key))
      }

      "ensure the vat registration date is in the past" in {
        val today = LocalDate.now()
        val today_day = today.getDayOfMonth.toString
        val today_month = today.getMonthValue.toString
        val today_year = today.getYear.toString

        val actual = vatRegistrationDateForm.bind(dateMap(today_day, today_month, today_year)).errors
        actual should contain(FormError(vatRegistrationDate, date_in_future_key))
      }

      "ensure the vat registration date is a valid date" in {
        val actual = vatRegistrationDateForm.bind(dateMap("29", "2", "2018")).errors
        actual should contain(FormError(vatRegistrationDate, invalid_date_error_key))
      }

      "ensure the vat registration date contains valid characters" in {
        val actual = vatRegistrationDateForm.bind(dateMap("A", "!", "b")).errors
        actual should contain(FormError(vatRegistrationDate, invalid_characters_error_key))
      }

      "ensure a valid date scenario does not generate errors" in {
        val form = vatRegistrationDateForm.bind(dateMap("6", "4", "2018"))
        form.errors(vatRegistrationDate) shouldBe Seq.empty
      }
    }

  }
}