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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CharityJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreCharityInformationHttpParser.{StoreCharityInformationFailureResponse, StoreCharityInformationSuccess}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreCharityInformationService

import scala.concurrent.Future

class CharityResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreCharityInformationService {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(CharityJourney)
  }

  object TestCharityResolverController extends CharityResolverController(
    mockControllerComponents,
    mockStoreCharityInformationService
  )

  lazy val testGetRequest = FakeRequest("GET", "/client/charity-resolver")

  "calling the resolve method on CharityResolverController" when {
    "the charity feature switch is on" when {
      "store charity information returns StoreCharityInformationSuccess" should {
        "goto email" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreCharityInformation(testVatNumber)(Future.successful(Right(StoreCharityInformationSuccess)))

          val res = await(TestCharityResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(routes.EmailRoutingController.route().url)
        }
      }
      "store charity information returns StoreCharityInformationFailureResponse" should {
        "throw internal server exception" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreCharityInformation(testVatNumber)(Future.successful(Left(StoreCharityInformationFailureResponse(INTERNAL_SERVER_ERROR))))

          intercept[InternalServerException] {
            await(TestCharityResolverController.resolve(testGetRequest.withSession(
              SessionKeys.vatNumberKey -> testVatNumber
            )))
          }
        }
      }
      "vat number is not in session" should {
        "goto capture vat number" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreCharityInformation(testVatNumber)(Future.successful(Right(StoreCharityInformationSuccess)))

          val res = await(TestCharityResolverController.resolve(testGetRequest))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(routes.CaptureVatNumberController.show().url)
        }
      }
    }

    "the charity feature switch is off" should {
      "throw not found exception" in {
        disable(CharityJourney)

        intercept[NotFoundException] {
          await(TestCharityResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )))
        }
      }
    }
  }

}

