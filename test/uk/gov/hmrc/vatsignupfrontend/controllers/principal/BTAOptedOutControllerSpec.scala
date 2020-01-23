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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockClaimSubscriptionService
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

class BTAOptedOutControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockVatControllerComponents with MockClaimSubscriptionService {

  object TestBTAOptedOutController extends BTAOptedOutController

  val vrnFromBTA = "123456789"
  lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", s"/vat-number/$vrnFromBTA")

  "redirect" should {
    "redirect to the Capture VAT number page" in {

      mockAuthAdminRole()

      val result = TestBTAOptedOutController.redirect(vrnFromBTA)(testGetRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ResolveVatNumberController.resolve().url)
    }
  }
}
