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
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreCompanyNumberStub.stubStoreCompanyNumberSuccess
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ConfirmSocietyControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /confirm-registered-society" when {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/confirm-registered-society", Map(SessionKeys.societyNameKey -> testCompanyName))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /confirm-registered-society" should {

    "the company number is successfully stored" when {

      "if CT enrolled" should {
        "redirect to agree to receive email page" in {
          stubAuth(OK, successfulAuthResponse(irctEnrolment))
          stubStoreCompanyNumberSuccess(testVatNumber, testCompanyNumber, companyUtr = Some(testSaUtr))

          val res = post("/confirm-registered-society",
            Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.societyCompanyNumberKey -> testCompanyNumber
            ))()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.AgreeCaptureEmailController.show().url)
          )

        }
      }

      "if not CT enrolled" should {
        "redirect to capture company UTR page" in {
          stubAuth(OK, successfulAuthResponse())

          val res = post("/confirm-registered-society",
            Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.societyCompanyNumberKey -> testCompanyNumber
            ))()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureCompanyUtrController.show().url)
          )
        }
      }

    }
  }

}
