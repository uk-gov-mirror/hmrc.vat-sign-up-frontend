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

package uk.gov.hmrc.vatsignupfrontend.connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

trait MockAuthConnector extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  implicit class RetrievalCombiner[A](a: A) {
    def ~[B](b: B): A ~ B = new ~(a, b)
  }

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

  def mockFailedAuth(authorisationException: AuthorisationException = InternalError("Uh oh")): Unit = {
    when(mockAuthConnector.authorise(ArgumentMatchers.any(), ArgumentMatchers.any()
    )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext]))
      .thenReturn(Future.failed(authorisationException))
  }

  def mockAuthEmptyRetrieval(): Unit =
    mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))

  def mockAuthNinoRetrieval(optNino: Option[String], hasIrsaEnrolment: Boolean = false): Unit = {
    val enrolments =
      if (hasIrsaEnrolment) Enrolments(Set(testIRSAEnrolment))
      else Enrolments(Set.empty)

    mockAuthorise(
      retrievals = (Retrievals.credentialRole and Retrievals.affinityGroup and Retrievals.allEnrolments) and Retrievals.nino
    )(
      Future.successful(
        Some(Admin) ~ None ~ enrolments ~ optNino
      )
    )
  }

  def mockAuthConfidenceLevelRetrieval(confidenceLevel: ConfidenceLevel): Unit =
    mockAuthorise(
      retrievals = (Retrievals.credentialRole and Retrievals.affinityGroup and Retrievals.allEnrolments) and Retrievals.confidenceLevel
    )(
      Future.successful(
        Some(Admin) ~ None ~ Enrolments(Set.empty) ~ confidenceLevel
      )
    )

  def mockAuthRetrieveAgentEnrolment(): Unit =
    mockAuthorise(retrievals = Retrievals.allEnrolments)(Future.successful(Enrolments(Set(testAgentEnrolment))))

  def mockPrincipalAuthSuccess(enrolments: Enrolments): Unit = {
    mockAuthorise(
      retrievals = (Retrievals.credentialRole and Retrievals.affinityGroup and Retrievals.allEnrolments) and Retrievals.allEnrolments
    )(
      Future.successful(
        Some(Admin) ~ Some(Organisation) ~ enrolments ~ enrolments
      )
    )
  }

  def mockAgentAuthSuccess(enrolments: Enrolments): Unit = {
    mockAuthorise(
      retrievals = Retrievals.allEnrolments and Retrievals.allEnrolments
    )(
      Future.successful(
        enrolments ~ enrolments
      )
    )
  }

  def mockAuthRetrieveVatDecEnrolment(hasIRSAEnrolment: Boolean = false): Unit = {
    val enrolments = if (hasIRSAEnrolment) Enrolments(Set(testVatDecEnrolment, testIRSAEnrolment))
    else Enrolments(Set(testVatDecEnrolment))

    mockPrincipalAuthSuccess(enrolments)
  }

  def mockAuthRetrieveMtdVatEnrolment(): Unit = mockPrincipalAuthSuccess(Enrolments(Set(testMtdVatEnrolment)))

  def mockAuthRetrieveAllVatEnrolments(): Unit = mockPrincipalAuthSuccess(Enrolments(Set(testMtdVatEnrolment, testVatDecEnrolment)))

  def mockAuthRetrieveIRCTEnrolment(): Unit = mockPrincipalAuthSuccess(Enrolments(Set(testIRCTEnrolment)))

  def mockAuthRetrievePartnershipEnrolment(): Unit = mockPrincipalAuthSuccess(Enrolments(Set(testPartnershipEnrolment)))

  def mockAuthRetrieveEmptyEnrolment(): Unit = mockPrincipalAuthSuccess(Enrolments(Set.empty))

  def mockAuthAdminRole(): Unit =
    mockAuthorise(retrievals = Retrievals.credentialRole and Retrievals.affinityGroup and Retrievals.allEnrolments
    )(Future.successful(
      Some(Admin) ~ None ~ Enrolments(Set.empty)
    ))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }
}
