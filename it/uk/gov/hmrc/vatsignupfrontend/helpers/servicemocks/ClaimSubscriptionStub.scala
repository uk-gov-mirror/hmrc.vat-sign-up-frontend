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

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, PostCode}

object ClaimSubscriptionStub extends WireMockMethods {
  def stubClaimSubscription(vatNumber: String, optPostCode: Option[PostCode], registrationDate: DateModel, isFromBta: Boolean)(status: Int): Unit =
    stubClaimSubscription(vatNumber, requestJson(optPostCode, registrationDate, isFromBta))(status)

  def stubClaimSubscription(vatNumber: String, isFromBta: Boolean)(status: Int): Unit =
    stubClaimSubscription(vatNumber, requestJson(isFromBta))(status)

  private def stubClaimSubscription(vatNumber: String, body: JsObject)(status: Int): Unit =
    when(
      method = POST,
      uri = s"/vat-sign-up/claim-subscription/vat-number/$vatNumber",
      body = body
    ) thenReturn status

  private def requestJson(isFromBta: Boolean) =
    Json.obj(
      "isFromBta" -> isFromBta
    )

  private def requestJson(optPostCode: Option[PostCode], registrationDate: DateModel, isFromBta: Boolean): JsObject =
    Json.obj(
      "registrationDate" -> registrationDate.toLocalDate.toString,
      "isFromBta" -> isFromBta
    ) ++ (optPostCode match {
      case Some(postCode) => Json.obj("postCode" -> postCode.postCode)
      case None => Json.obj()
    })
}