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
import uk.gov.hmrc.vatsignupfrontend.forms.YesNoForm
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreVatNumberStub
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreVatNumberStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class MultipleVatCheckControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /more-than-one-vat-business" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/more-than-one-vat-business")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /more-than-one-vat-business" should {
    "return a redirect to vat number" when {
      "form value is YES" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

        val res = post("/more-than-one-vat-business")(YesNoForm.yesNo -> YesNoForm.yes)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatNumberController.show().url)
        )
      }
    }

    "return a redirect to business type" when {
      "form value is NO" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubStoreVatNumberSuccess(isFromBta = Some(false))

        val res = post("/more-than-one-vat-business")(YesNoForm.yesNo -> YesNoForm.no)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBusinessEntityController.show().url)
        )
      }
    }
  }

}
