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

package uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks

import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, CONFLICT, NO_CONTENT}
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.Constants.{StoreVatNumberNoRelationshipCodeKey, StoreVatNumberNoRelationshipCodeValue}

object VatEligibilityStub extends WireMockMethods {

  def stubVatNumberEligibilitySuccess(vatNumber: String): Unit = {
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/mtdfb-eligibility")
      .thenReturn(status = NO_CONTENT)
  }

  def stubVatNumberEligibilityFailure(vatNumber: String): Unit = {
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/mtdfb-eligibility")
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj(StoreVatNumberNoRelationshipCodeKey -> StoreVatNumberNoRelationshipCodeValue))
  }

  def stubVatNumberIneligibleForMtd(vatNumber: String): Unit = {
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/mtdfb-eligibility")
      .thenReturn(status = BAD_REQUEST)
  }

  def stubVatNumberEligibilityInvalid(vatNumber: String): Unit = {
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/mtdfb-eligibility")
      .thenReturn(status = NOT_FOUND)
  }

  def stubVatNumberEligibilityAlreadySubscribed(vatNumber: String): Unit = {
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/mtdfb-eligibility")
      .thenReturn(status = CONFLICT)
  }

}