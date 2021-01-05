/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.connectors

import javax.inject.{Inject, Singleton}

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreEmailAddressHttpParser.StoreEmailAddressResponse
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreEmailAddressHttpParser._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreEmailAddressConnector @Inject()(val http: HttpClient,
                                        val applicationConfig: AppConfig) {

  val emailAddressKey = "email"
  val transactionEmailKey = "transactionEmail"

  def storeEmailAddress(
    vatNumber: String,
    email: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[StoreEmailAddressResponse] = {
    http.PUT[JsObject, StoreEmailAddressResponse](
      applicationConfig.storeEmailAddressUrl(vatNumber),
      Json.obj(emailAddressKey -> email)
    )
  }

  def storeTransactionEmailAddress(
    vatNumber: String,
    transactionEmail: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[StoreEmailAddressResponse] = {
    http.PUT[JsObject, StoreEmailAddressResponse](
      applicationConfig.storeTransactionEmailAddressUrl(vatNumber),
      Json.obj(transactionEmailKey -> transactionEmail)
    )
  }
}