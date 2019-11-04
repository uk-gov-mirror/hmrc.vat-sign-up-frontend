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

package uk.gov.hmrc.vatsignupfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.vatsignupfrontend.models.PostCode
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberForUnenrolledService
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberForUnenrolledService._
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global


trait MockStoreVatNumberForUnenrolledService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreVatNumberForUnerolledService: StoreVatNumberForUnenrolledService = mock[StoreVatNumberForUnenrolledService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreVatNumberForUnerolledService)
  }

  private def mockStoreVatNumber(vatNumber: String,
                                 registrationDate: String,
                                 businessPostCode: PostCode,
                                 isFromBta: Boolean)
                                (returnValue: Future[StoreVatNumberUnenrolledResponse]) = {
    when(mockStoreVatNumberForUnerolledService.storeVatNumber(ArgumentMatchers.eq(vatNumber), ArgumentMatchers.eq(registrationDate),
      ArgumentMatchers.eq(businessPostCode), ArgumentMatchers.eq(isFromBta))
    (ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockUnenrolledSuccessStoreVatNumber(vatNumber: String,
                                          registrationDate: String,
                                          businessPostCode: PostCode,
                                          isFromBta: Boolean): Unit = {
    mockStoreVatNumber(vatNumber, registrationDate, businessPostCode, isFromBta)(Future(StoreVatNumberUnenrolledSuccess))
  }

  def mockUnenrolledFailedStoreVatNumber(vatNumber: String,
                                         registrationDate: String,
                                         businessPostCode: PostCode,
                                         isFromBta: Boolean): Unit = {
    mockStoreVatNumber(vatNumber, registrationDate, businessPostCode, isFromBta)(Future(StoreVatNumberUnenrolledFailure(500)))
  }

  def mockUnenrolledKnownFactsFailedStoreVatNumber(vatNumber: String,
                                         registrationDate: String,
                                         businessPostCode: PostCode,
                                         isFromBta: Boolean): Unit = {
    mockStoreVatNumber(vatNumber, registrationDate, businessPostCode, isFromBta)(Future(StoreVatNumberUnenrolledKFFailure))
  }

}
