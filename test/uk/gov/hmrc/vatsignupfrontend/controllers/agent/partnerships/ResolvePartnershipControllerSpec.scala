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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.OptionalSautrJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedLiabilityPartnership, LimitedPartnership, ScottishLimitedPartnership}

class ResolvePartnershipControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestResolvePartnershipController extends ResolvePartnershipController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/resolve-partnership")

  "Calling the resolve action of the Resolve Partnership Controller" when {
    "the user is a Limited Partnership" should {
      "redirect to capture partnership company number page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestResolvePartnershipController.resolve(testGetRequest.withSession(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedPartnership)
        ))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.AgentCapturePartnershipCompanyNumberController.show().url)
      }
    }
    "the user is a Limited Liability Partnership" should {
      "redirect to capture partnership company number page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestResolvePartnershipController.resolve(testGetRequest.withSession(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedLiabilityPartnership)
        ))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.AgentCapturePartnershipCompanyNumberController.show().url)
      }
    }
    "the user is a Scottish Limited Partnership" should {
      "redirect to capture partnership company number page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestResolvePartnershipController.resolve(testGetRequest.withSession(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(ScottishLimitedPartnership)
        ))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.AgentCapturePartnershipCompanyNumberController.show().url)
      }
    }
    "the user is a General Partnership and Joint Venture or Property feature switch is disabled" should {
      "redirect to capture partnership utr page" in {
        disable(OptionalSautrJourney)
        mockAuthRetrieveAgentEnrolment()

        val result = TestResolvePartnershipController.resolve(testGetRequest.withSession(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
        ))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
      }
    }
    "the user is a General Partnership and the Joint Venture or Property feature switch is enabled" should {
      "redirect to joint venture or property page" in {
        enable(OptionalSautrJourney)
        mockAuthRetrieveAgentEnrolment()

        val result = TestResolvePartnershipController.resolve(testGetRequest.withSession(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
        ))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.JointVenturePropertyController.show().url)
      }
    }
  }

}
