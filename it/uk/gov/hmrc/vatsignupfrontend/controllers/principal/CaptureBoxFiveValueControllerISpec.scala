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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{AdditionalKnownFacts, FeatureSwitching}
import uk.gov.hmrc.vatsignupfrontend.forms.BoxFiveValueForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CaptureBoxFiveValueControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(AdditionalKnownFacts)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(AdditionalKnownFacts)
  }

  "GET /box-5-figure" when {
    "the AdditionalKnownFacts feature switch is enabled" should {
      "return an OK" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/box-5-figure")

        res should have(
          httpStatus(OK)
        )
      }
    }
    "the AdditionalKNownFacts feature switch is disabled" should {
      "return a Not Found" in {
        disable(AdditionalKnownFacts)
        stubAuth(OK, successfulAuthResponse())

        val res = get("/box-5-figure")

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }

  }

  "POST /box-5-figure" when {
    "the AdditionalKnownFacts feature switch is enabled" should {
      "return a Not Implemented" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/box-5-figure")(BoxFiveValueForm.boxFiveValue -> testBoxFiveValue)

        res should have(
          httpStatus(NOT_IMPLEMENTED)
          // TODO: Redirect to most recent vat payment page
        )
      }
    }
  }
}
