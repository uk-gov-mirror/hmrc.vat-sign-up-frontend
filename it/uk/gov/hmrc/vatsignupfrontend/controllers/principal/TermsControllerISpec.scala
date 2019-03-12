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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.DirectDebitTermsJourney
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubmissionStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class TermsControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /terms-of-participation" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

      val res = get("/terms-of-participation")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /terms-of-participation" when {
    "the user has the direct debit attribute on the control list" should {
      "Submit successfully and redirect to information received" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubSubmissionSuccess()

        val res = post("/terms-of-participation", cookies = Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.hasDirectDebitKey -> "true",
          SessionKeys.acceptedDirectDebitTermsKey -> "true"
        ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.InformationReceivedController.show().url)
        )
      }

      "Submission is unsuccessful" should {
        "return INTERNAL_SERVER_ERROR" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubSubmissionFailure()

          val res = post("/terms-of-participation", cookies = Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.hasDirectDebitKey -> "true",
            SessionKeys.acceptedDirectDebitTermsKey -> "true"
          ))()

          res should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
    }
  }

  "the user does not have the direct debit attribute on the control list" should {
    "redirect to resolve-vat-number" when {
      "vat number and acceptedDirectDebit keys missing from session" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubSubmissionFailure()

        val res = post("/terms-of-participation")()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ResolveVatNumberController.resolve().url)
        )
      }
    }
    "redirect to direct-debit-terms-and-conditions" when {
      "acceptedDirectDebit key is missing from session" in {
        enable(DirectDebitTermsJourney)

        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubSubmissionFailure()

        val res = post("/terms-of-participation", cookies = Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.hasDirectDebitKey -> "true"
        ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DirectDebitTermsAndConditionsController.show().url)
        )
      }
    }
    "redirect to direct-debit-terms-and-conditions" when {
      "acceptedDirectDebit key in session is false" in {
        enable(DirectDebitTermsJourney)

        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubSubmissionFailure()

        val res = post("/terms-of-participation", cookies = Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.hasDirectDebitKey -> "true",
          SessionKeys.acceptedDirectDebitTermsKey -> "false"
        ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DirectDebitTermsAndConditionsController.show().url)
        )
      }
    }
    "redirect to resolve-vat-number" when {
      "vat number key is missing from session" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubSubmissionFailure()

        val res = post("/terms-of-participation", cookies = Map(
          SessionKeys.acceptedDirectDebitTermsKey -> "true"
        ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ResolveVatNumberController.resolve().url)
        )
      }
    }
  }
}
