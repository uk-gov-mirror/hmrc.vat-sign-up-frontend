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

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents

class InvalidVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents with FeatureSwitching with BeforeAndAfterEach {

  object TestInvalidVatNumberController extends InvalidVatNumberController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/could-not-confirm-vat-number")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/could-not-confirm-vat-number")


  "Calling the show action of the Invalid Vat Number controller" when {
    "the known facts journey feature switch is enabled" should {
      "show the page" in {
        mockAuthAdminRole()
        val request = testGetRequest

        val result = TestInvalidVatNumberController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "Calling the submit action of the Invalid Vat Number controller" should {
    "the known facts journey feature switch is enabled" should {
      "return SEE_OTHER" in {
        mockAuthAdminRole()

        val result = TestInvalidVatNumberController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe routes.CaptureVatNumberController.show().url
      }
    }
  }

}
