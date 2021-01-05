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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.error.{routes => errorRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

import scala.concurrent.Future

class ResolveVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockVatControllerComponents {

  object TestResolveVatNumberController extends ResolveVatNumberController

  lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/resolve-vat-number")

  "Calling the resolve action of the Resolve Vat Number controller" when {
    "the user has a VAT-DEC enrolment" should {
      "redirect to Multiple Vat Check page" in {
        mockAuthRetrieveVatDecEnrolment()
        val request = testGetRequest

        val result = TestResolveVatNumberController.resolve(request)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.MultipleVatCheckController.show().url)

      }
    }

    "the user does not have a VAT-DEC enrolment" should {
      "redirect to Capture VAT number page" in {
        mockAuthRetrieveEmptyEnrolment()

        val result = TestResolveVatNumberController.resolve(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CaptureVatNumberController.show().url)
      }
    }

    "user has a MTD-VAT enrolment" should {
      "redirect to Multiple Vat Check page" in {
        mockAuthRetrieveAllVatEnrolments()
        val result = TestResolveVatNumberController.resolve(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.MultipleVatCheckController.show().url)
      }
    }

    "the user has an agent enrolment" should {
      "redirect to the agent using principal journey page" in {
        mockPrincipalAuthSuccess(Enrolments(Set(testAgentEnrolment)))

        val result = TestResolveVatNumberController.resolve(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(errorRoutes.AgentUsingPrincipalJourneyController.show().url)
      }
    }

    "the user has an agent affinity group" should {
      "redirect to the agent using principal journey page" in {
        mockAuthorise(
          retrievals = (Retrievals.credentialRole and Retrievals.affinityGroup and Retrievals.allEnrolments) and Retrievals.allEnrolments
        )(
          Future.successful(
            Some(Admin) ~ Some(Agent) ~ Enrolments(Set.empty) ~ Enrolments(Set.empty)
          )
        )

        val result = TestResolveVatNumberController.resolve(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(errorRoutes.AgentUsingPrincipalJourneyController.show().url)
      }
    }
  }

}
