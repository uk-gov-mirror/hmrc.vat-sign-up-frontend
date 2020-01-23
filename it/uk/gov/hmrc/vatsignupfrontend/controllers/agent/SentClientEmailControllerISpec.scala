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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FinalCheckYourAnswer
import uk.gov.hmrc.vatsignupfrontend.forms.EmailForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class SentClientEmailControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /sent-client-email" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/sent-client-email", Map(SessionKeys.emailKey -> testEmail))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /sent-client-email" should {
    "return a redirect to the AgentSendYourApplicationController" when {
      "the final check your answer feature switch is disabled" in {
        disable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/sent-client-email", Map(SessionKeys.emailKey -> testEmail))(EmailForm.email -> testEmail)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AgentSendYourApplicationController.show().url)
        )

        val session = getSessionMap(res)
        session.keys should contain(emailKey)
      }
    }
    "return a redirect to the final check your answer controller" when {
      "the final check your answer feature switch is enabled" in {
        enable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/sent-client-email", Map(SessionKeys.emailKey -> testEmail))(EmailForm.email -> testEmail)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersFinalController.show().url)
        )

        val session = getSessionMap(res)
        session.keys should contain(emailKey)
      }
    }
  }
}
