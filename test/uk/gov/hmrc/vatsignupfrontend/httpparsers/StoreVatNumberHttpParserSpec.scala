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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import org.scalatest.EitherValues
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.Constants.{StoreVatNumberKnownFactsMismatchCodeValue, StoreVatNumberNoRelationshipCodeKey, StoreVatNumberNoRelationshipCodeValue}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreVatNumberHttpParser._

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

      "parse a FORBIDDEN response as a NoAgentClientRelationship when the response code matches" in {
        val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(StoreVatNumberNoRelationshipCodeKey -> StoreVatNumberNoRelationshipCodeValue)))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe NoAgentClientRelationship
      }

      "parse a FORBIDDEN response as a KnownFactsMismatch when the response code matches" in {
        val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(StoreVatNumberNoRelationshipCodeKey -> StoreVatNumberKnownFactsMismatchCodeValue)))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe KnownFactsMismatch
      }

      "parse a FORBIDDEN response as a StoreVatNumberFailureResponse when the response code does not match" in {
        val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(StoreVatNumberNoRelationshipCodeKey -> "INCORRECT_CODE")))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberFailureResponse(FORBIDDEN)
      }

      "parse a CONFLICT response as an AlreadySubscribed when the vat number is already subscribed" in {
        val httpResponse = HttpResponse(CONFLICT)

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe AlreadySubscribed
      }

      "parse a PRECONDITION_FAILED response as an InvalidVatNumber when the vat number is already subscribed" in {
        val httpResponse = HttpResponse(PRECONDITION_FAILED)

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberHttpParser.InvalidVatNumber
      }

      "parse a UNPROCESSABLE_ENTITY response as a IneligibleVatNumber when the vat number is already subscribed" in {
        val httpResponse = HttpResponse(UNPROCESSABLE_ENTITY)

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberHttpParser.IneligibleVatNumber
      }

      "parse any other response as a StoreVatNumberFailureResponse" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR)

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberFailureResponse(INTERNAL_SERVER_ERROR)
      }

    }
  }

}
