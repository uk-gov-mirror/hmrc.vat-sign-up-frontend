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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalCaptureRegisteredSocietyUtr => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.RegisteredSocietyUtrForm._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class CaptureRegisteredSocietyUtrSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)
  val appConfig = new AppConfig(configuration, env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_registered_society_utr(
    registeredSocietyUtrForm = registeredSocietyUtrForm.form,
    postAction = testCall)(
    FakeRequest(),
    applicationMessages,
    appConfig
  )

  "The Capture Registered Society Utr view" should {

    val testPage = TestView(
      name = "Capture Registered Society Utr View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHaveForm("Registered Society Utr Form")(actionCall = testCall)

    testPage.shouldHaveTextField(registeredSocietyUtr, messages.heading, hideLabel = false)

    testPage.shouldHavePara(messages.line1)

    testPage.shouldHaveALink("lost-Utr", messages.link, appConfig.findLostUtrNumberUrl)

    testPage.shouldHaveContinueButton()
  }

}

