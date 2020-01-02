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

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.DirectDebitTermsJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.ControllerSpec

class DirectDebitTermsAndConditionsControllerSpec extends ControllerSpec {

  object TestDirectDebitTermsAndConditionsController extends DirectDebitTermsAndConditionsController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/direct-debit-terms-and-conditions")
  lazy val testPostRequest = FakeRequest("POST", "/direct-debit-terms-and-conditions")

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(DirectDebitTermsJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(DirectDebitTermsJourney)
  }

  "Calling the show action of the Direct Debit Terms and Conditions controller" should {

    lazy val result = TestDirectDebitTermsAndConditionsController.show(testGetRequest)

    "return status OK (200)" in {
      mockAuthAdminRole()
      status(result) shouldBe Status.OK
    }

    "return text/html as the content type" in {
      contentType(result) shouldBe Some("text/html")
    }

    "have charset utf-8" in {
      charset(result) shouldBe Some("utf-8")
    }

    "render the Direct Debit Terms and Conditions View" in {
      titleOf(result) shouldBe MessageLookup.PrincipalDirectDebitTermsAndConditions.title
    }
  }

  "return a Not Found Exception" when {
    "the feature switch is disabled" in {
      disable(DirectDebitTermsJourney)

      intercept[NotFoundException](
        await(TestDirectDebitTermsAndConditionsController.show(testGetRequest))
      )
    }
  }

  "Calling the submit action of the Direct Debit Terms and Conditions controller" should {
    lazy val result = TestDirectDebitTermsAndConditionsController.submit(testPostRequest)

    "return status SEE_OTHER (303)" in {
      mockAuthAdminRole()
      status(result) shouldBe Status.SEE_OTHER
    }

    s"have a redirectLocation to '${routes.CaptureEmailController.show().url}'" in {
      redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
    }

    "Add to session the SessionKey 'acceptedDirectDebitTermsKey' with value set to 'true'" in {
      session(result).get(SessionKeys.acceptedDirectDebitTermsKey) shouldBe Some("true")
    }
  }
}
