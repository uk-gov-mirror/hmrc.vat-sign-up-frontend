/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{JsNumber, JsString, JsValue}
import uk.gov.hmrc.play.test.UnitSpec

class ContactPreferenceSpec extends UnitSpec {

  "ContactPreferenceJsonReads" should {
    "return contact preference from valid json (paper)" in {
      val json: JsValue = JsString("Paper")
      json.as[ContactPreference](ContactPreference.jsonReads) shouldBe Paper
    }
    "return contact preference from valid json (digital)" in {
      val json: JsValue = JsString("Digital")
      json.as[ContactPreference](ContactPreference.jsonReads) shouldBe Digital
    }
    "return a exception when type is not a string" in {
      val json: JsValue = JsNumber(123)
      intercept[Exception](json.as[ContactPreference](ContactPreference.jsonReads))
    }
    "return a exception when string is not a ContactPreference constant" in {
      val json: JsValue = JsString("foo bar wizz")
      json.validate[ContactPreference](ContactPreference.jsonReads).asEither.left.get.head._2.head.message shouldBe "Is not a valid ContactPreference"
    }
  }
}
