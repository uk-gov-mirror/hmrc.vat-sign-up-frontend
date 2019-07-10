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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.forms.PartnershipUtrForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CapturePartnershipUtrControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /partnership-utr" should {
    s"return an $OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/partnership-utr")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "GET /partnership-no-utr" should {
    s"return a $SEE_OTHER to check your answers" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/partnership-no-utr")

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CheckYourAnswersPartnershipsController.show().url)
      )
    }
  }

  "POST /partnership-utr" should {
    s"return a $SEE_OTHER to PPOB page" in {
      stubAuth(OK, successfulAuthResponse())

      val res = post("/partnership-utr")(PartnershipUtrForm.partnershipUtr-> testSaUtr)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.PrincipalPlacePostCodeController.show().url)
      )
    }
  }

}
