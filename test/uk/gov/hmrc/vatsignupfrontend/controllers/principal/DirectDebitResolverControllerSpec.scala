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
import play.api.http.Status.SEE_OTHER
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.DirectDebitTermsJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents

class DirectDebitResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestDirectDebitResolverController extends DirectDebitResolverController(mockControllerComponents)

  private def sessionValuesDD(directDebitFlag: Option[String]): Iterable[(String, String)] = directDebitFlag map (hasDirectDebitKey -> _)

  private def sessionValuesMigrated(isMigratedFlag: Option[String]): Iterable[(String, String)] = isMigratedFlag map (isMigratedKey -> _)

  def testGetRequest(directDebitFlag: Option[String] = None, isMigratedFlag: Option[String] = None): FakeRequest[AnyContentAsEmpty.type] = {
    FakeRequest("GET", "/direct-debit-resolver")
      .withSession(sessionValuesDD(directDebitFlag).toSeq: _*)
      .withSession(sessionValuesMigrated(isMigratedFlag).toSeq: _*)
  }

  "Calling the show action of the Direct Debit Resolver controller" when {

    "isMigratedKey value is in session" should {
      lazy val result = TestDirectDebitResolverController.show(testGetRequest(isMigratedFlag = Some("true")))

      "return status SEE_OTHER (303)" in {
        mockAuthAdminRole()

        status(result) shouldBe SEE_OTHER
      }

      "redirect to Send Application page" in {
        redirectLocation(result) shouldBe Some(routes.SendYourApplicationController.show().url)
      }
    }

    "isMigratedKey value is not in session" when {
      "hasDirectDebitKey in session and DirectDebitTermsJourney feature switch has been enabled" should {

        lazy val result = TestDirectDebitResolverController.show(testGetRequest(directDebitFlag = Some("true")))

        "return status SEE_OTHER (303)" in {
          mockAuthAdminRole()
          enable(DirectDebitTermsJourney)

          status(result) shouldBe SEE_OTHER
        }

        "redirect to the Direct Debit T&Cs Agree page" in {
          redirectLocation(result) shouldBe Some(routes.DirectDebitTermsAndConditionsController.show().url)
        }
      }

      "hasDirectDebitKey in session and DirectDebitTermsJourney feature switch has been disabled" should {

        lazy val result = TestDirectDebitResolverController.show(testGetRequest(directDebitFlag = Some("true")))

        "return status SEE_OTHER (303)" in {
          mockAuthAdminRole()
          disable(DirectDebitTermsJourney)

          status(result) shouldBe SEE_OTHER
        }

        "redirect to the Capture Email page" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
        }
      }
    }

    "prerequisite data is missing from session" when {

      "the direct debit feature switch has been enabled" should {

        lazy val result = TestDirectDebitResolverController.show(testGetRequest())

        "return status SEE_OTHER (303)" in {
          mockAuthAdminRole()
          enable(DirectDebitTermsJourney)

          status(result) shouldBe SEE_OTHER
        }

        "redirect to the Capture Email page" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
        }
      }

      "Direct Debit feature switch is disabled" should {

        lazy val result = TestDirectDebitResolverController.show(testGetRequest())

        "return status SEE_OTHER (303)" in {
          mockAuthAdminRole()
          disable(DirectDebitTermsJourney)

          status(result) shouldBe SEE_OTHER
        }

        "redirect to the Capture Email page" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
        }
      }
    }
  }
}
