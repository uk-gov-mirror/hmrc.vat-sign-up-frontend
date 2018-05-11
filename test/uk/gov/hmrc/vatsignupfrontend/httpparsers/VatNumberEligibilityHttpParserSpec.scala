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
import play.api.http.Status.{BAD_REQUEST, CONFLICT, FORBIDDEN, NO_CONTENT, NOT_FOUND}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityHttpParser.VatNumberEligibilityHttpReads

class VatNumberEligibilityHttpParserSpec extends UnitSpec with EitherValues {
  val testHttpVerb = "PUT"
  val testUri = "/"

  "VatNumberEligibilityHttpReads" when {
    "read" should {
      "parse a NO_CONTENT response as an VatNumberEligible" in {
        val httpResponse = HttpResponse(NO_CONTENT)

        val res = VatNumberEligibilityHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.right.value shouldBe VatNumberEligible
      }

      "parse a BAD_REQUEST response as a IneligibleForMtdVatNumber when the vat number is not on the control list" in {
        val httpResponse = HttpResponse(BAD_REQUEST)

        val res = VatNumberEligibilityHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe IneligibleForMtdVatNumber
      }

      "parse a NOT_FOUND response as a InvalidVatNumber when the vat number is not on the control list" in {
        val httpResponse = HttpResponse(NOT_FOUND)

        val res = VatNumberEligibilityHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe InvalidVatNumber
      }

      "parse a CONFLICT response as a VatNumberAlreadySubscribed when the vat number is already subscribed" in {
        val httpResponse = HttpResponse(CONFLICT)

        val res = VatNumberEligibilityHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe VatNumberAlreadySubscribed
      }

      "parse any other response as a VatNumberEligibilityFailureResponse" in {
        val httpResponse = HttpResponse(FORBIDDEN)

        val res = VatNumberEligibilityHttpReads.read(testHttpVerb, testUri, httpResponse)

        res.left.value shouldBe VatNumberEligibilityFailureResponse(FORBIDDEN)
      }

    }
  }
}
