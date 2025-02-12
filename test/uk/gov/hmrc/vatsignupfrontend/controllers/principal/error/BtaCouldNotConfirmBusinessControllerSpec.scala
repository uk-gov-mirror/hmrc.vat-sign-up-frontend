/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents

class BtaCouldNotConfirmBusinessControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockVatControllerComponents {

  object TestCouldNotConfirmBusinessController extends BtaCouldNotConfirmBusinessController

  lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/error/bta/could-not-confirm-business")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/error/bta/could-not-confirm-business")

  "Calling the show action of the Could not confirm business controller" should {
    "show the could not confirm business page" in {
      mockAuthAdminRole()
      val request = testGetRequest

      val result = TestCouldNotConfirmBusinessController.show(request)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Could not confirm business controller" should {
    "redirect to capture your vat number page" in {
      mockAuthAdminRole()

      val result = TestCouldNotConfirmBusinessController.submit(testPostRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(mockAppConfig.btaAddVatUrl)
    }
  }

}
