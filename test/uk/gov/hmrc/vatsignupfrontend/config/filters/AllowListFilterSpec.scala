/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.config.filters

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results._
import play.api.mvc.{Action, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration}
import uk.gov.hmrc.allowlist.AkamaiAllowlistFilter
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig

class AllowListFilterSpec extends UnitSpec with GuiceOneServerPerSuite {

  val testString = "success"

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .bindings(bind(classOf[AkamaiAllowlistFilter]).to(classOf[AllowListFilter]))
    .configure(Configuration("feature-switch.enable-ip-allowlisting" -> true,
      "ip-allowlist.urls" -> "127.0.0.1"
    )).routes({
    case ("GET", "/index") => Action(Ok("success"))
    case _ => Action(Ok("failure"))
  }).build()

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "AllowListFilter" should {

    "redirects none allow listed ip" in {
      app.configuration.get[Boolean]("feature-switch.enable-ip-allowlisting") shouldBe true

      val fr = FakeRequest("GET", "/index").withHeaders(
        "True-Client-IP" -> "127.0.0.2"
      )
      Call(fr.method, fr.uri)
      val Some(result) = route(app, fr)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(appConfig.shutterPage)
    }

    "not block allow listed ip" in {
      app.configuration.get[Boolean]("feature-switch.enable-ip-allowlisting") shouldBe true
      val Some(result) = route(app, FakeRequest("GET", "/index").withHeaders(
        "True-Client-IP" -> "127.0.0.1"
      ))

      status(result) shouldBe OK
      contentAsString(result) shouldBe testString
    }
  }

}
