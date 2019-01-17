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
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreCompanyNumberStub.stubStoreCompanyNumber
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreRegisteredSocietyHttpParser.CtReferenceMismatch

import scala.concurrent.Future

object StoreRegisteredSocietyStub extends WireMockMethods {


  def stubStoreRegisteredSociety(vatNumber: String, companyNumber: String, companyUtr: Option[String] = None
                                )(responseStatus: Int, optBody: Option[JsValue] = None): Unit = {
    val ongoingStubbing = when(
      method = POST,
      uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/registered-society",
      body = companyUtr match {
        case Some(companyUtr) =>
          Json.obj(
            "companyNumber" -> companyNumber,
            "ctReference" -> companyUtr
          )
        case None =>
          Json.obj(
            "companyNumber" -> companyNumber
          )
      }
    )
    optBody match {
      case Some(body) => ongoingStubbing.thenReturn(status = responseStatus, Map.empty, body)
      case _ => ongoingStubbing.thenReturn(status = responseStatus)
    }
  }

  def stubStoreRegisteredSocietySuccess(vatNumber: String, companyNumber: String, companyUtr: Option[String]): Unit =
    stubStoreRegisteredSociety(vatNumber, companyNumber, companyUtr)(NO_CONTENT)

  def stubStoreCompanyNumberCtMismatch(vatNumber: String, companyNumber: String, companyUtr: Option[String]): Unit =
    stubStoreRegisteredSociety(vatNumber, companyNumber, companyUtr)(BAD_REQUEST, Some(Json.obj("CODE" -> "CtReferenceMismatch")))

  def stubStoreRegisteredSocietyFailure(vatNumber: String, companyNumber: String, companyUtr: Option[String]): Unit =
    stubStoreRegisteredSociety(vatNumber, companyNumber, companyUtr)(INTERNAL_SERVER_ERROR)

}
