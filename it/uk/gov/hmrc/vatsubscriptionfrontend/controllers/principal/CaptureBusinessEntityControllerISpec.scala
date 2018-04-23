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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal

import play.api.http.Status._
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessEntityForm
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}


class CaptureBusinessEntityControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /business-type" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/business-type")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /business-type" should {

    "return a SEE_OTHER status and go to capture your details" when {
      "the business type is limited company" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> limitedCompany)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureYourDetailsController.show().url)
        )
      }
    }

    "return a SEE_OTHER status and go to capture your details" when {
      "the business type is sole trader" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> soleTrader)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureYourDetailsController.show().url)
        )
      }
    }

    "return a SEE_OTHER status and go to cannot use service" when {
      "the business type is other" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> other)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CannotUseServiceController.show().url)
        )
      }
    }

    "return a NOT_IMPLEMENTED status" when {
      "the business type is sole trader and a vat number is not on the enrolment" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> soleTrader)

        res should have(
          httpStatus(NOT_IMPLEMENTED)
        )
      }

      "the business type is limited company and a vat number is not on the enrolment" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type")(BusinessEntityForm.businessEntity -> limitedCompany)

        res should have(
          httpStatus(NOT_IMPLEMENTED)
        )
      }
    }
  }
}
