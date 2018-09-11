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

package uk.gov.hmrc.vatsignupfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreEmailAddressHttpParser.{StoreEmailAddressFailure, StoreEmailAddressResponse, StoreEmailAddressSuccess}
import uk.gov.hmrc.vatsignupfrontend.services.StoreEmailAddressService

import scala.concurrent.Future


trait MockStoreEmailAddressService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreEmailAddressService: StoreEmailAddressService = mock[StoreEmailAddressService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreEmailAddressService)
  }

  private def mockStoreEmailAddress(vatNumber: String, email: String)(returnValue: Future[StoreEmailAddressResponse]): Unit = {
    when(mockStoreEmailAddressService.storeEmailAddress(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(email)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  private def mockStoreTransactionEmailAddress(
    vatNumber: String,
    transactionEmail: String
  )(returnValue: Future[StoreEmailAddressResponse]): Unit = {
    when(mockStoreEmailAddressService.storeTransactionEmailAddress(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(transactionEmail)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockStoreEmailAddressSuccess(vatNumber: String, email: String)(emailVerified: Boolean): Unit =
    mockStoreEmailAddress(vatNumber, email)(Future.successful(Right(StoreEmailAddressSuccess(emailVerified))))

  def mockStoreEmailAddressFailure(vatNumber: String, email: String): Unit =
    mockStoreEmailAddress(vatNumber, email)(Future.successful(Left(StoreEmailAddressFailure(500))))

  def mockStoreTransactionEmailAddressSuccess(vatNumber: String, transactionEmail: String)(emailVerified: Boolean): Unit =
    mockStoreTransactionEmailAddress(vatNumber, transactionEmail)(Future.successful(Right(StoreEmailAddressSuccess(emailVerified))))

  def mockStoreTransactionEmailAddressFailure(vatNumber: String, transactionEmail: String): Unit =
    mockStoreTransactionEmailAddress(vatNumber, transactionEmail)(Future.successful(Left(StoreEmailAddressFailure(500))))


}
