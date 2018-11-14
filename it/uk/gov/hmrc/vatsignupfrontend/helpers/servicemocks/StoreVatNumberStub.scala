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

import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreVatNumberHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, MigratableDates, PostCode}

object StoreVatNumberStub extends WireMockMethods {

  private def requestJson(isFromBta: Boolean) =
    Json.obj(
      "vatNumber" -> testVatNumber,
      "isFromBta" -> isFromBta
    )


  def stubStoreVatNumberSuccess(isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", requestJson(isFromBta))
      .thenReturn(status = CREATED)
  }

  def stubStoreVatNumberNoRelationship(isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", requestJson(isFromBta))
      .thenReturn(status = FORBIDDEN, body = Json.obj(CodeKey -> NoRelationshipCode))
  }

  def stubStoreVatNumberIneligible(isFromBta: Boolean,
                                   migratableDates: MigratableDates): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", requestJson(isFromBta))
      .thenReturn(status = UNPROCESSABLE_ENTITY, Json.toJson(migratableDates))
  }


  def stubStoreVatNumberAlreadySignedUp(isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", requestJson(isFromBta))
      .thenReturn(status = CONFLICT)
  }

  def stubStoreVatNumberFailure(isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", requestJson(isFromBta))
      .thenReturn(status = BAD_REQUEST)
  }

  def stubStoreVatNumberSubscriptionClaimed(isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", requestJson(isFromBta))
      .thenReturn(status = OK, body = Json.obj(CodeKey -> SubscriptionClaimedCode))
  }


  private def requestJson(postCode: PostCode, registrationDate: DateModel, isFromBta: Boolean): JsObject =
    Json.obj(
      "vatNumber" -> testVatNumber,
      "postCode" -> postCode.postCode,
      "registrationDate" -> registrationDate.toLocalDate.toString,
      "isFromBta" -> isFromBta
    )

  def stubStoreVatNumberSuccess(postCode: PostCode, registrationDate: DateModel, isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", body =
      requestJson(postCode, registrationDate, isFromBta))
      .thenReturn(status = CREATED)
  }

  def stubStoreVatNumberSubscriptionClaimed(postCode: PostCode, registrationDate: DateModel, isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", body =
      requestJson(postCode, registrationDate, isFromBta))
      .thenReturn(status = OK, body = Json.obj(CodeKey -> SubscriptionClaimedCode))
  }

  def stubStoreVatNumberKnownFactsMismatch(postCode: PostCode, registrationDate: DateModel, isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", body =
      requestJson(postCode, registrationDate, isFromBta))
      .thenReturn(status = FORBIDDEN, body = Json.obj(CodeKey -> KnownFactsMismatchCode))
  }

  def stubStoreVatNumberInvalid(postCode: PostCode, registrationDate: DateModel, isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", body =
      requestJson(postCode, registrationDate, isFromBta))
      .thenReturn(status = PRECONDITION_FAILED)
  }

  def stubStoreVatNumberIneligible(postCode: PostCode,
                                   registrationDate: DateModel,
                                   isFromBta: Boolean,
                                   migratableDates: MigratableDates = MigratableDates()): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", body =
      requestJson(postCode, registrationDate, isFromBta))
      .thenReturn(status = UNPROCESSABLE_ENTITY, Json.toJson(migratableDates))
  }

  def stubStoreVatNumberAlreadySignedUp(postCode: PostCode, registrationDate: DateModel, isFromBta: Boolean): Unit = {
    when(method = POST, uri = "/vat-sign-up/subscription-request/vat-number", body =
      requestJson(postCode, registrationDate, isFromBta))
      .thenReturn(status = CONFLICT)
  }

}