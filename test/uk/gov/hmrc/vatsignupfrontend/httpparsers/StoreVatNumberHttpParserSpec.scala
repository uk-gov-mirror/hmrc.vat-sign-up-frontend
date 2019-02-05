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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import java.time.LocalDate

import org.scalatest.EitherValues
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreVatNumberHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{MigratableDates, OverseasTrader}

class StoreVatNumberHttpParserSpec extends UnitSpec with EitherValues {
  val testHttpVerb = "PUT"
  val testUri = "/"

  val currentDate = LocalDate.now()
  val testMigratableDates = MigratableDates(Some(currentDate), Some(currentDate))

  "StoreVatNumberHttpReads" when {
    "read" should {
      "parse an OK response as a OverseasVatNumberStored with a Json body that contains a true overseas flag" in {
        val httpResponse = HttpResponse(OK, Some(Json.obj(OverseasTrader.key -> true)))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.right.value shouldBe OverseasVatNumberStored
      }

      "parse an OK response as a VatNumberStored with a Json body that contains a false overseas flag" in {
        val httpResponse = HttpResponse(OK, Some(Json.obj(OverseasTrader.key -> false)))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.right.value shouldBe VatNumberStored
      }

      "parse a CREATED response as a VatNumberStored" in {
        val httpResponse = HttpResponse(CREATED)

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.right.value shouldBe VatNumberStored
      }

      "parse a FORBIDDEN response as a NoAgentClientRelationship when the response code matches" in {
        val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(CodeKey -> NoRelationshipCode)))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe NoAgentClientRelationship
      }

      "parse a FORBIDDEN response as a KnownFactsMismatch when the response code matches" in {
        val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(CodeKey -> KnownFactsMismatchCode)))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe KnownFactsMismatch
      }

      "parse a FORBIDDEN response as a StoreVatNumberFailureResponse when the response code does not match" in {
        val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(CodeKey -> "INCORRECT_CODE")))

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

      "parse an UNPROCESSABLE_ENTITY response as an IneligibleVatNumber when the vat number is already subscribed and " +
        "we have a date on which to migrate" in {
        val httpResponse = HttpResponse(UNPROCESSABLE_ENTITY, Some(Json.toJson(testMigratableDates)))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberHttpParser.IneligibleVatNumber(testMigratableDates)
      }

      "parse an UNPROCESSABLE_ENTITY response as an IneligibleVatNumber when the vat number is already subscribed" in {
        val httpResponse = HttpResponse(UNPROCESSABLE_ENTITY, Some(Json.obj()))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberHttpParser.IneligibleVatNumber(MigratableDates())
      }

      "parse a BAD_REQUEST response as a VatMigrationInProgress when the vat number is already subscribed" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.obj(CodeKey -> "VatMigrationInProgress")))

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberHttpParser.VatMigrationInProgress
      }

      "parse any other response as a StoreVatNumberFailureResponse" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR)

        val res = StoreVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe StoreVatNumberFailureResponse(INTERNAL_SERVER_ERROR)
      }

    }
  }

}
