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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CtKnownFactsIdentityVerification
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyUtrForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CaptureCompanyUtrControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /company-utr" when {
    "the CT Known Facts feature switch is enabled" should {
      "return an OK" in {
        enable(CtKnownFactsIdentityVerification)
        stubAuth(OK, successfulAuthResponse())

        val res = get("/company-utr")

        res should have(
          httpStatus(OK)
        )
      }
    }
  }

    "POST /company-utr" when {
      "the CT Known Facts feature switch is enabled" should {
        "return NOT_IMPLEMENTED status" in {
          enable(CtKnownFactsIdentityVerification)
          stubAuth(OK, successfulAuthResponse())

          val res = post("/company-utr")(CompanyUtrForm.companyUtr -> testCompanyUtr)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.NoCtEnrolmentSummaryController.show().url)
          )
        }
      }
    }

  }
