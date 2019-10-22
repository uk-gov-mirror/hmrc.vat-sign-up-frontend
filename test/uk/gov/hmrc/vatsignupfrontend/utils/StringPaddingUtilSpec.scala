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

package uk.gov.hmrc.vatsignupfrontend.utils

import uk.gov.hmrc.play.test.UnitSpec

class StringPaddingUtilSpec extends UnitSpec {

  private val zero = "0"
  private val maxLength = 8
  implicit private val config: PadConfig = PadConfig(maxLength, zero)

  object TestStringPaddingUtil extends StringPaddingUtil

  "leftPad" when {
    "using implicit config" when {
      "the length of the input string is 8" should {
        "return the input string, without padding" in {
          val testString = "12345678"
          val result = TestStringPaddingUtil.leftPad(testString)

          result shouldBe testString
        }
      }
      "the length of the input string is 7" should {
        "return the input string, padded with 1 leading zero" in {
          val testString = "1234567"
          val result = TestStringPaddingUtil.leftPad(testString)

          result shouldBe "01234567"
        }
      }
      "the length of the input string is 6" should {
        "return the input string, padded with 2 leading zeroes" in {
          val testString = "123456"
          val result = TestStringPaddingUtil.leftPad(testString)

          result shouldBe "00123456"
        }
      }
      "the length of the input string is 5" should {
        "return the input string, padded with 3 leading zeroes" in {
          val testString = "12345"
          val result = TestStringPaddingUtil.leftPad(testString)

          result shouldBe "00012345"
        }
      }
      "the length of the input string is greater than the max length" should {
        "return the input string" in {
          val testString = "123456789"
          val result = TestStringPaddingUtil.leftPad(testString)

          result shouldBe "123456789"
        }
      }
    }
    "setting the parameters" when {
      "the length of the input string is 8" should {
        "return the input string, without padding" in {
          val testString = "12345678"
          val result = TestStringPaddingUtil.leftPad(testString, maxLength, zero)

          result shouldBe testString
        }
      }
      "the length of the input string is 7" should {
        "return the input string, padded with 1 leading zero" in {
          val testString = "1234567"
          val result = TestStringPaddingUtil.leftPad(testString, maxLength, zero)

          result shouldBe "01234567"
        }
      }
      "the length of the input string is 6" should {
        "return the input string, padded with 2 leading zeroes" in {
          val testString = "123456"
          val result = TestStringPaddingUtil.leftPad(testString, maxLength, zero)

          result shouldBe "00123456"
        }
      }
      "the length of the input string is 5" should {
        "return the input string, padded with 3 leading zeroes" in {
          val testString = "12345"
          val result = TestStringPaddingUtil.leftPad(testString, maxLength, zero)

          result shouldBe "00012345"
        }
      }
      "the length of the input string is greater than the max length" should {
        "return the input string" in {
          val testString = "123456789"
          val result = TestStringPaddingUtil.leftPad(testString, maxLength, zero)

          result shouldBe "123456789"
        }
      }
    }
  }

  "rightPad" when {
    "using the implicit config" when {
      "the length of the input string is 8" should {
        "return the input string, without padding" in {
          val testString = "12345678"
          val result = TestStringPaddingUtil.rightPad(testString)

          result shouldBe testString
        }
      }
      "the length of the input string is 7" should {
        "return the input string, padded with 1 leading zero" in {
          val testString = "1234567"
          val result = TestStringPaddingUtil.rightPad(testString)

          result shouldBe "12345670"
        }
      }
      "the length of the input string is 6" should {
        "return the input string, padded with 2 leading zeroes" in {
          val testString = "123456"
          val result = TestStringPaddingUtil.rightPad(testString)

          result shouldBe "12345600"
        }
      }
      "the length of the input string is 5" should {
        "return the input string, padded with 3 leading zeroes" in {
          val testString = "12345"
          val result = TestStringPaddingUtil.rightPad(testString)

          result shouldBe "12345000"
        }
      }
      "the length of the input string is greater than the max length" should {
        "return the input string" in {
          val testString = "123456789"
          val result = TestStringPaddingUtil.rightPad(testString)

          result shouldBe "123456789"
        }
      }
    }
    "setting the paramenters" when {
      "the length of the input string is 8" should {
        "return the input string, without padding" in {
          val testString = "12345678"
          val result = TestStringPaddingUtil.rightPad(testString, maxLength, zero)

          result shouldBe testString
        }
      }
      "the length of the input string is 7" should {
        "return the input string, padded with 1 leading zero" in {
          val testString = "1234567"
          val result = TestStringPaddingUtil.rightPad(testString, maxLength, zero)

          result shouldBe "12345670"
        }
      }
      "the length of the input string is 6" should {
        "return the input string, padded with 2 leading zeroes" in {
          val testString = "123456"
          val result = TestStringPaddingUtil.rightPad(testString, maxLength, zero)

          result shouldBe "12345600"
        }
      }
      "the length of the input string is 5" should {
        "return the input string, padded with 3 leading zeroes" in {
          val testString = "12345"
          val result = TestStringPaddingUtil.rightPad(testString, maxLength, zero)

          result shouldBe "12345000"
        }
      }
      "the length of the input string is greater than the max length" should {
        "return the input string" in {
          val testString = "123456789"
          val result = TestStringPaddingUtil.rightPad(testString, maxLength, zero)

          result shouldBe "123456789"
        }
      }
    }
  }

}
