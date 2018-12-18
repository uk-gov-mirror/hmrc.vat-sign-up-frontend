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
import org.mockito.Mockito.{reset, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreRegisteredSocietyHttpParser._
import uk.gov.hmrc.vatsignupfrontend.services.StoreRegisteredSocietyService
import play.api.http.Status.INTERNAL_SERVER_ERROR

import scala.concurrent.Future

trait MockStoreRegisteredSocietyService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreRegisteredSocietyService: StoreRegisteredSocietyService = mock[StoreRegisteredSocietyService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreRegisteredSocietyService)
  }

  private def mockStoreRegisteredSociety(vatNumber: String, companyNumber: String)(returnValue: Future[StoreRegisteredSocietyResponse]): Unit = {
    when(mockStoreRegisteredSocietyService.storeRegisteredSociety(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(companyNumber)
    )(ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockStoreRegisteredSocietySuccess(vatNumber: String, companyNumber: String): Unit =
    mockStoreRegisteredSociety(vatNumber, companyNumber)(Future.successful(Right(StoreRegisteredSocietySuccess)))

  def mockStoreRegisteredSocietyFailure(vatNumber: String, companyNumber: String): Unit =
    mockStoreRegisteredSociety(vatNumber, companyNumber)(Future.successful(Left(StoreRegisteredSocietyFailureResponse(INTERNAL_SERVER_ERROR))))

}
