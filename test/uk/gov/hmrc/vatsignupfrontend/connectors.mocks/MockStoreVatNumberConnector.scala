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

package uk.gov.hmrc.vatsignupfrontend.connectors.mocks

import org.mockito.ArgumentMatchers
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.connectors.StoreVatNumberConnector
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreVatNumberHttpParser.StoreVatNumberResponse

import scala.concurrent.Future

trait MockStoreVatNumberConnector extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreVatNumberConnector)
  }

  val mockStoreVatNumberConnector: StoreVatNumberConnector = mock[StoreVatNumberConnector]

  def mockStoreVatNumber(vatNumber: String,
                         isFromBta: Boolean
                        )(response: Future[StoreVatNumberResponse]): Unit =
    when(mockStoreVatNumberConnector.storeVatNumber(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(isFromBta)
    )(
      ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn response


  def mockStoreVatNumber(vatNumber: String,
                         postCode: String,
                         registrationDate: String,
                         isFromBta: Boolean
                        )(response: Future[StoreVatNumberResponse]): Unit =
    when(mockStoreVatNumberConnector.storeVatNumber(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(postCode),
      ArgumentMatchers.eq(registrationDate),
      ArgumentMatchers.eq(isFromBta)
    )(
      ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn response
}
