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

package uk.gov.hmrc.vatsubscriptionfrontend.services

import javax.inject.{Inject, Singleton}

import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsubscriptionfrontend.connectors.StoreSubscriptionDetailsConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreSubscriptionDetailsService @Inject()(val storeSubscriptionDetailsConnector: StoreSubscriptionDetailsConnector) {

  def storeVatNumber(vatNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext):
                                        Future[Either[StoreSubscriptionDetailsFailure.type, StoreSubscriptionDetailsSuccess.type]] = {
    storeSubscriptionDetailsConnector.storeVatNumber(vatNumber) map { response =>
      response.status match {
        case NO_CONTENT =>
          Right(StoreSubscriptionDetailsSuccess)
        case _ =>
          Left(StoreSubscriptionDetailsFailure)
      }
    }
  }
}

object StoreSubscriptionDetailsSuccess

object StoreSubscriptionDetailsFailure