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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.RegisteredSocietyJourney
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.GetCompanyNameStub.stubGetCompanyName
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse.NonPartnershipEntity

class CaptureRegisteredSocietyCompanyNumberControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(RegisteredSocietyJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    enable(RegisteredSocietyJourney)
  }

  "GET /registered-society-company-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/registered-society-company-number")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /registered-society-company-number" should {
    "return a Not Implemented" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))
      stubGetCompanyName(testCompanyNumber, NonPartnershipEntity)

      val res = post("/client/registered-society-company-number")(CompanyNumberForm.companyNumber -> testCompanyNumber)

      res should have(
        httpStatus(NOT_IMPLEMENTED)
        //TODO redirectUri(routes.ConfirmRegisteredSocietyNameController.show().url)
      )
    }
  }
}
