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
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.StoreVatNumberHttpParser._
import uk.gov.hmrc.vatsubscriptionfrontend.services.StoreVatNumberService
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.vatsubscriptionfrontend.models.{DateModel, PostCode}

import scala.concurrent.Future


trait MockStoreVatNumberService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreVatNumberService: StoreVatNumberService = mock[StoreVatNumberService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreVatNumberService)
  }

  private def mockStoreVatNumber(vatNumber: String)(returnValue: Future[StoreVatNumberResponse]): Unit = {
    when(mockStoreVatNumberService.storeVatNumber(ArgumentMatchers.eq(vatNumber))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockStoreVatNumberSuccess(vatNumber: String): Unit =
    mockStoreVatNumber(vatNumber)(Future.successful(Right(StoreVatNumberSuccess)))

  def mockStoreVatNumberFailure(vatNumber: String): Unit =
    mockStoreVatNumber(vatNumber)(Future.successful(Left(StoreVatNumberFailureResponse(INTERNAL_SERVER_ERROR))))

  def mockStoreVatNumberNoRelationship(vatNumber: String): Unit =
    mockStoreVatNumber(vatNumber)(Future.successful(Left(NoAgentClientRelationship)))

  def mockStoreVatNumberAlreadySubscribed(vatNumber: String): Unit =
    mockStoreVatNumber(vatNumber)(Future.successful(Left(AlreadySubscribed)))

  private def mockStoreVatNumber(vatNumber: String, postCode: PostCode, registrationDate: DateModel)(returnValue: Future[StoreVatNumberResponse]): Unit = {
    when(mockStoreVatNumberService.storeVatNumber(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(postCode),
      ArgumentMatchers.eq(registrationDate)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockStoreVatNumberSuccess(vatNumber: String, postCode: PostCode, registrationDate: DateModel): Unit =
    mockStoreVatNumber(vatNumber, postCode, registrationDate)(Future.successful(Right(StoreVatNumberSuccess)))

  def mockStoreVatNumberFailure(vatNumber: String, postCode: PostCode, registrationDate: DateModel): Unit =
    mockStoreVatNumber(vatNumber, postCode, registrationDate)(Future.successful(Left(StoreVatNumberFailureResponse(INTERNAL_SERVER_ERROR))))

  def mockStoreVatNumberNoRelationship(vatNumber: String, postCode: PostCode, registrationDate: DateModel): Unit =
    mockStoreVatNumber(vatNumber, postCode, registrationDate)(Future.successful(Left(NoAgentClientRelationship)))

  def mockStoreVatNumberAlreadySubscribed(vatNumber: String, postCode: PostCode, registrationDate: DateModel): Unit =
    mockStoreVatNumber(vatNumber, postCode, registrationDate)(Future.successful(Left(AlreadySubscribed)))

}
