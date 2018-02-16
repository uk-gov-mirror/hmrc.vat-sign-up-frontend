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
import uk.gov.hmrc.vatsubscriptionfrontend.forms.VatNumberForm._

class VatNumberFormSpec extends PlaySpec with GuiceOneAppPerSuite{

  "The vatNumberForm" should {

    val error_key = "error.invalid_vat_number"

    "validate that data containing 9 digits passes" in {
      val successVatNumber = "123456789"
      val actual = vatNumberForm.bind(Map(vatNumber -> successVatNumber)).value
      actual shouldBe Some(successVatNumber)
    }

    "validate that data has been entered" in {
      val formWithError = vatNumberForm.bind(Map(vatNumber -> ""))
      formWithError.errors should contain(FormError(vatNumber, error_key))
    }

    "validate that data containing any non numeric data fails" in {
      val formWithError = vatNumberForm.bind(Map(vatNumber -> "12345678A"))
      formWithError.errors should contain(FormError(vatNumber, error_key))
    }

    "validate that data containing more than 9 digits fails" in {
      val formWithError = vatNumberForm.bind(Map(vatNumber -> "1234567890"))
      formWithError.errors should contain (FormError(vatNumber, error_key))
    }

    "validate that data containing less than 9 digits fails" in {
      val formWithError = vatNumberForm.bind(Map(vatNumber -> "12345678"))
      formWithError.errors should contain(FormError(vatNumber, error_key))
    }
  }

}