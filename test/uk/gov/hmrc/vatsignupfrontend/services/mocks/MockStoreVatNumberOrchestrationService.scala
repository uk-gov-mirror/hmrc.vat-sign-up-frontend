/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberOrchestrationService
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberOrchestrationService.StoreVatNumberOrchestrationServiceResponse

import scala.concurrent.Future

trait MockStoreVatNumberOrchestrationService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreVatNumberOrchestrationService: StoreVatNumberOrchestrationService = mock[StoreVatNumberOrchestrationService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreVatNumberOrchestrationService)
  }

  def mockOrchestrate(enrolments: Enrolments,
                      vatNumber: String
                     )(returnValue: Future[StoreVatNumberOrchestrationServiceResponse]): Unit = {
    when(mockStoreVatNumberOrchestrationService.orchestrate(
      ArgumentMatchers.eq(enrolments),
      ArgumentMatchers.eq(vatNumber)
    )(ArgumentMatchers.any[HeaderCarrier])
    ) thenReturn returnValue
  }

}
