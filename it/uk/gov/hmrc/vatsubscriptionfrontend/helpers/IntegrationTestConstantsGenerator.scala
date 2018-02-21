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


object IntegrationTestConstantsGenerator {

  private val rand = new Random()

  private val UPPER_BOUND_9_DIGIT_NUMBER = 100000000
  private val UPPER_BOUND_8_DIGIT_NUMBER = 10000000
  private val UPPER_BOUND_7_DIGIT_NUMBER = 1000000
  private val UPPER_BOUND_6_DIGIT_NUMBER = 100000

  def randomVatNumber: String = "%09d".format(rand.nextInt(UPPER_BOUND_9_DIGIT_NUMBER))

  def randomCrn: String = "%08d".format(rand.nextInt(UPPER_BOUND_8_DIGIT_NUMBER))
}
