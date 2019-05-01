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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.SkipCidCheck
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.{
  CaptureYourDetailsController, ConfirmYourRetrievedUserDetailsController, DirectDebitResolverController, CaptureVatNumberController
}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.{AuthProfile, IRSA}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.{MockCitizenDetailsService, MockStoreNinoService}

class SoleTraderResolverControllerSpec extends UnitSpec
                                       with GuiceOneAppPerSuite
                                       with MockControllerComponents
                                       with MockCitizenDetailsService
                                       with MockStoreNinoService {

  object TestSoleTraderResolverController extends SoleTraderResolverController(
    mockControllerComponents,
    mockCitizenDetailsService,
    mockStoreNinoService
  )

  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(SessionKeys.vatNumberKey -> testVatNumber)

  "Calling the resolve action" when {

    "the SkipCidCheck feature switch is disabled" when {
      "the user has a nino on their auth profile" when {
        "citizen details successfully returns the user details" should {
          "redirect to ConfirmYourRetrievedUserDetailsController with the user details in session and a nino source of AuthProfile" in {
            disable(SkipCidCheck)
            mockAuthNinoRetrieval(Some(testNino))
            mockCitizenDetailsSuccessByNino(testNino, testUserDetails)
            mockStoreNinoSuccess(testVatNumber, testNino, AuthProfile)

            val res = await(TestSoleTraderResolverController.resolve(request))

            status(res) shouldBe SEE_OTHER
            redirectLocation(res) should contain(ConfirmYourRetrievedUserDetailsController.show().url)
            res.session.get(SessionKeys.userDetailsKey) should contain(Json.toJson(testUserDetails).toString())
            res.session.get(SessionKeys.ninoSourceKey) should contain(Json.toJson(AuthProfile).toString())
          }
        }
        "citizen details fails" should {
          "throw an InternalServerException" in {
            mockAuthNinoRetrieval(Some(testNino))
            mockCitizenDetailsFailureByNino(testNino)

            intercept[InternalServerException](await(TestSoleTraderResolverController.resolve(request)))
          }
        }
      }
      "the user has an IRSA enrolment" when {
        "citizen details successfully returns the user details" should {
          "redirect to ConfirmYourRetrievedUserDetailsController with the user details in session and a nino source of IRSA" in {
            mockAuthNinoRetrieval(None, hasIrsaEnrolment = true)
            mockCitizenDetailsSuccessBySautr(testSaUtr, testUserDetails)

            val res = await(TestSoleTraderResolverController.resolve(request))

            status(res) shouldBe SEE_OTHER
            redirectLocation(res) should contain(ConfirmYourRetrievedUserDetailsController.show().url)
            res.session.get(SessionKeys.userDetailsKey) should contain(Json.toJson(testUserDetails).toString())
            res.session.get(SessionKeys.ninoSourceKey) should contain(Json.toJson(IRSA).toString())
          }
        }
        "citizen details fails" should {
          "throw an InternalServerException" in {
            mockAuthNinoRetrieval(None, hasIrsaEnrolment = true)
            mockCitizenDetailsFailureBySautr(testSaUtr)

            intercept[InternalServerException](await(TestSoleTraderResolverController.resolve(request)))
          }
        }
      }

      "user has no nino or IRSA enrolment on profile" should {
        "redirect to CaptureYourDetailsController" in {
          disable(SkipCidCheck)
          mockAuthNinoRetrieval(None)
          val res = await(TestSoleTraderResolverController.resolve(request))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) should contain(CaptureYourDetailsController.show().url)
        }
      }
    }

    "the SkipCidCheck feature switch is enabled" when {
      "user has no VAT number in session" should {
        "redirect to capture VAT number" in {
          enable(SkipCidCheck)
          mockAuthNinoRetrieval(Some(testNino))

          val res = await(TestSoleTraderResolverController.resolve(FakeRequest()))
          status(res) shouldBe SEE_OTHER
          redirectLocation(res) should contain(CaptureVatNumberController.show().url)
        }
      }

      "user has no nino or IRSA enrolment on profile" should {
        "redirect to the CaptureNinoController" in {
          enable(SkipCidCheck)
          mockAuthNinoRetrieval(None)
          val res = await(TestSoleTraderResolverController.resolve(request))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) should contain(routes.CaptureNinoController.show().url)
        }
      }

      "the user has a nino on their auth profile" should {
        "redirect to DirectDebitResolver" in {
          enable(SkipCidCheck)
          mockAuthNinoRetrieval(Some(testNino))
          mockStoreNinoSuccess(testVatNumber, testNino, AuthProfile)

          val res = await(TestSoleTraderResolverController.resolve(request))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) should contain(DirectDebitResolverController.show().url)
        }
      }

      "store NINO fails" should {
        "throw an InternalServerException" in {
          enable(SkipCidCheck)
          mockAuthNinoRetrieval(Some(testNino))
          mockStoreNinoFailure(testVatNumber, testNino, AuthProfile)

          intercept[InternalServerException](await(TestSoleTraderResolverController.resolve(request)))
        }
      }
    }
  }
}
