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
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch.KnownFactsJourney
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CannotUseServicesControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /client/cannot-use-service-yet" should {
    "return an OK" in {

      enable(KnownFactsJourney)

      stubAuth(OK, successfulAuthResponse())

      val res = get("/client/cannot-use-service-yet")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /client/cannot-use-service-yet" should {
    "redirect to the capture client details page" in {

      enable(KnownFactsJourney)

      stubAuth(OK, successfulAuthResponse())

      val res = post("/client/cannot-use-service-yet")()

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureClientDetailsController.show().url)
      )
    }
  }

  "Making a request to /client/cannot-use-service-yet when feature switch not enabled" should {
    "return NotFound" in {
      disable(KnownFactsJourney)
      stubAuth(OK, successfulAuthResponse())

      val res = get("/client/cannot-use-service-yet")

      res should have(
        httpStatus(NOT_FOUND)
      )

    }
  }

}
