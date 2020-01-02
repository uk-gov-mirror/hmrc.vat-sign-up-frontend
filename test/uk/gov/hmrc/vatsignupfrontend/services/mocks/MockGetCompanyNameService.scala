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
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testCompanyName
import uk.gov.hmrc.vatsignupfrontend.httpparsers.GetCompanyNameHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.CompanyType
import uk.gov.hmrc.vatsignupfrontend.services.GetCompanyNameService

import scala.concurrent.Future


trait MockGetCompanyNameService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockGetCompanyNameService: GetCompanyNameService = mock[GetCompanyNameService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockGetCompanyNameService)
  }

  def mockGetCompanyName(companyNumber: String)(returnValue: Future[GetCompanyNameResponse]): Unit = {
    when(mockGetCompanyNameService.getCompanyName(
      ArgumentMatchers.eq(companyNumber)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockGetCompanyNameSuccess(companyNumber: String, companyType: CompanyType): Unit =
    mockGetCompanyName(companyNumber)(Future.successful(Right(CompanyDetails(testCompanyName, companyType))))

  def mockGetCompanyNameNotFound(companyNumber: String): Unit =
    mockGetCompanyName(companyNumber)(Future.successful(Left(CompanyNumberNotFound)))

  def mockGetCompanyNameFailure(companyNumber: String): Unit =
    mockGetCompanyName(companyNumber)(Future.successful(Left(GetCompanyNameFailureResponse(500))))

}
