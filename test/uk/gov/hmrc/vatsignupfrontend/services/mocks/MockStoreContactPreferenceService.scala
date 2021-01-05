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
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreContactPreferenceHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.ContactPreference
import uk.gov.hmrc.vatsignupfrontend.services.StoreContactPreferenceService

import scala.concurrent.Future


trait MockStoreContactPreferenceService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreContactPreferenceService: StoreContactPreferenceService = mock[StoreContactPreferenceService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreContactPreferenceService)
  }

  private def mockStoreContactPreference(vatNumber: String, contactPreference: ContactPreference)(returnValue: Future[StoreContactPreferenceResponse]): Unit = {
    when(mockStoreContactPreferenceService.storeContactPreference(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(contactPreference)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockStoreContactPreferenceSuccess(vatNumber: String, contactPreference: ContactPreference): Unit =
    mockStoreContactPreference(vatNumber, contactPreference)(Future.successful(Right(StoreContactPreferenceSuccess)))

  def mockStoreContactPreferenceFailure(vatNumber: String, contactPreference: ContactPreference): Unit =
    mockStoreContactPreference(vatNumber, contactPreference)(Future.successful(Left(StoreContactPreferenceFailure(INTERNAL_SERVER_ERROR))))

}
