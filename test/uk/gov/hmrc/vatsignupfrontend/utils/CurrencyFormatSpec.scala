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

import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec


class CurrencyFormatSpec extends UnitSpec with MockitoSugar {

  import StringUtils._

  "StringUtils.currencyFormat" should {
    "return 1.00 for an input of 12.00" in {
      val testValue = "1.00"
      testValue.currencyFormat() shouldBe "1.00"
    }

    "return 12.00 for an input of 12.00" in {
      val testValue = "12.00"
      testValue.currencyFormat() shouldBe "12.00"
    }

    "return 123.00 for an input of 123.00" in {
      val testValue = "123.00"
      testValue.currencyFormat() shouldBe "123.00"
    }

    "return 1,234.00 for an input of 1234.00" in {
       val testValue = "1234.00"
      testValue.currencyFormat() shouldBe "1,234.00"
    }

    "return 12,345.00 for an input of 12345.00" in {
      val testValue = "12345.00"
      testValue.currencyFormat() shouldBe "12,345.00"
    }

    "return 123,456.00 for an input of 123456.00" in {
      val testValue = "123456.00"
      testValue.currencyFormat() shouldBe "123,456.00"
    }

    "return 1,234,567.00 for an input of 1234567.00" in {
      val testValue = "1234567.00"
      testValue.currencyFormat() shouldBe "1,234,567.00"
    }

    "return 12,345,678.00 for an input of 12345678.00" in {
      val testValue = "12345678.00"
      testValue.currencyFormat() shouldBe "12,345,678.00"
    }
  }

}
