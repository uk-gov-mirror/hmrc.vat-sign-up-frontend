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

package uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks

import play.api.http.Status._
import play.api.libs.json.Json

object CtReferenceLookupStub extends WireMockMethods {

  def stubCtReferenceLookup(companyNumber: String)(responseStatus: Int): Unit = {
    when(
      method = POST,
      uri = s"/vat-sign-up/subscription-request/ct-reference-check",
      body = Json.obj("companyNumber" -> companyNumber)
    ).thenReturn(responseStatus)
  }

  def stubCtReferenceFound(companyNumber: String): Unit =
    stubCtReferenceLookup(companyNumber)(OK)

  def stubCtReferenceNotFound(companyNumber: String): Unit =
    stubCtReferenceLookup(companyNumber)(NOT_FOUND)

  def stubCtReferenceLookupFailure(companyNumber: String): Unit =
    stubCtReferenceLookup(companyNumber)(INTERNAL_SERVER_ERROR)

}
