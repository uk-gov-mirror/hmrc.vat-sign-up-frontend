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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{VerifyAgentEmail, VerifyClientEmail}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.http.InternalServerException


class EmailRoutingControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestEmailRoutingController extends EmailRoutingController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/email")

  "Calling the route action of the EmailRoutingController" when {
    "VerifyClientEmail is enabled" should {
      "go to AgreeCaptureClientEmailController" in {
        disable(VerifyAgentEmail)
        enable(VerifyClientEmail)
        mockAuthRetrieveAgentEnrolment()
        val result = TestEmailRoutingController.route(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.AgreeCaptureClientEmailController.show().url)
      }
    }
    "VerifyAgentEmail is enabled" should {
      "go to AgreeCaptureEmailController" in {
        enable(VerifyAgentEmail)
        disable(VerifyClientEmail)
        mockAuthRetrieveAgentEnrolment()
        val result = TestEmailRoutingController.route(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureAgentEmailController.show().url)
      }
    }

    "Both are enabled" should {
      "go to AgreeCaptureEmailController" in {
        enable(VerifyAgentEmail)
        enable(VerifyClientEmail)
        mockAuthRetrieveAgentEnrolment()
        val result = TestEmailRoutingController.route(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureAgentEmailController.show().url)
      }
    }

    "Both are disabled" should {
      "Return technical difficulties" in {
        disable(VerifyAgentEmail)
        disable(VerifyClientEmail)
        mockAuthRetrieveAgentEnrolment()

        intercept[InternalServerException](await(TestEmailRoutingController.route(testGetRequest)))
      }
    }
  }

}
