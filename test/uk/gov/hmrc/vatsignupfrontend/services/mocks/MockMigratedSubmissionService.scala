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
import uk.gov.hmrc.vatsignupfrontend.httpparsers.SubmissionHttpParser.{SubmissionFailureResponse, SubmissionResponse, SubmissionSuccessResponse}
import uk.gov.hmrc.vatsignupfrontend.services.MigratedSubmissionService

import scala.concurrent.Future


trait MockMigratedSubmissionService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockMigratedSubmissionService: MigratedSubmissionService = mock[MigratedSubmissionService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMigratedSubmissionService)
  }

  private def mockMigratedSubmit(vatNumber: String)(returnValue: Future[SubmissionResponse]): Unit = {
    when(mockMigratedSubmissionService.submit(ArgumentMatchers.eq(vatNumber))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockMigratedSubmitSuccess(vatNumber: String): Unit =
    mockMigratedSubmit(vatNumber)(Future.successful(Right(SubmissionSuccessResponse)))

  def mockMigratedSubmitFailure(vatNumber: String): Unit =
    mockMigratedSubmit(vatNumber)(Future.successful(Left(SubmissionFailureResponse(INTERNAL_SERVER_ERROR))))

}
