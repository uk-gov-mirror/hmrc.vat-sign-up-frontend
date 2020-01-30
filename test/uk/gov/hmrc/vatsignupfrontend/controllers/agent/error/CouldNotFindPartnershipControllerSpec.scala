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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.error

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships.{routes => partnershipRoutes}

class CouldNotFindPartnershipControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockVatControllerComponents {

  object TestCouldNotFindPartnershipController extends CouldNotFindPartnershipController

  lazy val testGetRequest = FakeRequest("GET", "/client/error/company-number-not-found ")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/client/error/company-number-not-found ")

  "Calling the show action of the Could not find Partnership controller" should {
    "show the could not find partnership  page" in {
      mockAuthRetrieveAgentEnrolment()
      val request = testGetRequest

      val result = TestCouldNotFindPartnershipController.show(request)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Could not find Partnership controller" should {
    "redirect to capture company number page" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestCouldNotFindPartnershipController.submit(testPostRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) should contain(partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show().url)
    }
  }

}
