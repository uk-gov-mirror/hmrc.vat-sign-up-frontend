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

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.forms.OtherBusinessEntityForm
import uk.gov.hmrc.vatsignupfrontend.forms.OtherBusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CaptureBusinessEntityOtherControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /business-type-other" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/business-type-other")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /business-type-other" when {

    "the business type is vat group" should {
      "return a SEE_OTHER status and go to vat group" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type-other")(OtherBusinessEntityForm.businessEntity -> vatGroup)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.VatGroupResolverController.resolve().url)
        )
      }
    }

    "the business type is division" should {
      "return a SEE_OTHER status and go to division resolver" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type-other")(OtherBusinessEntityForm.businessEntity -> division)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DivisionResolverController.resolve().url)
        )
      }
    }

    "the business type is unincorporated association" should {
      "return a SEE_OTHER status and go to unincorporated association resolver" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type-other")(OtherBusinessEntityForm.businessEntity -> unincorporatedAssociation)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.UnincorporatedAssociationResolverController.resolve().url)
        )
      }
    }

    "the business type is trust" should {
      "return a SEE_OTHER status and go to trust resolver" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type-other")(OtherBusinessEntityForm.businessEntity -> trust)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.TrustResolverController.resolve().url)
        )
      }
    }

    "the business type is registered society" should {
      "return a SEE_OTHER status and go to capture society company number page" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type-other")(OtherBusinessEntityForm.businessEntity -> registeredSociety)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureRegisteredSocietyCompanyNumberController.show().url)
        )
      }
    }

    "the business type is a charity" should {
      "return a SEE_OTHER status and go to charity resolver" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type-other")(OtherBusinessEntityForm.businessEntity -> charity)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CharityResolverController.resolve().url)
        )
      }
    }

    "the business type is a government organisation" should {
      "return a SEE_OTHER status and go to government organisation resolver" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/business-type-other")(OtherBusinessEntityForm.businessEntity -> governmentOrganisation)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.GovernmentOrganisationResolverController.resolve().url)
        )
      }
    }

  }

}
