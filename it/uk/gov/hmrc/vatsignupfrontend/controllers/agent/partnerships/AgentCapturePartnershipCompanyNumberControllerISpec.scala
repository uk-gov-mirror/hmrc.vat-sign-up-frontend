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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CrnDissolved
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.error.{routes => errorRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.GetCompanyNameStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.{LimitedPartnership, NonPartnershipEntity}

class AgentCapturePartnershipCompanyNumberControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /partnership-company-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/partnership-company-number")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /partnership-company-number" when {
    "get company name returns LimitedPartnership" should {
      "redirect to Confirm Partnership page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetCompanyName(testCompanyNumber, LimitedPartnership)

        val res = post("/client/partnership-company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ConfirmPartnershipController.show().url)
        )
      }
    }

    "get company name returns a LimitedPartnership which is dissolved" should {
      "redirect to dissolved company page" in {
        enable(CrnDissolved)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetCompanyName(testCompanyNumber, LimitedPartnership, Some("dissolved"))

        val res = post("/client/partnership-company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.DissolvedCompanyController.show().url)
        )
      }
    }

    "get company name returns a LimitedPartnership which is converted-closed" should {
      "redirect to dissolved company page" in {
        enable(CrnDissolved)
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetCompanyName(testCompanyNumber, LimitedPartnership, Some("converted-closed"))

        val res = post("/client/partnership-company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.DissolvedCompanyController.show().url)
        )
      }
    }

    "the company number is shorter than 8 characters" when {
      "the CRN doesn't have a prefix" should {
        "redirect to the Confirm partnership page" in {
          stubAuth(OK, successfulAuthResponse(agentEnrolment))
          stubGetCompanyName(testShortPaddedCompanyNumber, LimitedPartnership)

          val res = post("/client/partnership-company-number")(CompanyNumberForm.companyNumber -> testShortCompanyNumber)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ConfirmPartnershipController.show().url)
          )
        }
      }
      "the CRN has a prefix" should {
        "redirect to the Confirm partnership page" in {
          stubAuth(OK, successfulAuthResponse(agentEnrolment))
          stubGetCompanyName(testPrefixedPaddedCompanyNumber, LimitedPartnership)

          val res = post("/client/partnership-company-number")(CompanyNumberForm.companyNumber -> testPrefixedCompanyNumber)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ConfirmPartnershipController.show().url)
          )
        }
      }
    }

    "get company name returns NonPartnershipEntity" should {
      "redirect to Not a Limited Partnership page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetCompanyName(testCompanyNumber, NonPartnershipEntity)

        val res = post("/client/partnership-company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.NotALimitedPartnershipController.show().url)
        )
      }
    }

    "get company name returns NOT_FOUND" should {
      "redirect to Could Not Find Partnership page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubGetCompanyNameCompanyNotFound(testCompanyNumber)

        val res = post("/client/partnership-company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.CouldNotFindPartnershipController.show().url)
        )
      }
    }
  }

}
