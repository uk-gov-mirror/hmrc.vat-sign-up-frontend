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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch._
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}


class CaptureBusinessEntityControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def afterAll(): Unit = {
    super.afterAll()
    disable(OptionalSautrJourney)
  }

  "GET /business-type" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/business-type")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /business-type" should {
    "redirect to capture company number name" when {
      "the business entity is limited company" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> limitedCompany)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureCompanyNumberController.show().url)
        )
      }
    }

    "redirect to capture NINO" when {
      "the business entity is sole trader and the skip CID check feature switch is enabled" in {
        enable(SkipCidCheck)

        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> soleTrader)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(soletrader.routes.CaptureNinoController.show().url)
        )
      }
    }

    "redirect to capture client details" when {
      "the business entity is sole trader" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> soleTrader)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureClientDetailsController.show().url)
        )
      }
    }

    "redirect to resolve partnership" when {
      "the business entity is general partnership" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> generalPartnership)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(partnerships.routes.ResolvePartnershipController.resolve().url)
        )
      }
    }
    "redirect to resolve partnership when joint venture or property feature switch is enabled" when {
      "the business entity is general partnership" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        enable(OptionalSautrJourney)

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> generalPartnership)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(partnerships.routes.ResolvePartnershipController.resolve().url)
        )
      }
    }

    "redirect to the resolve partnership page" when {
      "the business entity is limited partnership" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> limitedPartnership)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(partnerships.routes.ResolvePartnershipController.resolve().url)
        )
      }
    }

    "redirect to business entity other page" when {
      "the business entity is other" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type ")(BusinessEntityForm.businessEntity -> other)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBusinessEntityOtherController.show().url)
        )
      }
    }
  }
}
