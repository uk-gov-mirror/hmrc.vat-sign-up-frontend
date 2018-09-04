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

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{VerifyAgentEmail, VerifyClientEmail}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class UseDifferentEmailAddressControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(VerifyAgentEmail)
    enable(VerifyClientEmail)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(VerifyAgentEmail)
    disable(VerifyClientEmail)
  }

  "GET /client/use-different-email-address" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/use-different-email-address")

      res should have(
        httpStatus(OK)
      )
    }
    "return an NOT_FOUND if VerifyAgentEmail is disabled" in {
      disable(VerifyAgentEmail)
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/use-different-email-address")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
    "return an NOT_FOUND if VerifyClientEmail is disabled" in {
      disable(VerifyClientEmail)
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/use-different-email-address")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

  "POST /client/use-different-email-address" should {
    "return a SEE_OTHER" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = post("/client/use-different-email-address")()

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureClientEmailController.show().url)
      )
    }
  }

}
