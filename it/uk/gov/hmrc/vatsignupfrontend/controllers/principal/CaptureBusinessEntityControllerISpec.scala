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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.DivisionLookupJourney
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testVatNumber
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreOverseasInformationStub.stubStoreOverseasInformation
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}
import uk.gov.hmrc.vatsignupfrontend.models.{Overseas, SoleTrader}

class CaptureBusinessEntityControllerISpec extends ComponentSpecBase with CustomMatchers {
  val administrativeDivisionVatNumber1 = "000000000"
  val administrativeDivisionVatNumber2 = "000000001"
  val nonAdministrativeDivisionVatNumber = "000000002"

  override val config: Map[String, String] = super.config + ("administrative-divisions" -> s"$administrativeDivisionVatNumber1,$administrativeDivisionVatNumber2)")

  "there is an oveaseas VRN" should {
    "return a redirect to capture vat number" in {
      stubAuth(OK, successfulAuthResponse())
      stubStoreOverseasInformation(testVatNumber)(NO_CONTENT)

      val res = get("/business-type", Map(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.businessEntityKey -> Overseas.toString))

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.DirectDebitResolverController.show().url)
      )

      SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.businessEntityKey) should contain(Overseas.toString)

    }

    "there is no VRN in session" should {
      "return a redirect to capture vat number" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/business-type")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatNumberController.show().url)
        )
      }
    }

    "there is a VRN in session and it is a division VRN" should {
      "redirect to DivisionResolverController" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        enable(DivisionLookupJourney)
        val res = get("/business-type", Map(SessionKeys.vatNumberKey -> administrativeDivisionVatNumber1))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DivisionResolverController.resolve().url)
        )
      }
    }


    "there is a VRN in session and it is not a division VRN" should {
      "return an OK" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

        val res = get("/business-type", Map(SessionKeys.vatNumberKey -> testVatNumber))

        res should have(
          httpStatus(OK)
        )
      }
    }
  }


  "POST /business-type" when {
    "the business type is sole trader" should {
      "return a SEE_OTHER status and go to the Sole Trader resolver" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> soleTrader)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(soletrader.routes.SoleTraderResolverController.resolve().url)
        )
      }
    }

    "the business type is limited company" when {
      "the user has VAT-DEC enrolment" should {
        "return a SEE_OTHER status and go to capture company number" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

          val res = post("/business-type")(BusinessEntityForm.businessEntity -> limitedCompany)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureCompanyNumberController.show().url)
          )
        }
      }
      "the user does not have a VAT-DEC enrolment" should {
        "return a SEE_OTHER status and go to capture company number" in {
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

    "the business type is other" should {
      "return a SEE_OTHER status and go to the other business entity type page" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> other)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBusinessEntityOtherController.show().url)
        )
      }
    }

  }

}

