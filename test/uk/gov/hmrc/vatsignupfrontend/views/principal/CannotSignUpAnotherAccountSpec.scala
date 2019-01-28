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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalCannotSignUpAnotherAccount => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class CannotSignUpAnotherAccountSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)
  lazy val appConfig = new AppConfig(configuration, env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.cannot_sign_up_another_account(
    )(
    FakeRequest(),
    applicationMessages,
    appConfig
  )

  "The Principal Cannot Sign Up Another Account view" should {

    val testPage = TestView(
      name = "Principal Cannot Sign Up Another Account View",
      title = messages.title,
      heading = messages.heading,
      page = page,
      haveSignOutInBanner = false
    )

    testPage.shouldHaveParaSeq(
      messages.line1,
      messages.line2,
      messages.line3
    )

    testPage.shouldHaveContinueButtonLink(href = appConfig.createAccountUrl, text = messages.buttonText)
  }

}

