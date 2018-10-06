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

package uk.gov.hmrc.vatsignupfrontend.connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

trait MockAuthConnector extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  def mockAuthorise[T](predicate: Predicate = EmptyPredicate,
                       retrievals: Retrieval[T]
                      )(response: Future[T]): Unit = {
    when(
      mockAuthConnector.authorise(
        ArgumentMatchers.eq(predicate),
        ArgumentMatchers.eq(retrievals)
      )(
        ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext])
    ) thenReturn response
  }

  def mockAuthEmptyRetrieval(): Unit =
    mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))

  def mockAuthConfidenceLevelRetrieval(confidenceLevel: ConfidenceLevel): Unit =
    mockAuthorise(retrievals = Retrievals.credentialRole and Retrievals.confidenceLevel)(Future.successful(new ~(Some
    (Admin), confidenceLevel)))

  def mockAuthRetrieveAgentEnrolment(): Unit =
    mockAuthorise(retrievals = Retrievals.allEnrolments)(Future.successful(Enrolments(Set(testAgentEnrolment))))

  def mockAuthRetrieveVatDecEnrolment(hasIRSAEnrolment: Boolean = false): Unit = {
    mockAuthorise(
      retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
    )(
      Future.successful {
        if(hasIRSAEnrolment) new ~(Some(Admin), Enrolments(Set(testVatDecEnrolment, testIRSAEnrolment)))
        else new ~(Some(Admin), Enrolments(Set(testVatDecEnrolment)))
      }
    )
  }

  def mockAuthRetrieveIRCTEnrolment(): Unit =
    mockAuthorise(
      retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
    )(Future.successful(new ~(Some(Admin), Enrolments(Set(testIRCTEnrolment)))))

  def mockAuthRetrievePartnershipEnrolment(): Unit =
    mockAuthorise(
      retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
    )(Future.successful(new ~(Some(Admin), Enrolments(Set(testPartnershipEnrolment)))))


  def mockAuthAdminRole(): Unit =
    mockAuthorise(retrievals = Retrievals.credentialRole)(Future.successful(Some(Admin)))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }
}
