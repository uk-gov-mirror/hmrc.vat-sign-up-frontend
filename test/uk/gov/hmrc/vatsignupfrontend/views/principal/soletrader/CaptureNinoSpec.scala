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

package uk.gov.hmrc.vatsignupfrontend.views.principal.soletrader

import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.forms.NinoForm._
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{CaptureNino => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class CaptureNinoSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.soletrader.capture_nino(
    ninoForm = ninoForm(isAgent = false).form,
    postAction = testCall)(
      FakeRequest(),
      applicationMessages,
      new AppConfig(configuration, env
    )
  )

  "Capture NINO page" should {
    val testView = TestView(
      name = "Capture NINO view",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testView.shouldHaveForm("Capture NINO form")(actionCall = testCall)

    testView.shouldHaveHint(messages.formHint)

    testView.shouldHaveContinueButton()
  }

}
