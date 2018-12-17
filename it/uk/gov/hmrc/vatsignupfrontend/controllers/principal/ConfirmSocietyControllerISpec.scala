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
import sun.net.RegisteredDomain
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.RegisteredSocietyJourney
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreCompanyNumberStub.stubStoreCompanyNumberSuccess
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, RegisteredSocietyJourney}

class ConfirmSocietyControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(RegisteredSocietyJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(RegisteredSocietyJourney)
  }

  "GET /confirm-registered-society" when {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/confirm-registered-society", Map(SessionKeys.societyNameKey -> testCompanyName))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "GET /confirm-registered-society" when {
    "the Registered Society feature switch is disabled" should {
      "return a 404 not found response" in {
        disable(RegisteredSocietyJourney)
        stubAuth(OK, successfulAuthResponse())

        val res = get("/confirm-registered-society", Map(SessionKeys.societyNameKey -> testCompanyName))

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
  }

  "GET /confirm-registered-society" when {
    "the Registered Society feature switch is disabled" should {
      "return an 404 not found" in {
        disable(RegisteredSocietyJourney)
        stubAuth(OK, successfulAuthResponse())

        val res = get("/confirm-registered-society", Map(SessionKeys.societyNameKey -> testCompanyName))

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }

  }

  "POST /confirm-registered-society" should {

    "redirect to agree to receive email page" when {

      "the company number is successfully stored" in {
        stubAuth(OK, successfulAuthResponse(irctEnrolment))
        stubStoreCompanyNumberSuccess(testVatNumber, testCompanyNumber, None)

        val res = post("/confirm-registered-society",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.societyCompanyNumberKey -> testCompanyNumber
          ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AgreeCaptureEmailController.show().url)
        )

      }
    }

  }

}
