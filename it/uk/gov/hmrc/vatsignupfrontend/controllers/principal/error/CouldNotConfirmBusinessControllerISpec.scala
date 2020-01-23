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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.error

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.SoleTrader

class CouldNotConfirmBusinessControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /error/could-not-confirm-business" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/error/could-not-confirm-business")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /error/could-not-confirm-business" should {
    "redirect to the capture vat number page" in {
      stubAuth(OK, successfulAuthResponse())

      val res = post("/error/could-not-confirm-business")()

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(principalRoutes.CaptureVatNumberController.show().url)
      )
    }
  }
  "redirect to the capture business entity page" in {
    stubAuth(OK, successfulAuthResponse())

    val res = post("/error/could-not-confirm-business",
      Map(
        vatNumberKey -> testVatNumber,
        companyNumberKey -> testCompanyNumber,
        companyUtrKey -> testCtUtr,
        businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
      ))()

    res should have(
      httpStatus(SEE_OTHER),
      redirectUri(principalRoutes.CaptureBusinessEntityController.show().url)
    )

    val session = getSessionMap(res)
    session.keys should contain(vatNumberKey)
    session.keys shouldNot contain(companyNumberKey)
    session.keys shouldNot contain(companyUtrKey)
    session.keys shouldNot contain(businessEntityKey)
  }
}
