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
import uk.gov.hmrc.vatsignupfrontend.httpparsers.CtReferenceLookupHttpParser._
import uk.gov.hmrc.vatsignupfrontend.services.CtReferenceLookupService

import scala.concurrent.Future


trait MockCtReferenceLookupService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockCtReferenceLookupService: CtReferenceLookupService = mock[CtReferenceLookupService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCtReferenceLookupService)
  }

  private def mockCtReferenceLookup(companyNumber: String)(returnValue: Future[CtReferenceLookupResponse]): Unit = {
    when(mockCtReferenceLookupService.checkCtReferenceExists(
      ArgumentMatchers.eq(companyNumber)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockCtReferenceFound(companyNumber: String): Unit =
    mockCtReferenceLookup(companyNumber)(Future.successful(Right(CtReferenceIsFound)))

  def mockCtReferenceNotFound(companyNumber: String): Unit =
    mockCtReferenceLookup(companyNumber)(Future.successful(Left(CtReferenceNotFound)))

  def mockCtReferenceFailure(companyNumber: String)(status: Int): Unit =
    mockCtReferenceLookup(companyNumber)(Future.successful(Left(CtReferenceLookupFailureResponse(status))))

}
