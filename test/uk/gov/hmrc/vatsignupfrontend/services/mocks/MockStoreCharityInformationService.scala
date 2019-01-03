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
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreCharityInformationHttpParser.StoreCharityInformationResponse
import uk.gov.hmrc.vatsignupfrontend.services.StoreCharityInformationService

import scala.concurrent.Future

trait MockStoreCharityInformationService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreCharityInformationService: StoreCharityInformationService = mock[StoreCharityInformationService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreCharityInformationService)
  }

  def mockStoreCharityInformation(vatNumber: String)(returnValue: Future[StoreCharityInformationResponse]): Unit = {
    when(mockStoreCharityInformationService.storeCharityInformation(
      ArgumentMatchers.eq(vatNumber)
    )(ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

}
