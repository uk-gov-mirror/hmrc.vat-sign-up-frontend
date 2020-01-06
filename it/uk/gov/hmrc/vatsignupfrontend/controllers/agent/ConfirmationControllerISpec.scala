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
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}
import uk.gov.hmrc.vatsignupfrontend.models.SoleTrader

class ConfirmationControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /information-received" should {
    "return an OK when there's a business entity in session" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/information-received",
        Map(
          SessionKeys.businessEntityKey -> SoleTrader.toString
        )
      )

      res should have(
        httpStatus(OK)
      )
    }
    "redirect to Capture VAT number if there isn't a business entity in session" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/information-received")

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureVatNumberController.show().url)
      )
    }
  }
}
