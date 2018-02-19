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

package uk.gov.hmrc.vatsubscriptionfrontend.helpers

import scala.util.Random


object TestConstantsGenerator {

  private val rand = new Random()

  private val MIN_10_DIGIT_NUMBER = 100000000
  private val MIN_9_DIGIT_NUMBER = 10000000
  private val MIN_8_DIGIT_NUMBER = 1000000
  private val MIN_7_DIGIT_NUMBER = 100000

  def randomVatNumber: String = "%09d".format(rand.nextInt(MIN_10_DIGIT_NUMBER))

  def randomCrn: String = rand.nextInt(3) match {
    case 0 => randomCrnNumeric
    case 1 => randomCrnNumericNoLeadingZeros
    case 2 => randomCrnAlphaNumeric
  }

  def randomCrnNumeric: String = "%08d".format(rand.nextInt(MIN_9_DIGIT_NUMBER))

  def randomCrnNumericNoLeadingZeros: String = "%7d".format(rand.nextInt(MIN_8_DIGIT_NUMBER))

  // todo
  def randomCrnAlphaNumeric: String = "SC" + "%06d".format(rand.nextInt(MIN_7_DIGIT_NUMBER))
}
