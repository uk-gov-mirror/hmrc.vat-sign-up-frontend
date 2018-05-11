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
import uk.gov.hmrc.vatsignupfrontend.forms.EmailForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreCompanyNumberStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ConfirmCompanyNumberControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /confirm-company-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/confirm-company-number", Map(SessionKeys.companyNumberKey -> testCompanyNumber, SessionKeys.vatNumberKey -> testVatNumber))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /confirm-company-number" should {

    "the company number is successfully stored" should {
      "redirect to agree to receive email page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreCompanyNumberSuccess(testVatNumber, testCompanyNumber)

        val res = post("/client/confirm-company-number", Map(SessionKeys.companyNumberKey -> testCompanyNumber, SessionKeys.vatNumberKey -> testVatNumber))(EmailForm.email -> testEmail)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AgreeCaptureEmailController.show().url)
        )

      }
    }

    "store company number returned error" should {
      "INTERNAL_SERVER_ERROR" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreCompanyNumberFailure(testVatNumber, testCompanyNumber)

        val res = post("/client/confirm-company-number", Map(SessionKeys.companyNumberKey -> testCompanyNumber, SessionKeys.vatNumberKey -> testVatNumber))(EmailForm.email -> testEmail)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }

  }

}
