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

import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{Base, SignUpCompleteClient => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class SignUpCompleteClientViewSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.sign_up_complete_client()(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  lazy val appConfig = app.injector.instanceOf[AppConfig]

  lazy val document = Jsoup.parse(page.body)

  "Sign up complete client view" should {

    val testPage = TestView(
      name = "Sign up complete client View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHaveParaSeq(
      messages.line1
    )

    testPage.shouldHaveBulletSeq(
      messages.bullet1,
      messages.bullet2,
      messages.bullet3,
      messages.bullet4
    )

    testPage.shouldHaveContinueButtonLink(appConfig.btaUrl, Base.continue)
  }

}
