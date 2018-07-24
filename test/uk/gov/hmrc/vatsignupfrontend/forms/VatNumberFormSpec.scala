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
import uk.gov.hmrc.vatsignupfrontend.forms.VatNumberForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testVatNumber

class VatNumberFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The vatNumberForm" should {
    val individual_vrn_not_entered_error_key = "error.principal.no_vat_number_entered"
    val agent_vrn_not_entered_error_key = "error.agent.no_vat_number_entered"
    val wrong_vrn_length_error_key = "error.invalid_vat_number_length"
    val invalid_vrn_entered_error_key = "error.invalid_vat_number"
    val validationForm = vatNumberForm(isAgent = false)

    "validate that data containing 9 digits passes" in {
      val actual = validationForm.bind(Map(vatNumber -> testVatNumber)).value
      actual shouldBe Some(testVatNumber)
    }

    "validate that data has been entered on the agent form" in {
      val validationForm = vatNumberForm(isAgent = true)
      val formWithError = validationForm.bind(Map(vatNumber -> ""))
      formWithError.errors should contain(FormError(vatNumber, agent_vrn_not_entered_error_key))
    }

    "validate that data has been entered on the principal form" in {
      val formWithError = validationForm.bind(Map(vatNumber -> ""))
      formWithError.errors should contain(FormError(vatNumber, individual_vrn_not_entered_error_key))
    }

    "validate that data containing any non numeric data fails" in {
      val formWithError = validationForm.bind(Map(vatNumber -> (testVatNumber.drop(1) + "A")))
      formWithError.errors should contain(FormError(vatNumber, invalid_vrn_entered_error_key))
    }

    "validate that data containing more than 9 digits fails" in {
      val formWithError = validationForm.bind(Map(vatNumber -> (testVatNumber + "1")))
      formWithError.errors should contain(FormError(vatNumber, wrong_vrn_length_error_key))
    }

    "validate that data containing less than 9 digits fails" in {
      val formWithError = validationForm.bind(Map(vatNumber -> testVatNumber.drop(1)))
      formWithError.errors should contain(FormError(vatNumber, wrong_vrn_length_error_key))
    }

  }
}