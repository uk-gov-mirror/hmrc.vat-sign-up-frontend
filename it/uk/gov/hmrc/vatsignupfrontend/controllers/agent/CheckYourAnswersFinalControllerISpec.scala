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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{CrnDissolved, FeatureSwitching}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.GetCompanyNameStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubmissionStub.{stubSubmissionFailure, stubSubmissionSuccess}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubscriptionRequestSummaryStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.NonPartnershipEntity

class CheckYourAnswersFinalControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  "GET /client/check-your-answers-final" when {

    "the subscription request summary returned INTERNAL SERVER ERROR" should {
      "return INTERNAL_SERVER_ERROR" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetSubscriptionRequestException(testVatNumber)(INTERNAL_SERVER_ERROR)

        val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "the subscription request summary returned other error" should {
      "return SEE_OTHER and restart journey" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetSubscriptionRequestInvalidJson(testVatNumber)(SEE_OTHER)

        val res = get("/client/check-your-answers-final")

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
              optSignUpEmail = Some(testEmail),
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyNameCompanyFailure(testCompanyNumber)

            val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }
        "the company name service returned dissolved" should {
          "return INTERNAL_SERVER_ERROR" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = Some(testEmail),
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(CrnDissolved)
            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyName(testCompanyNumber, NonPartnershipEntity, Some("dissolved"))

            val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }
        "the company name service returned converted closed" should {
          "return INTERNAL_SERVER_ERROR" in {
            val model = SubscriptionRequestSummary(
              vatNumber = testVatNumber,
              businessEntity = LimitedCompany,
              optNino = None,
              optCompanyNumber = Some(testCompanyNumber),
              optSautr = None,
              optSignUpEmail = Some(testEmail),
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            enable(CrnDissolved)
            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyName(testCompanyNumber, NonPartnershipEntity, Some("converted-closed"))

            val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

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
              optSignUpEmail = Some(testEmail),
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyNameCompanyNotFound(testCompanyNumber)

            val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

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
              optSignUpEmail = Some(testEmail),
              transactionEmail = testEmail,
              contactPreference = Digital
            )

            stubAuth(OK, successfulAuthResponse(agentEnrolment))
            stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))
            stubGetCompanyName(testCompanyNumber, NonPartnershipEntity)
            val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

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
            optSignUpEmail = Some(testEmail),
            transactionEmail = testEmail,
            contactPreference = Digital
          )

          stubAuth(OK, successfulAuthResponse(agentEnrolment))
          stubGetSubscriptionRequest(testVatNumber)(OK, Some(model))

          val res = get("/client/check-your-answers-final", Map(SessionKeys.vatNumberKey -> testVatNumber))

          res should have(
            httpStatus(OK)
          )
        }
      }
    }
  }

  "POST /client/check-your-answers-final" when {
    "the submission was successful" should {
      "redirect to the confirmation page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubSubmissionSuccess()

        val res = post("/client/check-your-answers-final", cookies = Map(
          SessionKeys.vatNumberKey -> testVatNumber
        ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ConfirmationController.show().url)
        )
      }
    }
    "the submission is unsuccessful" should {
      "return INTERNAL_SERVER_ERROR" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubSubmissionFailure()

        val res = post("/client/check-your-answers-final", cookies = Map(
          SessionKeys.vatNumberKey -> testVatNumber
        ))()

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }
}
