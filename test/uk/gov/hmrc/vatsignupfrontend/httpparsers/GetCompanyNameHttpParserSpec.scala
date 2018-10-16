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

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.Constants.{GetCompanyNameCodeKey, GetCompanyTypeCodeKey}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.GetCompanyNameHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse

class GetCompanyNameHttpParserSpec extends UnitSpec {
  val testHttpVerb = "GET"
  val testUri = "/"

  "GetCompanyNameHttpReads" when {
    "read" should {
      "parse a OK response as an GetCompanyNameSuccess" in {
        val httpResponse = HttpResponse(OK, Some(Json.obj(GetCompanyNameCodeKey -> testCompanyNumber, GetCompanyTypeCodeKey -> GetCompanyNameHttpParser.LimitedPartnershipKey)))

        val res = GetCompanyNameHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Right(GetCompanyNameSuccess(testCompanyNumber, companieshouse.LimitedPartnership))
      }
      "parse a NOT_FOUND response as an CompanyNumberNotFound" in {
        val httpResponse = HttpResponse(NOT_FOUND)

        val res = GetCompanyNameHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(CompanyNumberNotFound)
      }
      "parse any other response as an StoreCompanyNumberFailure" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.obj()))

        val res = GetCompanyNameHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetCompanyNameFailureResponse(httpResponse.status))
      }
      "parse empty body response as an StoreCompanyNumberFailure" in {
        val httpResponse = HttpResponse(OK)

        val res = GetCompanyNameHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetCompanyNameFailureResponse(httpResponse.status))
      }
    }
  }
}
