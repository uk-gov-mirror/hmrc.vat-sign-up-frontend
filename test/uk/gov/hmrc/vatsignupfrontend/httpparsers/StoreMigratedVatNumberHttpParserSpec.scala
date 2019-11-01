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

import org.scalatest.EitherValues
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreMigratedVatNumberHttpParser.{KnownFactsMismatch, NoAgentClientRelationship, StoreMigratedVatNumberFailureStatus, StoreMigratedVatNumberSuccess}

class StoreMigratedVatNumberHttpParserSpec extends UnitSpec with EitherValues {

  val testHttpVerb = "POST"
  val testUri = "/"
  val CodeKey = "CODE"
  val knownFactsMismatch = "KNOWN_FACTS_MISMATCH"
  val relationshipNotFound = "RELATIONSHIP_NOT_FOUND"

  "StoreVatNumberHttpReads" when {
    "read" should {
      "parse an OK response as a store migrated Vat number SuccessResponse" in {
        val httpResponse = HttpResponse(OK)
        val result = StoreMigratedVatNumberHttpParser.StoreMigratedVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)
        result shouldBe Right(StoreMigratedVatNumberSuccess)

      }
      "the status is FORBIDDEN and the response code is KNOWN_FACTS_MISMATCH" should {
        "return KnownFactsMismatch" in {
          val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(CodeKey -> knownFactsMismatch)))
          val result = StoreMigratedVatNumberHttpParser.StoreMigratedVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)
          result shouldBe Left(KnownFactsMismatch)
        }
      }
      "the status is FORBIDDEN and the response code is RELATIONSHIP_NOT_FOUND" should {
        "return RelationShipNotFound" in {
          val httpResponse = HttpResponse(FORBIDDEN, Some(Json.obj(CodeKey -> relationshipNotFound)))
          val result = StoreMigratedVatNumberHttpParser.StoreMigratedVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)
          result shouldBe Left(NoAgentClientRelationship)
        }
      }
      "parse BAD_REQUEST response as a StoreMigratedVatNumberFailureStatus with status" in {
        val httpResponse = HttpResponse(BAD_REQUEST)
        val result = StoreMigratedVatNumberHttpParser.StoreMigratedVatNumberHttpReads.read(testHttpVerb, testUri, httpResponse)
        result shouldBe Left(StoreMigratedVatNumberFailureStatus(BAD_REQUEST))
      }
    }
  }

}
