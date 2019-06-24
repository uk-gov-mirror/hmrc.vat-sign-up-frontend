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
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.GetCompanyNameStub.stubGetCompanyName
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.NonPartnershipEntity

class CaptureRegisteredSocietyCompanyNumberControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /registered-society-company-number" when {
    "the society name feature switch is enabled" should {
      "return an OK" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/registered-society-company-number")

        res should have(
          httpStatus(OK)
        )
      }
    }
  }

  "POST /registered-society-company-number" when {
    "the society name feature switch is enabled" should {
      "redirect to confirm society name" in {
        stubAuth(OK, successfulAuthResponse())
        stubGetCompanyName(testCompanyNumber, NonPartnershipEntity)

        val res = post("/registered-society-company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.ConfirmRegisteredSocietyController.show().url)
        )
      }
    }
  }
}
