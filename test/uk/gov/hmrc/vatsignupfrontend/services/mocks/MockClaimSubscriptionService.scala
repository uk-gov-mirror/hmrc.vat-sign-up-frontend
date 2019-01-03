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

import org.mockito.Mockito._
import org.mockito.{ArgumentMatcher, ArgumentMatchers}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.vatsignupfrontend.httpparsers.ClaimSubscriptionHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, MigratableDates, PostCode}
import uk.gov.hmrc.vatsignupfrontend.services.ClaimSubscriptionService

import scala.concurrent.Future


trait MockClaimSubscriptionService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockClaimSubscriptionService: ClaimSubscriptionService = mock[ClaimSubscriptionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockClaimSubscriptionService)
  }

  def mockClaimSubscription(vatNumber: String, isFromBta: Boolean)(returnValue: Future[ClaimSubscriptionResponse]): Unit =
    when(mockClaimSubscriptionService.claimSubscription(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(isFromBta)
    )(ArgumentMatchers.any())) thenReturn returnValue

  def mockClaimSubscription(vatNumber: String, postCode: PostCode, registrationDate: DateModel, isFromBta: Boolean
                                )(returnValue: Future[ClaimSubscriptionResponse]): Unit =
    when(mockClaimSubscriptionService.claimSubscription(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(postCode),
      ArgumentMatchers.eq(registrationDate),
      ArgumentMatchers.eq(isFromBta)
    )(ArgumentMatchers.any())) thenReturn returnValue

}
