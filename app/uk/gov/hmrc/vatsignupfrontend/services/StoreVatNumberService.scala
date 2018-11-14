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

package uk.gov.hmrc.vatsignupfrontend.services

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.connectors.StoreVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreVatNumberHttpParser.StoreVatNumberResponse
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, PostCode}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreVatNumberService @Inject()(val storeVatNumberConnector: StoreVatNumberConnector
                                     )(implicit ec: ExecutionContext) {

  def storeVatNumberDelegated(vatNumber: String)(implicit hc:HeaderCarrier): Future[StoreVatNumberResponse] =
    storeVatNumberConnector.storeVatNumber(vatNumber, isFromBta = None)

  def storeVatNumber(vatNumber: String, isFromBta: Option[Boolean])(implicit hc: HeaderCarrier): Future[StoreVatNumberResponse] =
    storeVatNumberConnector.storeVatNumber(vatNumber, isFromBta)

  def storeVatNumber(vatNumber: String,
                     postCode: PostCode,
                     registrationDate: DateModel,
                     isFromBta: Boolean)(implicit hc: HeaderCarrier): Future[StoreVatNumberResponse] =
    storeVatNumberConnector.storeVatNumber(vatNumber, postCode.postCode, registrationDate.toLocalDate.toString, isFromBta)

}
