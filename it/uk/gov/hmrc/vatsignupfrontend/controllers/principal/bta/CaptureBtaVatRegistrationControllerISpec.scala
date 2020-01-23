/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.bta

import java.time.LocalDate

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.vatRegistrationDateKey
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.BTAClaimSubscription
import uk.gov.hmrc.vatsignupfrontend.forms.VatRegistrationDateForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.DateModel

class CaptureBtaVatRegistrationControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(BTAClaimSubscription)
  }

  "GET /bta/vat-registration-date" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/bta/vat-registration-date")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "if feature switch is disabled" should {
    "return a not found" in {
      disable(BTAClaimSubscription)

      val res = get("/bta/vat-registration-date")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

  "POST /bta/vat-registration-date" should {
    "return a redirect" in {
      stubAuth(OK, successfulAuthResponse())

      val yesterday = DateModel.dateConvert(LocalDate.now().minusDays(1))

      val res = post("/bta/vat-registration-date")(vatRegistrationDate + ".dateDay" -> yesterday.day,
        vatRegistrationDate + ".dateMonth" -> yesterday.month,
        vatRegistrationDate + ".dateYear" -> yesterday.year)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.BtaBusinessPostCodeController.show().url)
      )

      val session = getSessionMap(res)
      session.keys should contain(vatRegistrationDateKey)
    }
  }

}
