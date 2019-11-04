/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.vatsignupfrontend.connectors.StoreMigratedVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreMigratedVatNumberHttpParser.{KnownFactsMismatch, StoreMigratedVatNumberFailureStatus, StoreMigratedVatNumberSuccess}
import uk.gov.hmrc.vatsignupfrontend.models.PostCode
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberForUnenrolledService._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreVatNumberForUnenrolledService @Inject()(storeMigratedVatNumberConnector: StoreMigratedVatNumberConnector)(implicit ec: ExecutionContext) {

  def storeVatNumber(vatNumber: String,
                     registrationDate: String,
                     businessPostCode: PostCode, isFromBta: Boolean)(implicit hc: HeaderCarrier): Future[StoreVatNumberUnenrolledResponse] = {
    storeMigratedVatNumberConnector.storeVatNumber(vatNumber, registrationDate, Some(businessPostCode)).map {
      case Right(StoreMigratedVatNumberSuccess) => StoreVatNumberUnenrolledSuccess
      case Left(KnownFactsMismatch) => StoreVatNumberUnenrolledKFFailure
      case Left(StoreMigratedVatNumberFailureStatus(status)) => StoreVatNumberUnenrolledFailure(status)
    }
  }
}

object StoreVatNumberForUnenrolledService {

  sealed trait StoreVatNumberUnenrolledResponse

  case object StoreVatNumberUnenrolledSuccess extends StoreVatNumberUnenrolledResponse

  case object StoreVatNumberUnenrolledKFFailure extends StoreVatNumberUnenrolledResponse

  case class StoreVatNumberUnenrolledFailure(status: Int) extends StoreVatNumberUnenrolledResponse

}
