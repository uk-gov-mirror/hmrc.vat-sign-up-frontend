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

package uk.gov.hmrc.vatsignupfrontend.forms

import uk.gov.hmrc.vatsignupfrontend.forms.{EmailPasscodeForm => form}
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

class EmailPasscodeFormSpec extends UnitSpec {

  val fieldName = "verificationCode"

  def findErrors(testData: Seq[String]): Seq[String] =
    testData.flatMap(value => form().bind(Map(fieldName -> value)).errors.headOption.map(_.message))

  "the email verification code form" should {
    "return an error" when {
      "the code field is empty" in {
        val res = form().bind(Map(fieldName -> ""))
        res.errors.headOption.map(_.message) shouldBe Some("principal.capture_passcode.error.length")
        res.errors.size shouldBe 1
      }
      "the field only contains whitespace characters" in {
        val res = form().bind(Map(fieldName -> " "))

        res.errors.headOption.map(_.message) shouldBe Some("principal.capture_passcode.error.length")
        res.errors.size shouldBe 1
      }
      "the code entered is longer than 6 characters long" in {
        val res = form().bind(Map(fieldName -> "1234567"))
        res.errors.headOption.map(_.message) shouldBe Some("principal.capture_passcode.error.length")
        res.errors.size shouldBe 1
      }
    }
    "bind successfully" when {
      "a valid value is provided" in {
        val testData = Seq(
          "123456",
          "90876",
          "ABFEDH",
          "4DfJ3A"
        )

        val res = findErrors(testData)

        res.flatten.size shouldBe 0
      }
    }
  }

}