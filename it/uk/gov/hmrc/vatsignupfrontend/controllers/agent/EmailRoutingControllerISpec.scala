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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.VerifyAgentEmail
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class EmailRoutingControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /client/email" when {
    "when VerifyAgentEmail is disabled" should {
      "return redirect to agree capture email" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        disable(VerifyAgentEmail)

        val res = get("/client/email")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AgreeCaptureEmailController.show().url)
        )
      }
    }
    "when VerifyAgentEmail is enabled" should {
      "return redirect to Capture Agent Email" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        enable(VerifyAgentEmail)

        val res = get("/client/email")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureAgentEmailController.show().url)
        )
      }
    }
  }

}
