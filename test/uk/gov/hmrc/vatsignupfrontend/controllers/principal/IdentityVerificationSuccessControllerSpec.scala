/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.businessEntityKey
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{BusinessEntity, LimitedCompany, Other, SoleTrader}

class IdentityVerificationSuccessControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestIdentityVerificationSuccessController extends IdentityVerificationSuccessController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/confirmed-identity")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirmed-identity")

  "Calling the show action of the Identity Verification Success controller" should {
    "show the success Identity Verification page" in {
      mockAuthAdminRole()
      val request = testGetRequest

      val result = TestIdentityVerificationSuccessController.show(request)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Identity Verification Success controller" when {

    def request(businessEntity: BusinessEntity) = FakeRequest() withSession(
      businessEntityKey -> BusinessEntitySessionFormatter.toString(businessEntity)
    )

    "the business entity is Sole Trader" should {
      "redirect to the Agree Capture Email page" in {
        mockAuthAdminRole()
        val result = TestIdentityVerificationSuccessController.submit(request(SoleTrader))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.AgreeCaptureEmailController.show().url)
      }
    }

    "the business entity is Limited Company" should {
      "redirect to the Capture Company Number page" in {
        mockAuthAdminRole()
        val result = TestIdentityVerificationSuccessController.submit(request(LimitedCompany))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CaptureCompanyNumberController.show().url)
      }
    }

    "the business entity is Other" should {
      "redirect to the Capture Business Entity page" in {
        mockAuthAdminRole()
        val result = TestIdentityVerificationSuccessController.submit(request(Other))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CaptureBusinessEntityController.show().url)
      }
    }

  }

}
