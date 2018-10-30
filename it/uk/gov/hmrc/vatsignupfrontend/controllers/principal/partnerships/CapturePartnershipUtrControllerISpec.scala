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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.forms.PartnershipUtrForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CapturePartnershipUtrControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GeneralPartnershipJourney)
    enable(LimitedPartnershipJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(GeneralPartnershipJourney)
    disable(LimitedPartnershipJourney)
  }

  "GET /partnership-utr" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/partnership-utr")

      res should have(
        httpStatus(OK)
      )
    }

    "return an OK when only general partnerships is enabled" in {
      enable(GeneralPartnershipJourney)
      disable(LimitedPartnershipJourney)
      stubAuth(OK, successfulAuthResponse())

      val res = get("/partnership-utr")

      res should have(
        httpStatus(OK)
      )
    }

    "return an OK when only limited partnerships is enabled" in {
      disable(GeneralPartnershipJourney)
      enable(LimitedPartnershipJourney)
      stubAuth(OK, successfulAuthResponse())

      val res = get("/partnership-utr")

      res should have(
        httpStatus(OK)
      )
    }

    "return an Not Found if the feature switch is for both general and limited partnerships are disabled" in {
      disable(GeneralPartnershipJourney)
      disable(LimitedPartnershipJourney)
      stubAuth(OK, successfulAuthResponse())

      val res = get("/partnership-utr")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

  "POST /partnership-utr" should {
    "throw internal server error" in {
      stubAuth(OK, successfulAuthResponse())

      val res = post("/partnership-utr")(PartnershipUtrForm.partnershipUtr-> testSaUtr)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
      //TODO redirect to principal place of business page
    }

    "return an Not Found if the feature switch is for both general and limited partnerships are disabled" in {
      disable(GeneralPartnershipJourney)
      disable(LimitedPartnershipJourney)
      stubAuth(OK, successfulAuthResponse())

      val res = post("/partnership-utr")(PartnershipUtrForm.partnershipUtr-> testSaUtr)

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

}
