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
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.CtReferenceLookupStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreRegisteredSocietyStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ConfirmRegisteredSocietyControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /confirm-registered-society" when {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/confirm-registered-society", Map(SessionKeys.registeredSocietyNameKey -> testCompanyName))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /confirm-registered-society" should {
    "redirect to agree to receive email page" when {
      "there is no ctutr available and registered society is successfully stored" in {
        stubAuth(OK, successfulAuthResponse())
        stubCtReferenceNotFound(testCompanyNumber)
        stubStoreRegisteredSocietySuccess(testVatNumber, testCompanyNumber, None)

        val res = post("/confirm-registered-society",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.registeredSocietyCompanyNumberKey -> testCompanyNumber
          ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DirectDebitResolverController.show().url)
        )
      }
    }
    "redirect to agree to receive email page" when {
      "the enrolment ctutr matches the retrieved ctutr from DES" in {
        stubAuth(OK, successfulAuthResponse(irctEnrolment))
        stubCtReferenceFound(testCompanyNumber)
        stubStoreRegisteredSocietySuccess(testVatNumber, testCompanyNumber, Some(testCtUtr))

        val res = post("/confirm-registered-society",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.registeredSocietyCompanyNumberKey -> testCompanyNumber
          ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DirectDebitResolverController.show().url)
        )
      }
    }
    "redirect to agree to receive email page" when {
      "the user has an enrolment, but none exists on the backend" in {
        stubAuth(OK, successfulAuthResponse(irctEnrolment))
        stubCtReferenceNotFound(testCompanyNumber)
        stubStoreRegisteredSocietySuccess(testVatNumber, testCompanyNumber, None)

        val res = post("/confirm-registered-society",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.registeredSocietyCompanyNumberKey -> testCompanyNumber
          ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DirectDebitResolverController.show().url)
        )
      }
    }
    "redirect to capture registered society utr" when {
      "the ctutr does not match the enrolment ctutr" in {
        stubAuth(OK, successfulAuthResponse(irctEnrolment))
        stubCtReferenceFound(testCompanyNumber)
        stubStoreRegisteredSocietyCtMismatch(testVatNumber, testCompanyNumber, Some(testCtUtr))

        val res = post("/confirm-registered-society",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.registeredSocietyCompanyNumberKey -> testCompanyNumber
          ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureRegisteredSocietyUtrController.show().url)
        )
      }
    }

    "redirect to capture registered society ctutr page" when {
      "there is a ctutr available" in {
        stubAuth(OK, successfulAuthResponse())
        stubCtReferenceFound(testCompanyNumber)

        val res = post("/confirm-registered-society",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.registeredSocietyCompanyNumberKey -> testCompanyNumber
          ))()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureRegisteredSocietyUtrController.show().url)
        )
      }
    }
  }

}
