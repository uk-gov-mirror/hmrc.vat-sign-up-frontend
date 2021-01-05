/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.utils

import org.scalatestplus.mockito.MockitoSugar

class VatNumberChecksumValidationSpec extends UnitSpec with MockitoSugar {

  "VatNumberChecksumValidation.isValid" should {
    "return true if the checksum is correct for a mod 97 number" in {
      val testValue = "011000084"
      VatNumberChecksumValidation.isValidChecksum(testValue) shouldBe true
    }

    "return true if the checksum is correct for a mod 9755 number" in {
      val testValue = "011000029"
      VatNumberChecksumValidation.isValidChecksum(testValue) shouldBe true
    }

    "return false if the checksum is incorrect" in {
      val testValue = "999999999"
      VatNumberChecksumValidation.isValidChecksum(testValue) shouldBe false
    }
  }

}
