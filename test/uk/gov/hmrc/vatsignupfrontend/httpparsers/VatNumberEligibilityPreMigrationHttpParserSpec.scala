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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import java.time.LocalDate

import org.scalatest.EitherValues
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityPreMigrationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{MigratableDates, OverseasTrader}

class VatNumberEligibilityPreMigrationHttpParserSpec extends UnitSpec with EitherValues {
  val testHttpVerb = "PUT"
  val testUri = "/"
  val testMigratableDates = MigratableDates(Some(LocalDate.now()), Some(LocalDate.now()))

  "VatNumberEligibilityHttpReads" when {
    "read" should {
      "parse an OK response where the overseas flag is set to true as OverseasVatNumberEligible" in {
        val httpResponse = HttpResponse(OK, Some(Json.obj(OverseasTrader.key -> true)))

        val res = VatNumberEligibilityPreMigrationHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.right.value shouldBe VatNumberEligible(isOverseas = true)
      }

      "parse an OK response where the overseas flag is set to false as VatNumberEligible" in {
        val httpResponse = HttpResponse(OK, Some(Json.obj(OverseasTrader.key -> false)))

        val res = VatNumberEligibilityPreMigrationHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.right.value shouldBe VatNumberEligible(isOverseas = false)
      }

      "parse a BAD_REQUEST response with no body as a IneligibleForMtdVatNumber when the vat number is not on the control list" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.obj()))

        val res = VatNumberEligibilityPreMigrationHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe IneligibleForMtdVatNumber(MigratableDates())
      }

      "parse a BAD_REQUEST response with a json as a IneligibleForMtdVatNumber when the vat number is not on the control list" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.toJson(testMigratableDates)))

        val res = VatNumberEligibilityPreMigrationHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe IneligibleForMtdVatNumber(testMigratableDates)
      }

      "parse a NOT_FOUND response as a InvalidVatNumber when the vat number is not on the control list" in {
        val httpResponse = HttpResponse(NOT_FOUND)

        val res = VatNumberEligibilityPreMigrationHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe InvalidVatNumber
      }

      "parse any other response as a VatNumberEligibilityFailureResponse" in {
        val httpResponse = HttpResponse(FORBIDDEN)

        val res = VatNumberEligibilityPreMigrationHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe VatNumberEligibilityFailureResponse(FORBIDDEN)
      }

    }
  }
}
