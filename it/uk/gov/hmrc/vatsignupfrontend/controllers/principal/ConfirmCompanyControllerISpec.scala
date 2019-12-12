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
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.CtReferenceLookupStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreCompanyNumberStub.{stubStoreCompanyNumberCtMismatch, stubStoreCompanyNumberSuccess}
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ConfirmCompanyControllerISpec extends ComponentSpecBase with CustomMatchers {


  "GET /confirm-company" when {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/confirm-company", Map(SessionKeys.companyNameKey -> testCompanyName))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /confirm-company" should {

    "the company number is successfully stored" when {

      "if CT enrolled" should {
        "redirect to agree to receive email page" in {
          stubAuth(OK, successfulAuthResponse(irctEnrolment))
          stubCtReferenceFound(testCompanyNumber)
          stubStoreCompanyNumberSuccess(testVatNumber, testCompanyNumber, companyUtr = Some(testCtUtr))

          val res = post("/confirm-company",
            Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.companyNumberKey -> testCompanyNumber
            ))()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.DirectDebitResolverController.show().url)
          )
        }
      }

      "if not CT enrolled" when {
        "the SkipCtUtrOnCotaxNotFound feature switch is enabled" should {
          "redirect to capture company UTR page when there is a CT reference on COTAX" in {
            stubAuth(OK, successfulAuthResponse())
            stubCtReferenceFound(testCompanyNumber)

            val res = post("/confirm-company",
              Map(
                SessionKeys.vatNumberKey -> testVatNumber,
                SessionKeys.companyNumberKey -> testCompanyNumber
              ))()

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureCompanyUtrController.show().url)
            )
          }
          "redirect to the capture CTR page when store company number returns CtReferenceMismatch" in {
            stubAuth(OK, successfulAuthResponse(irctEnrolment))
            stubCtReferenceFound(testCompanyNumber)
            stubStoreCompanyNumberCtMismatch(testVatNumber, testCompanyNumber, testCtUtr)

            val res = post("/confirm-company",
              Map(
                SessionKeys.vatNumberKey -> testVatNumber,
                SessionKeys.companyNumberKey -> testCompanyNumber
              ))()

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureCompanyUtrController.show().url)
            )
          }
          "skip capture company UTR page when there isn't a CT reference on COTAX" in {
            stubAuth(OK, successfulAuthResponse())
            stubCtReferenceNotFound(testCompanyNumber)
            stubStoreCompanyNumberSuccess(testVatNumber, testCompanyNumber, None)

            val res = post("/confirm-company",
              Map(
                SessionKeys.vatNumberKey -> testVatNumber,
                SessionKeys.companyNumberKey -> testCompanyNumber
              ))()

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.DirectDebitResolverController.show().url)
            )
          }
          "the user has a CT enrolment and there isn't a CT reference on COTAX" in {
            stubAuth(OK, successfulAuthResponse(irctEnrolment))
            stubCtReferenceNotFound(testCompanyNumber)
            stubStoreCompanyNumberSuccess(testVatNumber, testCompanyNumber, None)

            val res = post("/confirm-company",
              Map(
                SessionKeys.vatNumberKey -> testVatNumber,
                SessionKeys.companyNumberKey -> testCompanyNumber
              ))()

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.DirectDebitResolverController.show().url)
            )
          }
        }


      }
    }
  }

}
