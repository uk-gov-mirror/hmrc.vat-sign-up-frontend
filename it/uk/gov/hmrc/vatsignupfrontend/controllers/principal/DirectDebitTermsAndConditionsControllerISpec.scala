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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.acceptedDirectDebitTermsKey
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.DirectDebitTermsJourney
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}

class DirectDebitTermsAndConditionsControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /direct-debit-terms-and-conditions" should {

    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())
      enable(DirectDebitTermsJourney)

      val res = get("/direct-debit-terms-and-conditions")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /direct-debit-terms-and-conditions" should {

    lazy val res = post("/direct-debit-terms-and-conditions")()

    "return a redirect to Agree Capture Email" in {
      stubAuth(OK, successfulAuthResponse())
      enable(DirectDebitTermsJourney)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.AgreeCaptureEmailController.show().url)
      )
    }

    "Add the acceptDirectDebitTermsKey to the session" in {
      val session = SessionCookieCrumbler.getSessionMap(res)
      session.keys should contain(acceptedDirectDebitTermsKey)
    }
  }
}
