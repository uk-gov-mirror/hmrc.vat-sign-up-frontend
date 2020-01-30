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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipNoSAUTR, OptionalSautrJourney}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedPartnership}
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

class ResolvePartnershipUtrControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockVatControllerComponents {

  object TestResolvePartnershipUtrController extends ResolvePartnershipUtrController

  lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/resolve-partnership-utr")

  "Calling the resolve action of the Resolve Partnership Sautr controller" when {
    "the user has a IR-SA-PART-ORG enrolment" when {
      "the user is a Limited Partnership" should {
        "redirect to confirm limited partnership page" in {
          mockAuthRetrievePartnershipEnrolment()

          val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedPartnership)
          ))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.ConfirmLimitedPartnershipController.show().url)
          session(result) get SessionKeys.partnershipSautrKey should contain(testSaUtr)
        }
      }
      s"the user is a General Partnership && $GeneralPartnershipNoSAUTR is disabled" should {
        "redirect to confirm general partnership page" in {
          disable(GeneralPartnershipNoSAUTR)
          mockAuthRetrievePartnershipEnrolment()

          val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
          ))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.ConfirmGeneralPartnershipController.show().url)
          session(result) get SessionKeys.partnershipSautrKey should contain(testSaUtr)

        }
      }
      s"the user is a General Partnership && $GeneralPartnershipNoSAUTR is enabled" should {
        "redirect to confirm general partnership page" in {
          enable(GeneralPartnershipNoSAUTR)
          mockAuthRetrievePartnershipEnrolment()

          val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
          ))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.ConfirmGeneralPartnershipController.show().url)
          session(result) get SessionKeys.partnershipSautrKey should contain(testSaUtr)

        }
      }
    }
    "the user does not have a IR-SA-PART-ORG enrolment" when {
      s"the $GeneralPartnershipNoSAUTR is enabled && $OptionalSautrJourney is enabled " when {
        "the user is General Partnership" should {
          "go to the capture partnership UTR page" in {
            enable(GeneralPartnershipNoSAUTR)
            enable(OptionalSautrJourney)
            mockAuthRetrieveEmptyEnrolment()

            mockAuthRetrieveEmptyEnrolment()

            val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
              SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
          }
        }
      }
      s"the $GeneralPartnershipNoSAUTR is enabled && $OptionalSautrJourney is disabled" when {
        "the user is General Partnership" should {
          "go to the capture partnership UTR page" in {
            enable(GeneralPartnershipNoSAUTR)
            disable(OptionalSautrJourney)
            mockAuthRetrieveEmptyEnrolment()

            mockAuthRetrieveEmptyEnrolment()

            val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
              SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
          }
        }
      }

      s"$OptionalSautrJourney is enabled && $GeneralPartnershipNoSAUTR is disabled" when {
        "the user is a General Partnership" should {
          "go to the Do You Have A Utr page" in {
            enable(OptionalSautrJourney)
            disable(GeneralPartnershipNoSAUTR)
            mockAuthRetrieveEmptyEnrolment()

            val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
              SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.DoYouHaveAUtrController.show().url)
          }
        }
        "the user is a Limited Partnership" should {
          "go to the capture partnership UTR page" in {
            enable(OptionalSautrJourney)
            disable(GeneralPartnershipNoSAUTR)
            mockAuthRetrieveEmptyEnrolment()

            val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
              SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
          }
        }
      }
      s"$OptionalSautrJourney is disabled && $GeneralPartnershipNoSAUTR is disabled" should {
        "go to the capture partnership UTR page" in {
          disable(OptionalSautrJourney)
          disable(GeneralPartnershipNoSAUTR)

          mockAuthRetrieveEmptyEnrolment()

          val result = TestResolvePartnershipUtrController.resolve(testGetRequest)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
        }
      }
    }
  }
}
