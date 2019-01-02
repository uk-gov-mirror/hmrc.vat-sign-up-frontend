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
import org.mockito.Mockito.reset
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreIdentityVerificationHttpParser.StoreIdentityVerificationResponse
import uk.gov.hmrc.vatsignupfrontend.services.StoreIdentityVerificationService
import org.mockito.Mockito._

import scala.concurrent.Future

trait MockStoreIdentityVerificationService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreIdentityVerificationService: StoreIdentityVerificationService =
    mock[StoreIdentityVerificationService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreIdentityVerificationService)
  }

  def mockStoreIdentityVerification(vatNumber: String,
                                    continueUrl: String
                                   )(response: Future[StoreIdentityVerificationResponse]): Unit =
    when(mockStoreIdentityVerificationService.storeIdentityVerification(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(continueUrl)
    )(
      ArgumentMatchers.any[HeaderCarrier]
    )) thenReturn response
}
