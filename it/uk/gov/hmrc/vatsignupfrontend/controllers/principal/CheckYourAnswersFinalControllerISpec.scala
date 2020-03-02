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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.GetCompanyNameStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubmissionStub.{stubSubmissionFailure, stubSubmissionSuccess}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubscriptionRequestSummaryStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.NonPartnershipEntity

class CheckYourAnswersFinalControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  "GET /check-your-answers-final" when {
    "feature switch is disabled" should {
      "return NOT_FOUND" in {
        disable(FinalCheckYourAnswer)

        val res = get("/check-your-answers-final",
          Map(SessionKeys.vatNumberKey -> testVatNumber)
        )

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }

    "the subscription request summary returned INTERNAL SERVER ERROR" should {
      "return INTERNAL_SERVER_ERROR" in {
        enable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse())
        stubGetSubscriptionRequestException(testVatNumber)(INTERNAL_SERVER_ERROR)

        val res = get("/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }

    "the subscription request summary returned other error" should {
      "return SEE_OTHER and restart journey" in {
        enable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse())
        stubGetSubscriptionRequestInvalidJson(testVatNumber)(SEE_OTHER)

        val res = get("/check-your-answers-final")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatNumberController.show().url)
        )
      }
    }

    "the subscription request summary returned OK" should {
      "contains a company number" should {
        "the company name service returned INTERNAL SERVER ERROR" should {
          "return INTERNAL_SERVER_ERROR" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = None,
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(FinalCheckYourAnswer)
            stubAuth(OK, successfulAuthResponse())
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyNameCompanyFailure(testCompanyNumber)

            val res = get("/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }
        "the company name service returned Company Dissolved" should {
          "return INTERNAL_SERVER_ERROR" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = None,
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(FinalCheckYourAnswer)
            enable(CrnDissolved)
            stubAuth(OK, successfulAuthResponse())
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyName(testCompanyNumber, NonPartnershipEntity, Some("dissolved"))

            val res = get("/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }
        "the company name service returned NOT FOUND " should {
          "redirect to Capture company Number for Limited Company" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = None,
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(FinalCheckYourAnswer)
            stubAuth(OK, successfulAuthResponse())
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyNameCompanyNotFound(testCompanyNumber)

            val res = get("/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )

          }
        }
        "the company name service returned OK" should {
          "return OK" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = None,
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(FinalCheckYourAnswer)
            stubAuth(OK, successfulAuthResponse())
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyName(testCompanyNumber, NonPartnershipEntity)
            val res = get("/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(OK)
            )
          }
        }
      }
      "doesn't contain a company number" should {
        "return OK" in {
          val model = SubscriptionRequestSummary(
            vatNumber = testVatNumber,
            businessEntity = SoleTrader,
            optNino = Some(testNino),
            optCompanyNumber = None,
            optSautr = None,
            optSignUpEmail = None,
            transactionEmail = testEmail,
            contactPreference = Digital
          )

          enable(FinalCheckYourAnswer)
          stubAuth(OK, successfulAuthResponse())
          stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))

          val res = get("/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

          res should have(
            httpStatus(OK)
          )
        }
      }
    }
  }
  "POST /check-your-answers-final" when {
    "the user has the direct debit attribute on the control list" should {
      "Submit successfully and redirect to information received" in {
        enable(FinalCheckYourAnswer)
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubSubmissionSuccess()

        val res = post("/check-your-answers-final", cookies = Map(
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
          enable(FinalCheckYourAnswer)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubSubmissionFailure()

          val res = post("/check-your-answers-final", cookies = Map(
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
    "the user does not have the direct debit attribute on the control list" should {
      "redirect to resolve-vat-number" when {
        "vat number and acceptedDirectDebit keys missing from session" in {
          enable(FinalCheckYourAnswer)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubSubmissionFailure()

          val res = post("/check-your-answers-final")()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ResolveVatNumberController.resolve().url)
          )
        }
      }
      "redirect to direct-debit-terms-and-conditions" when {
        "acceptedDirectDebit key is missing from session" in {
          enable(FinalCheckYourAnswer)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubSubmissionFailure()

          val res = post("/check-your-answers-final", cookies = Map(
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
          enable(FinalCheckYourAnswer)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubSubmissionFailure()

          val res = post("/check-your-answers-final", cookies = Map(
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
          enable(FinalCheckYourAnswer)
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubSubmissionFailure()

          val res = post("/check-your-answers-final", cookies = Map(
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
}
