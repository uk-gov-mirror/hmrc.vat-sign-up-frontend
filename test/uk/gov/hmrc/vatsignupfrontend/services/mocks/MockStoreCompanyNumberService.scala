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
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreCompanyNumberHttpParser.{CtReferenceMismatch, StoreCompanyNumberFailureResponse, StoreCompanyNumberResponse, StoreCompanyNumberSuccess}
import uk.gov.hmrc.vatsignupfrontend.services.StoreCompanyNumberService

import scala.concurrent.Future


trait MockStoreCompanyNumberService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreCompanyNumberService: StoreCompanyNumberService = mock[StoreCompanyNumberService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreCompanyNumberService)
  }

  private def mockStoreCompanyNumber(vatNumber: String, companyNumber: String, companyUtr: Option[String])
                                    (returnValue: Future[StoreCompanyNumberResponse]): Unit = {
    when(mockStoreCompanyNumberService.storeCompanyNumber(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(companyNumber),
      ArgumentMatchers.eq(companyUtr)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockStoreCompanyNumberSuccess(vatNumber: String, companyNumber: String, companyUtr: Option[String]): Unit =
    mockStoreCompanyNumber(vatNumber, companyNumber, companyUtr)(Future.successful(Right(StoreCompanyNumberSuccess)))

  def mockStoreCompanyNumberCtMismatch(vatNumber: String, companyNumber: String, companyUtr: String): Unit =
    mockStoreCompanyNumber(vatNumber, companyNumber, Some(companyUtr))(Future.successful(Left(CtReferenceMismatch)))

  def mockStoreCompanyNumberFailure(vatNumber: String, companyNumber: String, companyUtr: Option[String]): Unit =
    mockStoreCompanyNumber(vatNumber, companyNumber, companyUtr)(Future.successful(Left(StoreCompanyNumberFailureResponse(500))))

}
