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

package uk.gov.hmrc.vatsubscriptionfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.StoreCompanyNumberHttpParser.StoreCompanyNumberResponse
import uk.gov.hmrc.vatsubscriptionfrontend.models.{StoreCompanyNumberFailure, StoreCompanyNumberSuccess}
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreCompanyNumberService

import scala.concurrent.Future


trait MockStoreCompanyNumberService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreCompanyNumberService: StoreCompanyNumberService = mock[StoreCompanyNumberService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreCompanyNumberService)
  }

  private def mockStoreCompanyNumber(vatNumber: String)(returnValue: Future[StoreCompanyNumberResponse]): Unit = {
    when(mockStoreCompanyNumberService.storeCompanyNumber(ArgumentMatchers.eq(vatNumber))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockStoreCompanyNumberSuccess(vatNumber: String): Unit =
    mockStoreCompanyNumber(vatNumber)(Future.successful(Right(StoreCompanyNumberSuccess)))

  def mockStoreCompanyNumberFailure(vatNumber: String): Unit =
    mockStoreCompanyNumber(vatNumber)(Future.successful(Left(StoreCompanyNumberFailure(500))))

}
