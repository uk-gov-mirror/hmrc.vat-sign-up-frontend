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

import play.api.http.Status.{OK, SEE_OTHER}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.vatNumberKey
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub.{stubAuth, successfulAgentAuthResponse}
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}

class SessionTimeoutControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /keep-alive" when {
    "a  user chooses to not time out" should {
      "return an OK and keep there session" in {
        stubAuth(OK, successfulAgentAuthResponse())

        val res = get("/client/keep-alive",
          Map(SessionKeys.vatNumberKey -> vatNumberKey))

        res should have(
          httpStatus(OK)
        )
        val session = SessionCookieCrumbler.getSessionMap(res)
        session.keys should contain(vatNumberKey)
      }
    }
  }

  "GET /timeout" when {
    "a agent user times out" should {
      "redirect and sign out the user" in {
        stubAuth(OK, successfulAgentAuthResponse())

        val res = get("/client/timeout",
          Map(SessionKeys.vatNumberKey -> vatNumberKey))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri("/gg/sign-in?continue=%2Fvat-through-software%2Fsign-up%2Fclient%2Fvat-number&origin=vat-sign-up-frontend")
        )
        val session = SessionCookieCrumbler.getSessionMap(res)
        session.keys shouldNot contain(vatNumberKey)
      }
    }
  }

}

