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

import org.scalatest.EitherValues
import play.api.http.Status.{BAD_REQUEST, CREATED, FORBIDDEN}
import play.api.libs.json.Json
import play.api.libs.openid.Errors.BAD_RESPONSE
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.Constants.{StoreVatNumberNoRelationshipCodeKey, StoreVatNumberNoRelationshipCodeValue}
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.StoreVatNumberHttpParser.StoreVatNumberHttpReads
import uk.gov.hmrc.vatsubscriptionfrontend.models.{StoreVatNumberFailureResponse, StoreVatNumberNoRelationship, StoreVatNumberSuccess}

class StoreVatNumberHttpParserSpec extends UnitSpec with EitherValues {
  val testHttpVerb = "PUT"
  val testUri = "/"

  "StoreVatNumberHttpReads" when {
    "read" should {
      "parse a CREATED response as an StoreVatNumberSuccess" in {
        val httpResponse = HttpResponse(CREATED)

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.right.value shouldBe StoreVatNumberSuccess
      }

      "parse a FORBIDDEN response as an StoreVatNumberNoRelationship when the response code matches" in {
        val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(StoreVatNumberNoRelationshipCodeKey -> StoreVatNumberNoRelationshipCodeValue)))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberNoRelationship
      }

      "parse a FORBIDDEN response as a StoreVatNumberFailureResponse when the response code does not match" in {
        val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(StoreVatNumberNoRelationshipCodeKey -> "INCORRECT_CODE")))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberFailureResponse(FORBIDDEN)
      }

      "parse any other response as a StoreVatNumberFailureResponse" in {
        val httpResponse = HttpResponse(BAD_REQUEST)

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberFailureResponse(BAD_REQUEST)
      }

    }
  }
}
