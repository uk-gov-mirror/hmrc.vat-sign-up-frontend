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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreEmailAddressHttpParser._
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

class StoreEmailAddressHttpParserSpec extends UnitSpec {
  val testHttpVerb = "PUT"
  val testUri = "/"

  "StoreEmailAddressHttpReads" when {
    "reading from the old API" should {
      "parse a OK response as an StoreEmailAddressSuccess" in {
        val httpResponse = HttpResponse(OK, Some(Json.obj(EmailVerifiedKey -> true)))

        val res = StoreEmailAddressHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Right(StoreEmailAddressSuccess(true))
      }
      "parse any other response as an StoreEmailAddressFailure" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Some(Json.obj()))

        val res = StoreEmailAddressHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(StoreEmailAddressFailureStatus(httpResponse.status))
      }
    }
  }
}
