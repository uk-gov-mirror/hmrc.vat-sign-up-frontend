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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, RegisteredSocietyJourney}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreRegisteredSocietyStub.stubStoreRegisteredSocietySuccess
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ConfirmRegisteredSocietyControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(RegisteredSocietyJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(RegisteredSocietyJourney)
  }

  "GET /client/confirm-registered-society" when {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/confirm-registered-society", Map(SessionKeys.registeredSocietyNameKey -> testCompanyName))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "GET /client/confirm-registered-society" when {
    "the Registered Society feature switch is disabled" should {
      "return a 404" in {
        disable(RegisteredSocietyJourney)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/confirm-registered-society", Map(SessionKeys.registeredSocietyNameKey -> testCompanyName))

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
  }


  "POST /client/confirm-registered-society" should {
    "redirect to the capture agent email pager" when {
      "the registered society is successfully stored" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreRegisteredSocietySuccess(testVatNumber, testCompanyNumber, None)

        val res = post("/client/confirm-registered-society",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.registeredSocietyCompanyNumberKey -> testCompanyNumber
          ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureAgentEmailController.show().url)
        )
      }
    }
  }

}
