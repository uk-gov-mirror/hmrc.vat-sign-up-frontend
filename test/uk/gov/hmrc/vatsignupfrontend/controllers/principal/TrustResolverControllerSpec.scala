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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.TrustJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreTrustInformationHttpParser.{StoreTrustInformationFailureResponse, StoreTrustInformationSuccess}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreTrustInformationService

import scala.concurrent.Future

class TrustResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreTrustInformationService {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(TrustJourney)
  }

  object TestTrustResolverController extends TrustResolverController(
    mockControllerComponents,
    mockStoreTrustInformationService
  )

  lazy val testGetRequest = FakeRequest("GET", "/trust-resolver")

  "calling the resolve method on TrustResolverController" when {
    "the group feature switch is on" when {
      "store group information returns StoreTrustInformationSuccess" should {
        "goto agree capture email" in {
          mockAuthAdminRole()
          mockStoreTrustInformation(testVatNumber)(Future.successful(Right(StoreTrustInformationSuccess)))

          val res = await(TestTrustResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(routes.AgreeCaptureEmailController.show().url)
        }
      }
      "store group information returns StoreTrustInformationFailureResponse" should {
        "throw internal server exception" in {
          mockAuthAdminRole()
          mockStoreTrustInformation(testVatNumber)(Future.successful(Left(StoreTrustInformationFailureResponse(INTERNAL_SERVER_ERROR))))

          intercept[InternalServerException] {
            await(TestTrustResolverController.resolve(testGetRequest.withSession(
              SessionKeys.vatNumberKey -> testVatNumber
            )))
          }
        }
      }
      "vat number is not in session" should {
        "goto resolve vat number" in {
          mockAuthAdminRole()
          mockStoreTrustInformation(testVatNumber)(Future.successful(Right(StoreTrustInformationSuccess)))

          val res = await(TestTrustResolverController.resolve(testGetRequest))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(routes.ResolveVatNumberController.resolve().url)
        }
      }
    }

    "the group feature switch is off" should {
      "throw not found exception" in {
        disable(TrustJourney)

        intercept[NotFoundException] {
          await(TestTrustResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )))
        }
      }
    }
  }

}

