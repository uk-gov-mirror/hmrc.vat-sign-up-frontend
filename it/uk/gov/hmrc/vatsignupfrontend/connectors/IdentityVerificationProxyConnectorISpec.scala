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

package uk.gov.hmrc.vatsignupfrontend.connectors

import java.time.LocalDate
import java.util.UUID

import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.IdentityVerificationStub
import uk.gov.hmrc.vatsignupfrontend.httpparsers.IdentityVerificationProxyHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, UserDetailsModel}

class IdentityVerificationProxyConnectorISpec extends ComponentSpecBase {

  lazy val connector: IdentityVerificationProxyConnector = app.injector.instanceOf[IdentityVerificationProxyConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val testUserDetails = UserDetailsModel(
    firstName = UUID.randomUUID().toString,
    lastName = UUID.randomUUID().toString,
    dateOfBirth = DateModel.dateConvert(LocalDate.now()),
    nino = testNino
  )

  "start" when {
    "Backend returns a CREATED response" should {
      "return StoreNinoSuccess" in {

        val response = IdentityVerificationProxySuccessResponse("", "")
        IdentityVerificationStub.stubIdentityVerificationProxy(testUserDetails)(CREATED, response)

        val res = connector.start(testUserDetails)

        await(res) shouldBe Right(response)
      }
    }


  }

}