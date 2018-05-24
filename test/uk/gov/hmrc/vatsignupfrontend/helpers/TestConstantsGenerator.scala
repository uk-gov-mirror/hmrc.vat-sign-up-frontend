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

package uk.gov.hmrc.vatsignupfrontend.helpers

import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.CompanyNumber._

import scala.util.Random

object TestConstantsGenerator {

  private val rand = new Random()

  private val UPPER_BOUND_9_DIGIT_NUMBER = 1000000000
  private val UPPER_BOUND_8_DIGIT_NUMBER = 99999999
  private val UPPER_BOUND_7_DIGIT_NUMBER = 9999999
  private val UPPER_BOUND_6_DIGIT_NUMBER = 999999

  def randomVatNumber: String = "%09d".format(rand.nextInt(UPPER_BOUND_9_DIGIT_NUMBER))

  def randomCrn: String = rand.nextInt(2) match {
    case 0 => randomCrnNumeric
    case 1 => randomCrnAlphaNumeric
    case 2 => randomCrnNumericNoLeadingZeros
  }

  private lazy val prefixes = validCompanyNumberPrefixes.toList

  def randomPrefix: String = prefixes(rand.nextInt(prefixes.size)).toLowerCase

  def randomCrnNumeric: String = "%08d".format(rand.nextInt(UPPER_BOUND_8_DIGIT_NUMBER) + 1)

  def randomCrnNumericNoLeadingZeros: String = "%8d".format(rand.nextInt(UPPER_BOUND_7_DIGIT_NUMBER) + 1)

  def randomCrnAlphaNumeric: String = randomPrefix + "%06d".format(rand.nextInt(UPPER_BOUND_6_DIGIT_NUMBER) + 1)

  private def randomString(alphabet: String)(max: Int): String =
    Stream.continually(rand.nextInt(alphabet.length)).map(alphabet).take(rand.nextInt(max) + 1).mkString

  def randomAlpha(max: Int): String =
    randomString(('a' to 'z').mkString("") + ('A' to 'Z').mkString(""))(max)

  def randomNumeric(max: Int): String =
    randomString(('0' to '9').mkString(""))(max)

  private def randomAlphaNumericWithAdditional(additionalChars: String)(max: Int): String =
    randomString(('a' to 'z').mkString("") + ('A' to 'Z').mkString("") + ('0' to '9').mkString("") + additionalChars)(max)

  def randomEmail: String =
    randomAlphaNumericWithAdditional(additionalChars = "_.+-")(rand.nextInt(16) + 1) +
      "@" + randomAlphaNumericWithAdditional(additionalChars = "-")(rand.nextInt(10) + 1) +
      "." + {
      (0 to rand.nextInt(2)).map(_ => randomAlphaNumericWithAdditional(additionalChars = "-")(rand.nextInt(3) + 1)).mkString(".")
    }

  def randomPostCode: String =
    randomAlpha(2) +
      randomNumeric(1) + randomNumeric(1) + // must have 2 numeric
      randomAlpha(1) + randomAlpha(1) // must end with 2 alphas
}
