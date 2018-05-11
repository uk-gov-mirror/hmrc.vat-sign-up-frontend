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
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CaptureCompanyNumberControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /company-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/company-number")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /company-number" should {
    "return a redirect" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = post("/client/company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.ConfirmCompanyNumberController.show().url)
      )
    }
  }
}
