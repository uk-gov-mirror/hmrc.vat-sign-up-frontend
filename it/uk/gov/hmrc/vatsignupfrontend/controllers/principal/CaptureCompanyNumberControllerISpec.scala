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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CrnDissolved
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.GetCompanyNameStub.stubGetCompanyName
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.{LimitedLiabilityPartnership, NonPartnershipEntity}
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.error.{routes => errorRoutes}

class CaptureCompanyNumberControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /company-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/company-number")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /company-number" when {
    "the company is not a partnership" when {
      "redirect to Confirm Company Name page" in {
        stubAuth(OK, successfulAuthResponse())
        stubGetCompanyName(testCompanyNumber, NonPartnershipEntity)

        val res = post("/company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ConfirmCompanyController.show().url)
        )
      }
    }
    "the company is a partnership" should {
      "redirect to Partnership As Company Error page" in {
        stubAuth(OK, successfulAuthResponse())
        stubGetCompanyName(testCompanyNumber, LimitedLiabilityPartnership)

        val res = post("/company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.PartnershipAsCompanyErrorController.show().url)
        )
      }
    }
    "the company has been dissolved" should {
      "redirect to Company has been dissolved page" in {
        enable(CrnDissolved)
        stubAuth(OK, successfulAuthResponse())
        stubGetCompanyName(testCompanyNumber, LimitedLiabilityPartnership, Some("dissolved"))

        val res = post("/company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.DissolvedCompanyController.show().url)
        )
      }
    }

    "the company number is less than 8 characters long" when {
      "the CRN doesn't have a prefix" should {
        "redirect to the Confirm Company Name page" in {
          stubAuth(OK, successfulAuthResponse())
          stubGetCompanyName(testShortPaddedCompanyNumber, NonPartnershipEntity)

          val res = post("/company-number")(CompanyNumberForm.companyNumber -> testShortCompanyNumber)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ConfirmCompanyController.show().url)
          )
        }
      }
      "the CRN has a prefix" should {
        "redirect to the Confirm Company Name page" in {
          stubAuth(OK, successfulAuthResponse())
          stubGetCompanyName(testPrefixedPaddedCompanyNumber, NonPartnershipEntity)

          val res = post("/company-number")(CompanyNumberForm.companyNumber -> testPrefixedCompanyNumber)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ConfirmCompanyController.show().url)
          )
        }
      }
    }
  }

}
