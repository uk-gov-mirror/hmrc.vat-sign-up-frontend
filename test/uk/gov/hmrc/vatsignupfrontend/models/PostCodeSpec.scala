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

package uk.gov.hmrc.vatsignupfrontend.models

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstantsGenerator

class PostCodeSpec extends UnitSpec {

  private def expectedFormat(part1: String, part2: String) = {
    part1.toUpperCase.replaceAll(" ", "") + " " + part2.toUpperCase.replaceAll(" ", "")
  }

  private val part2 = TestConstantsGenerator.randomNumeric(1) + TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomAlpha(1)

  "PostCode.checkYourAnswersFormat" should {
    "format the postcode correctly" when {
      // The format is as follows, where A signifies a letter and N a digit:
      "the post code is in AN NAA format" in {
        val part1 = TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomNumeric(1)
        val testPostCode = part1 + part2
        PostCode(testPostCode).checkYourAnswersFormat shouldBe expectedFormat(part1, part2)
      }
      "the post code is in ANN NAA format" in {
        val part1 = TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomNumeric(1) + TestConstantsGenerator.randomNumeric(1)
        val testPostCode = part1 + part2
        PostCode(testPostCode).checkYourAnswersFormat shouldBe expectedFormat(part1, part2)
      }
      "the post code is in ANA NAA format" in {
        val part1 = TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomNumeric(1) + TestConstantsGenerator.randomAlpha(1)
        val testPostCode = part1 + part2
        PostCode(testPostCode).checkYourAnswersFormat shouldBe expectedFormat(part1, part2)
      }
      "the post code is in AAN NAA format" in {
        val part1 = TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomNumeric(1)
        val testPostCode = part1 + part2
        PostCode(testPostCode).checkYourAnswersFormat shouldBe expectedFormat(part1, part2)
      }
      "the post code is in AANN NAA format" in {
        val part1 = TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomNumeric(1) + TestConstantsGenerator.randomNumeric(1)
        val testPostCode = part1 + part2
        PostCode(testPostCode).checkYourAnswersFormat shouldBe expectedFormat(part1, part2)
      }
      "the post code is in AANA NAA format" in {
        val part1 = TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomAlpha(1) + TestConstantsGenerator.randomNumeric(1) + TestConstantsGenerator.randomAlpha(1)
        val testPostCode = part1 + part2
        PostCode(testPostCode).checkYourAnswersFormat shouldBe expectedFormat(part1, part2)
      }
      "the post code is in POBOX format" in {
        val part1 = "BFPO"
        val part2 = TestConstantsGenerator.randomNumeric(3)
        val testPostCode = part1 + part2
        PostCode(testPostCode).checkYourAnswersFormat shouldBe expectedFormat(part1, part2)
      }
    }
  }

}
