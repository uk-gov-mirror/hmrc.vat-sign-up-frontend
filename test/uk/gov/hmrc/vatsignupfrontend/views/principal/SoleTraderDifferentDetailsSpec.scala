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
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalSignInDifferentDetails => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class SoleTraderDifferentDetailsSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  val testChangeLink = "testhref"

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.sole_trader_different_details(
    changeLink = testChangeLink)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Principal sign in different details view" should {

    val testPage = TestView(
      name = "Principal sign in different details view",
      title = messages.title,
      heading = messages.heading,
      page = page,
      haveSignOutInBanner = false
    )

    testPage.shouldHaveParaSeq(
      messages.line1,
      messages.line2
    )

    testPage.shouldHaveALink("diff-details", messages.linkText, testChangeLink)

    testPage.shouldHaveSignOutButton(isAgent = false)

  }

}

