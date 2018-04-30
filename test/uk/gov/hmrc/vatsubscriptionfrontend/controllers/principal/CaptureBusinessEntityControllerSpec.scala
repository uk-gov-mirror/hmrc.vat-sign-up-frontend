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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrievals, ~}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsubscriptionfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsubscriptionfrontend.models.{BusinessEntity, LimitedCompany, Other, SoleTrader}

import scala.concurrent.Future

class CaptureBusinessEntityControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestCaptureBusinessEntityController extends CaptureBusinessEntityController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/business-type")

  def testPostRequest(entityTypeVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/business-type").withFormUrlEncodedBody(businessEntity -> entityTypeVal)

  "Calling the show action of the Capture Entity Type controller" should {
    "go to the Capture Entity Type page" in {
      mockAuthAdminRole()

      val result = TestCaptureBusinessEntityController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }


  "Calling the submit action of the Capture Business Entity controller" when {
    "form successfully submitted" should {

      "go to Identity Verification with limited company stored in session" when {
        "the business entity is limited company" in {
          mockAuthRetrieveVatDecEnrolment()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(limitedCompany)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(LimitedCompany))
        }
      }

      "go to Identity Verification with sole trader stored in session" when {
        "the business entity is sole trader" in {
          mockAuthRetrieveVatDecEnrolment()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(SoleTrader))
        }
      }

      "go to Cannot use service yet page" when {
        "the business entity is other" in {
          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(other)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CannotUseServiceController.show().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(Other))
        }
      }

      "go to check your answers" when {
        "no vat enrolment is on the profile" when {
          "the business entity is sole trader" in {
            mockAuthorise(
              retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
            )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))


            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

            val result = await(TestCaptureBusinessEntityController.submit(request))

            status(result) shouldBe Status.SEE_OTHER

            redirectLocation(result) should contain(routes.CheckYourAnswersController.show().url)

            result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(SoleTrader))
          }

          "the business entity is limited company" in {
            mockAuthorise(
              retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
            )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))


            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(limitedCompany)

            val result = await(TestCaptureBusinessEntityController.submit(request))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CheckYourAnswersController.show().url)

            result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(LimitedCompany))
          }
        }
      }

    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
        )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))


        val result = TestCaptureBusinessEntityController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}