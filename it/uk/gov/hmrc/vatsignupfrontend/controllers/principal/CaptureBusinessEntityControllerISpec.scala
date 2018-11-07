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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{CtKnownFactsIdentityVerification, UseIRSA}
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.{testSaUtr, testUserDetails}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.CitizenDetailsStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CaptureBusinessEntityControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseIRSA)
    disable(CtKnownFactsIdentityVerification)
  }

  "GET /business-type" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/business-type")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /business-type" when {

    "the business type is sole trader" when {
        "the user does not have an IRSA enrolment" should {
          "return a SEE_OTHER status and go to capture your details" in {
            enable(UseIRSA)
            stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

            val res = post("/business-type")(BusinessEntityForm.businessEntity -> soleTrader)

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureYourDetailsController.show().url)
            )
          }
        }
        "the user has an IRSA enrolment" should {
          "return a SEE_OTHER status and go to Confirm your retrieved details" in {
            enable(UseIRSA)
            stubAuth(OK, successfulAuthResponse(vatDecEnrolment, irsaEnrolment))
            stubGetCitizenDetails(testSaUtr)(OK, testUserDetails)

            val res = post("/business-type")(BusinessEntityForm.businessEntity -> soleTrader)

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.ConfirmYourRetrievedUserDetailsController.show().url)
            )
          }
        }
      }

    "the business type is limited company" when {
      "CtKnownFactsIdentityVerification is disabled" when {
        "the user has a VAT DEC enrolment" should {
          "return a SEE_OTHER status and go to capture company number" in {
            stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

            val res = post("/business-type")(BusinessEntityForm.businessEntity -> limitedCompany)

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureCompanyNumberController.show().url)
            )
          }
        }
        "the user does not have a VAT DEC enrolment" should {
          "return a SEE_OTHER status and go to caputure your details" in {
            stubAuth(OK, successfulAuthResponse())

            val res = post("/business-type")(BusinessEntityForm.businessEntity -> limitedCompany)

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureYourDetailsController.show().url)
            )
          }
        }
      }
      "CtKnownFactsIdentityVerification is enabled" should {
        "return a SEE_OTHER status and go to capture company number" in {
          enable(CtKnownFactsIdentityVerification)
          stubAuth(OK, successfulAuthResponse())

          val res = post("/business-type")(BusinessEntityForm.businessEntity -> limitedCompany)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureCompanyNumberController.show().url)
          )
        }
      }
    }

    "the business type is general partnership" should {
      "return a SEE_OTHER status and go to resolve partnership utr controller" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> generalPartnership)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(partnerships.routes.ResolvePartnershipUtrController.resolve().url)
        )
      }
    }

    "the business type is limited partnership" should {
      "return a SEE_OTHER status and go to capture partnership company number" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> limitedPartnership)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(partnerships.routes.CapturePartnershipCompanyNumberController.show().url)
        )
      }
    }

    "the business type is vat group" should {
      "return a SEE_OTHER status and go to vat group" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> vatGroup)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.VatGroupResolverController.resolve().url)
        )
      }
    }

    "the business type is other" should {
      "return a SEE_OTHER status and go to cannot use service" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> other)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CannotUseServiceController.show().url)
        )
      }
    }

  }

}
