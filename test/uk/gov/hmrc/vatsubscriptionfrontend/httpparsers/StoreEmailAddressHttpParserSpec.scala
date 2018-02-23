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

package uk.gov.hmrc.vatsubscriptionfrontend.httpparsers

import play.api.http.Status.{BAD_REQUEST, NO_CONTENT}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.StoreEmailAddressHttpParser.StoreEmailAddressHttpReads
import uk.gov.hmrc.vatsubscriptionfrontend.models.{StoreEmailAddressFailure, StoreEmailAddressSuccess}

class StoreEmailAddressHttpParserSpec extends UnitSpec {
  val testHttpVerb = "PUT"
  val testUri = "/"

  "StoreEmailAddressHttpReads" when {
    "read" should {
      "parse a NO_CONTENT response as an UpsertEnrolmentSuccess" in {
        val httpResponse = HttpResponse(NO_CONTENT)

        val res = StoreEmailAddressHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Right(StoreEmailAddressSuccess)
      }
      "parse any other response as an StoreEmailAddressFailure" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.obj()))

        val res = StoreEmailAddressHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(StoreEmailAddressFailure(httpResponse.status))
      }
    }
  }
}
