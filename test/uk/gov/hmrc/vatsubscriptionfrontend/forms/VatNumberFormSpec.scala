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

package uk.gov.hmrc.vatsubscriptionfrontend.forms.validation.testutils

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.vatsubscriptionfrontend.models.VatNumber
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class VatNumberFormSpec extends PlaySpec with GuiceOneAppPerSuite{

  import uk.gov.hmrc.vatsubscriptionfrontend.forms.VatNumberForm._
  import assets.MessageLookup.ErrorMessage._

  "The vatNumberForm" should {

    def errorMessage(value: String): String = {
      val formWithError = vatNumberForm.bind(Map(vrn -> value))
      formWithError.error(vrn).get.message
    }

    "transform the data to the case class" in {
      val testVrn = "123456789"
      val testInput = Map(vrn -> testVrn)
      val expected = VatNumber(testVrn)
      val actual = vatNumberForm.bind(testInput).value
      actual shouldBe Some(expected)
    }

    "validate that data has been entered" in {
      errorMessage("") shouldBe invalidVrn
    }

    "validate that data containing any non numeric data fails" in {
      errorMessage("12345678A") shouldBe invalidVrn
    }

    "validate that data containing more than 9 digits fails" in {
      errorMessage("12345678910") shouldBe invalidVrn
    }

    "validate that data containing less than 9 digits fails" in {
      errorMessage("12345678") shouldBe invalidVrn
    }

    "validate that data containing 9 digits passes" in {
      vatNumberForm.bind(Map(vrn -> "123456789")).hasErrors shouldBe false
    }

  }

}