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

package uk.gov.hmrc.vatsignupfrontend.services

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.connectors.StoreMigratedVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreMigratedVatNumberHttpParser.StoreMigratedVatNumberResponse
import uk.gov.hmrc.vatsignupfrontend.models.PostCode

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreMigratedVatNumberService @Inject()(storeMigratedVatNumberConnector: StoreMigratedVatNumberConnector)(implicit ec: ExecutionContext) {

  def storeVatNumber(vatNumber: String,
                     optRegistrationDate: Option[String],
                     optBusinessPostCode: Option[PostCode]
                    )(implicit hc: HeaderCarrier): Future[StoreMigratedVatNumberResponse] = {

    optRegistrationDate match {
      case Some(regDate) =>
        storeMigratedVatNumberConnector.storeVatNumber(vatNumber, regDate, optBusinessPostCode)
      case _ =>
        storeMigratedVatNumberConnector.storeVatNumber(vatNumber)
    }
  }

}
