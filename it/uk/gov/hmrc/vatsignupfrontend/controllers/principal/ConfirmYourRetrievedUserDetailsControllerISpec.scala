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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.UseIRSA
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.models.IRSA
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreNinoStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ConfirmYourRetrievedUserDetailsControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /confirm-your-details" when {
    "UseIRSA is disabled" should {
      "return an NOT_FOUND" in {
        disable(UseIRSA)

        val res = get("/confirm-your-details")

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
    "UseIRSA is enabled" should {
      "return an OK if user details is in session" in {
        enable(UseIRSA)

        stubAuth(OK, successfulAuthResponse(irsaEnrolment))

        val res = get("/confirm-your-details", Map(SessionKeys.userDetailsKey -> testUserDetailsJson))

        res should have(
          httpStatus(OK)
        )
      }
      "Redirect to business entity page if user details is not in session" in {
        enable(UseIRSA)

        stubAuth(OK, successfulAuthResponse(irsaEnrolment))

        val res = get("/confirm-your-details")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBusinessEntityController.show().url)
        )
      }

    }
  }

  "POST /confirm-your-details" should {
    "redirect to agree to receive email page if nino successfully stored" in {
      enable(UseIRSA)

      stubAuth(OK, successfulAuthResponse(irsaEnrolment))
      stubStoreNinoSuccess(testVatNumber, testUserDetails, Some(IRSA))

      val res = post("/confirm-your-details", Map(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.userDetailsKey -> testUserDetailsJson))()

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.AgreeCaptureEmailController.show().url)
      )
    }

    "redirect to agree to capture vat number if no vat number in session" in {
      enable(UseIRSA)

      stubAuth(OK, successfulAuthResponse(irsaEnrolment))

      val res = post("/confirm-your-details", Map(SessionKeys.userDetailsKey -> testUserDetailsJson))()


      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.YourVatNumberController.show().url)
      )

    }

    "redirect to agree to capture business entity page if no user details in session" in {
      enable(UseIRSA)

      stubAuth(OK, successfulAuthResponse(irsaEnrolment))

      val res = post("/confirm-your-details", Map(SessionKeys.vatNumberKey -> testVatNumber))()

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureBusinessEntityController.show().url)
      )

    }

    "throw InternalServerError if nino unsuccessfully stored" in {
      enable(UseIRSA)

      stubAuth(OK, successfulAuthResponse(irsaEnrolment))
      stubStoreNino(testVatNumber, testUserDetails, Some(IRSA))(BAD_REQUEST)

      val res = post("/confirm-your-details", Map(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.userDetailsKey -> testUserDetailsJson))()

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )

    }
  }
}
