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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.models.SoleTrader
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.sign_up_complete_client
import play.api.i18n.Messages.Implicits._

class SignUpCompleteClientControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestInformationReceivedController extends SignUpCompleteClientController(mockControllerComponents)

  implicit lazy val testGetRequest = FakeRequest("GET", "/sign-up-complete-client")

  "Calling the show action of the information received controller" should {
    "show the information received page" in {
      mockAuthAdminRole()
      val request = testGetRequest

      val result = TestInformationReceivedController.show(request)
      status(result) shouldBe Status.OK
      contentAsString(result) shouldBe sign_up_complete_client().body
    }
  }

}
