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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.transactionEmailKey
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.ContactPreferencesJourney
import uk.gov.hmrc.vatsignupfrontend.forms.EmailForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreEmailAddressStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}

class ConfirmAgentEmailControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /confirm-email" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/confirm-email", Map(
        SessionKeys.transactionEmailKey -> testEmail,
        SessionKeys.vatNumberKey -> testVatNumber
      ))

      res should have(
        httpStatus(OK)
      )
    }
  }


  "POST /confirm-email" should {
    "redirect to verify agent email page" when {
      "the email is successfully stored and returned with email not verified" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreTransactionEmailAddressSuccess(emailVerified = false)

        val res = post("/client/confirm-email", Map(
          SessionKeys.transactionEmailKey -> testEmail,
          SessionKeys.vatNumberKey -> testVatNumber
        ))(EmailForm.email -> testEmail)

        res should have(
          redirectUri(routes.VerifyAgentEmailController.show().url)
        )

        val session = SessionCookieCrumbler.getSessionMap(res)
        session.keys should contain(transactionEmailKey)
      }
    }

    "redirect to Agree Capture Client Email page" when {
      "the email is successfully stored and returned with email verified flag" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreTransactionEmailAddressSuccess(emailVerified = true)

        val res = post("/client/confirm-email", Map(
          SessionKeys.transactionEmailKey -> testEmail,
          SessionKeys.vatNumberKey -> testVatNumber
        ))(EmailForm.email -> testEmail)
        res should have(
          redirectUri(routes.AgreeCaptureClientEmailController.show().url)
        )

        val session = SessionCookieCrumbler.getSessionMap(res)
        session.keys should contain(transactionEmailKey)
      }
    }

    "redirect to terms page" when {
      "the email is successfully stored and returned with email verified flag" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreTransactionEmailAddressSuccess(emailVerified = false)

        val res = post("/client/confirm-email", Map(
          SessionKeys.transactionEmailKey -> testEmail,
          SessionKeys.vatNumberKey -> testVatNumber
        ))(EmailForm.email -> testEmail)

        res should have(
          redirectUri(routes.VerifyAgentEmailController.show().url)
        )

        val session = SessionCookieCrumbler.getSessionMap(res)
        session.keys should contain(transactionEmailKey)
      }
    }

    "redirect to contact preferences page" when {
      "the email is successfully stored and returned with email verified flag and contact prefs journey is enabled" in {
        enable(ContactPreferencesJourney)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreTransactionEmailAddressSuccess(emailVerified = true)

        val res = post("/client/confirm-email", Map(
          SessionKeys.transactionEmailKey -> testEmail,
          SessionKeys.vatNumberKey -> testVatNumber
        ))(EmailForm.email -> testEmail)

        res should have(
          redirectUri(routes.ContactPreferenceController.show().url)
        )

        val session = SessionCookieCrumbler.getSessionMap(res)
        session.keys should contain(transactionEmailKey)
      }
    }

    "throw an internal server error" when {
      "storing the email has been unsuccessful" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreTransactionEmailAddressFailure()

        val res = post("/client/confirm-email", Map(
          SessionKeys.transactionEmailKey -> testEmail,
          SessionKeys.vatNumberKey -> testVatNumber
        ))(EmailForm.email -> testEmail)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }

}
