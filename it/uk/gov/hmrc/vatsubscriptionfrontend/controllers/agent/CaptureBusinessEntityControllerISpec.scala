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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.agent

import play.api.http.Status._
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessEntityForm
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}


class CaptureBusinessEntityControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /business-entity" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/business-entity")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /business-entity" should {
    "redirect to capture company number name" when {
      "the business entity is limited company" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-entity")(BusinessEntityForm.businessEntity -> limitedCompany)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureCompanyNumberController.show().url)
        )
      }
    }

    "redirect to capture client details" when {
      "the business entity is sole trader" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-entity")(BusinessEntityForm.businessEntity -> soleTrader)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureClientDetailsController.show().url)
        )
      }
    }

    "redirect to cannot use service" when {
      "the business entity is other" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-entity")(BusinessEntityForm.businessEntity -> other)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CannotUseServiceController.show().url)
        )
      }
    }
  }
}
