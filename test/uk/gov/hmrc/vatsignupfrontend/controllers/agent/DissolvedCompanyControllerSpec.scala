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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships.{routes => partnershipRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testCompanyName
import uk.gov.hmrc.vatsignupfrontend.models.{LimitedCompany, LimitedPartnership, RegisteredSociety}

class DissolvedCompanyControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestDissolvedCompanyController extends DissolvedCompanyController(
    mockControllerComponents
  )

  lazy val testGetRequest = FakeRequest("GET", "/client/error/dissolved-company")

  "Calling the show action of DissolvedCompanyController with a company number in session" should {
    "show the dissolved company view" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestDissolvedCompanyController.show(testGetRequest.withSession(SessionKeys.companyNameKey -> testCompanyName))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the show action of DissolvedCompanyController without a company number in session" should {
    "redirect to CaptureCompanyNumberController if the business entity is Limited Company" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestDissolvedCompanyController.show(testGetRequest.withSession(SessionKeys.businessEntityKey -> LimitedCompany.toString))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CaptureCompanyNumberController.show().url)
    }
    "redirect to AgentCapturePartnershipCompanyNumberController if the business entity is Partnership" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestDissolvedCompanyController.show(testGetRequest.withSession(SessionKeys.businessEntityKey -> LimitedPartnership.toString))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show().url)
    }
    "redirect to CaptureRegisteredSocietyCompanyNumberController if the business entity is Registered Society" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestDissolvedCompanyController.show(testGetRequest.withSession(SessionKeys.businessEntityKey -> RegisteredSociety.toString))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CaptureRegisteredSocietyCompanyNumberController.show().url)
    }
    "redirect to CaptureBusinessEntityController without a Business Entity" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestDissolvedCompanyController.show(testGetRequest)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
    }
  }
}
