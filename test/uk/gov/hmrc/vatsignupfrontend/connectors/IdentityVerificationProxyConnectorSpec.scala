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

import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, UserDetailsModel}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

class IdentityVerificationProxyConnectorSpec extends UnitSpec with MockitoSugar {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val appConfig = new AppConfig(configuration, env)

  object TestIdentityVerificationProxyConnector extends IdentityVerificationProxyConnector(
    mock[HttpClient],
    appConfig
  )

  val testUserDetails = UserDetailsModel(
    "Jim",
    "Ferguson",
    testNino,
    DateModel("23", "04", "1948")
  )

  "The IdentityVerificationProxyConnector.startIdentityVerificationRequest" should {

    "storing the vat number" in {
      val request = TestIdentityVerificationProxyConnector.startIdentityVerificationRequest(testUserDetails)
      val redirectionUrl = appConfig.baseUrl + routes.IdentityVerificationCallbackController.continue().url
      request shouldBe
        Json.parse(s"""
          | {
          |  "origin" : "mtd-vat",
          |  "completionURL":"$redirectionUrl",
          |  "failureURL":"$redirectionUrl",
          |  "confidenceLevel" : 200,
          |  "userData" : {
          |   "firstName":"Jim",
          |   "lastName" : "Ferguson",
          |   "dateOfBirth": "1948-04-23",
          |   "nino": "$testNino"
          |  }
          |}
        """.stripMargin)
    }

  }

}
