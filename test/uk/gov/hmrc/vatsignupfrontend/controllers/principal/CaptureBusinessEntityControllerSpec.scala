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
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models._

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
    "form successfully submitted" when {
      "the business entity is sole trader" should {
        "go to sole trader resolver with sole trader stored in session" in {
          mockAuthAdminRole()
          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(soleTrader)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(soletrader.routes.SoleTraderResolverController.resolve().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(SoleTrader))
        }
      }

      "the business entity is limited company" should {
          "go to capture company number controller" in {
            mockAuthAdminRole()
            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(limitedCompany)

            val result = await(TestCaptureBusinessEntityController.submit(request))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CaptureCompanyNumberController.show().url)

            result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(LimitedCompany))
          }
        }

      "the business entity is general partnership" should {
        "go to resolve partnership utr controller" in {
          mockAuthAdminRole()
          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(generalPartnership)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(partnerships.routes.ResolvePartnershipUtrController.resolve().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(GeneralPartnership))
        }
      }

      "the business entity is limited partnership" should {
        "go to capture partnership company number controller" in {
          mockAuthAdminRole()
          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(limitedPartnership)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(partnerships.routes.CapturePartnershipCompanyNumberController.show().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(LimitedPartnership))
        }
      }

      "the business entity is vat group" should {
        "goto vat group resolver" in {
          mockAuthAdminRole()
          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(vatGroup)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.VatGroupResolverController.resolve().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(VatGroup))
        }
      }

      "the business entity is unincorporated association" should {
        "goto unincorporated association resolver" in {
          mockAuthAdminRole()
          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(unincorporatedAssociation)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UnincorporatedAssociationResolverController.resolve().url)

          result.session get SessionKeys.businessEntityKey should contain(
            BusinessEntitySessionFormatter.toString(UnincorporatedAssociation)
          )
        }
      }

      "the business entity is trust" should {
        "goto trust resolver" in {
          mockAuthAdminRole()
          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(trust)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TrustResolverController.resolve().url)

          result.session get SessionKeys.businessEntityKey should contain(
            BusinessEntitySessionFormatter.toString(Trust)
          )
        }
      }

      "the business entity is other" should {
        "go to Cannot use service yet page" in {
          mockAuthAdminRole()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = testPostRequest(other)

          val result = await(TestCaptureBusinessEntityController.submit(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CannotUseServiceController.show().url)

          result.session get SessionKeys.businessEntityKey should contain(BusinessEntitySessionFormatter.toString(Other))
        }
      }

    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthAdminRole()

        val result = TestCaptureBusinessEntityController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

  }

}
