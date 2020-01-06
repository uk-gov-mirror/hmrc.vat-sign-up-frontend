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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.frontendToBackendBusinessEntity
import uk.gov.hmrc.vatsignupfrontend.models.{ContactPreference, SubscriptionRequestSummary}

object SubscriptionRequestSummaryStub extends WireMockMethods {

  def subscriptionRequestSummaryToJson(subModel: SubscriptionRequestSummary): JsObject = {
    Json.obj(
      "vatNumber" -> subModel.vatNumber,
      "businessEntity" -> Json.obj(
        "entityType" -> frontendToBackendBusinessEntity(subModel.businessEntity),
        "nino" -> subModel.optNino,
        "companyNumber" -> subModel.optCompanyNumber,
        "sautr" -> subModel.optSautr
      ),
    "transactionEmail" -> subModel.transactionEmail,
      "contactPreference" -> ContactPreference.contactPreferenceFormat.toString(subModel.contactPreference)
    ) ++ subModel.optSignUpEmail.fold(Json.obj())(email => Json.obj("optSignUpEmail" -> email))
  }
  def stubGetSubscriptionRequest(vatNumber: String)(status: Int, response: Option[SubscriptionRequestSummary]): StubMapping =
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber")
      .thenReturn(status = status, body = response.map(subscriptionRequestSummaryToJson(_)).getOrElse(Json.obj()))

  def stubGetSubscriptionRequestInvalidJson(vatNumber: String)(status: Int): StubMapping =
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber")
      .thenReturn(status = status, body = Json.obj("foo" -> "bar"))

  def stubGetSubscriptionRequestException(vatNumber: String)(responseStatus: Int): StubMapping =
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber")
      .thenReturn(status = responseStatus)
}
