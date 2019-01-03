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
import play.api.test.Helpers.{redirectLocation, _}
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.{CaptureYourDetailsController, ConfirmYourRetrievedUserDetailsController}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.{AuthProfile, IRSA}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockCitizenDetailsService

import scala.concurrent.Future

class SoleTraderResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents with MockCitizenDetailsService {

  object TestSoleTraderResolverController extends SoleTraderResolverController(
    mockControllerComponents,
    mockCitizenDetailsService
  )

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "Calling the resolve action" when {
    "the user has a nino on their auth profile" when {
      "citizen details successfully returns the user details" should {
        "redirect to ConfirmYourRetrievedUserDetailsController with the user details in session and a nino source of AuthProfile" in {
          mockAuthorise(retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.nino))(
            Future.successful(
              new ~(
                Some(Admin),
                new ~(
                  Enrolments(Set.empty),
                  Some(testNino)
                )
              )
            )
          )
          mockCitizenDetailsSuccessByNino(testNino, testUserDetails)

          val res = await(TestSoleTraderResolverController.resolve(request))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) should contain(ConfirmYourRetrievedUserDetailsController.show().url)
          res.session.get(SessionKeys.userDetailsKey) should contain(Json.toJson(testUserDetails).toString())
          res.session.get(SessionKeys.ninoSourceKey) should contain(Json.toJson(AuthProfile).toString())
        }
      }
      "citizen details fails" should {
        "throw an InternalServerException" in {
          mockAuthorise(retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.nino))(
            Future.successful(
              new ~(
                Some(Admin),
                new ~(
                  Enrolments(Set.empty),
                  Some(testNino)
                )
              )
            )
          )
          mockCitizenDetailsFailureByNino(testNino)

          intercept[InternalServerException](await(TestSoleTraderResolverController.resolve(request)))
        }
      }
    }
    "the user has an IRSA enrolment" when {
      "citizen details successfully returns the user details" should {
        "redirect to ConfirmYourRetrievedUserDetailsController with the user details in session and a nino source of IRSA" in {
          mockAuthorise(retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.nino))(
            Future.successful(
              new ~(
                Some(Admin),
                new ~(
                  Enrolments(Set(testIRSAEnrolment)),
                  None
                )
              )
            )
          )
          mockCitizenDetailsSuccessBySautr(testSaUtr, testUserDetails)

          val res = TestSoleTraderResolverController.resolve(request)

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) should contain(ConfirmYourRetrievedUserDetailsController.show().url)
          res.session.get(SessionKeys.userDetailsKey) should contain(Json.toJson(testUserDetails).toString())
          res.session.get(SessionKeys.ninoSourceKey) should contain(Json.toJson(IRSA).toString())        }
      }
      "citizen details fails" should {
        "throw an InternalServerException" in {
          mockAuthorise(retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.nino))(
            Future.successful(
              new ~(
                Some(Admin),
                new ~(
                  Enrolments(Set(testIRSAEnrolment)),
                  None
                )
              )
            )
          )
          mockCitizenDetailsFailureBySautr(testSaUtr)

          intercept[InternalServerException](await(TestSoleTraderResolverController.resolve(request)))
        }
      }
    }
    "user has no nino or IRSA enrolment on profile" should {
      "redirect to CaptureYourDetailsController" in {
        mockAuthorise(retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.nino))(
          Future.successful(
            new ~(
              Some(Admin),
              new ~(
                Enrolments(Set.empty),
                None
              )
            )
          )
        )

        val res = TestSoleTraderResolverController.resolve(request)

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) should contain(CaptureYourDetailsController.show().url)
      }
    }
  }
}
