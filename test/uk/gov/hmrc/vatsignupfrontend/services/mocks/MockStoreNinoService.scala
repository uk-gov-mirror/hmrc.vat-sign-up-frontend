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

package uk.gov.hmrc.vatsignupfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreNinoHttpParser._
import uk.gov.hmrc.vatsignupfrontend.services.StoreNinoService

import scala.concurrent.Future

trait MockStoreNinoService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreNinoService: StoreNinoService = mock[StoreNinoService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreNinoService)
  }

  private def mockStoreNino(vatNumber: String, nino: String)(returnValue: Future[StoreNinoResponse]): Unit = {
    when(mockStoreNinoService.storeNino(
      ArgumentMatchers.eq(vatNumber), ArgumentMatchers.eq(nino)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockStoreNinoSuccess(vatNumber: String, nino: String): Unit =
    mockStoreNino(vatNumber, nino)(Future.successful(Right(StoreNinoSuccess)))

  def mockStoreNinoNoVatStored(vatNumber: String, nino: String): Unit =
    mockStoreNino(vatNumber, nino)(Future.successful(Left(NoVATNumberFailure)))

  def mockStoreNinoFailure(vatNumber: String, nino: String): Unit =
    mockStoreNino(vatNumber, nino)(Future.successful(Left(StoreNinoFailureResponse(INTERNAL_SERVER_ERROR))))

}
