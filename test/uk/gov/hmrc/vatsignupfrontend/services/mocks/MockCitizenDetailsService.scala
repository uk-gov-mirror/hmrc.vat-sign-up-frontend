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
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.CitizenDetailsHttpParser.{CitizenDetailsResponse,
CitizenDetailsRetrievalFailureResponse, CitizenDetailsRetrievalSuccess, MoreThanOneCitizenMatched, NoCitizenRecord}
import uk.gov.hmrc.vatsignupfrontend.models.UserDetailsModel
import uk.gov.hmrc.vatsignupfrontend.services.CitizenDetailsService

import scala.concurrent.Future


trait MockCitizenDetailsService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockCitizenDetailsService: CitizenDetailsService = mock[CitizenDetailsService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCitizenDetailsService)
  }

  private def mockCitizenDetailsBySautr(sautr: String)(returnValue: Future[CitizenDetailsResponse]): Unit = {
    when(mockCitizenDetailsService.getCitizenDetailsBySautr(
      ArgumentMatchers.eq(sautr)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockCitizenDetailsSuccessBySautr(sautr: String, userDetails: UserDetailsModel): Unit =
    mockCitizenDetailsBySautr(sautr)(Future.successful(Right(CitizenDetailsRetrievalSuccess(userDetails))))

  def mockCitizenDetailsNotFoundBySautr(sautr: String): Unit =
    mockCitizenDetailsBySautr(sautr)(Future.successful(Left(NoCitizenRecord)))

  def mockCitizenDetailsMoreThanOneBySautr(sautr: String): Unit =
    mockCitizenDetailsBySautr(sautr)(Future.successful(Left(MoreThanOneCitizenMatched)))

  def mockCitizenDetailsFailureBySautr(sautr: String): Unit =
    mockCitizenDetailsBySautr(sautr)(Future.successful(Left(CitizenDetailsRetrievalFailureResponse(500))))


  private def mockCitizenDetailsByNino(nino: String)(returnValue: Future[CitizenDetailsResponse]): Unit = {
    when(mockCitizenDetailsService.getCitizenDetailsByNino(
      ArgumentMatchers.eq(nino)
    )(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(returnValue)
  }

  def mockCitizenDetailsSuccessByNino(nino: String, userDetails: UserDetailsModel): Unit =
    mockCitizenDetailsByNino(nino)(Future.successful(Right(CitizenDetailsRetrievalSuccess(userDetails))))

  def mockCitizenDetailsNotFoundByNino(nino: String): Unit =
    mockCitizenDetailsByNino(nino)(Future.successful(Left(NoCitizenRecord)))

  def mockCitizenDetailsMoreThanOneByNino(nino: String): Unit =
    mockCitizenDetailsByNino(nino)(Future.successful(Left(MoreThanOneCitizenMatched)))

  def mockCitizenDetailsFailureByNino(nino: String): Unit =
    mockCitizenDetailsByNino(nino)(Future.successful(Left(CitizenDetailsRetrievalFailureResponse(500))))

}
