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

import play.api.http.Status._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.SubmissionHttpParser.SubmissionHttpReads

class SubmissionHttpParserSpec extends UnitSpec {
  val testHttpVerb = "POST"
  val testUri = "/"

  "SubmissionHttpHttpReads" when {
    "read" should {
      "parse a NO_CONTENT response as an SubmissionSuccessResponse" in {
        val httpResponse = HttpResponse(NO_CONTENT)

        val res = SubmissionHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Right(SubmissionSuccessResponse)
      }

      "parse a INTERNAL_SERVER_ERROR response as an SubmissionFailureResponse when the response code matches" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR)

        val res = SubmissionHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(SubmissionFailureResponse(INTERNAL_SERVER_ERROR))
      }
    }
  }

}
