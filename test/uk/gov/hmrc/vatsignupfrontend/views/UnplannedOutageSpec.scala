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

package uk.gov.hmrc.vatsignupfrontend.views

import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{UnplannedOutage => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig

class UnplannedOutageSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)
  lazy val appConfig = new AppConfig(configuration, env)

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.unplanned_outage()(
    FakeRequest(),
    applicationMessages,
    appConfig
  )

  "The Unplanned outage view" should {

    val testPage = TestView(
      name = "Unplanned outage View",
      title = messages.title,
      heading = messages.heading,
      page = page,
      haveSignOutInBanner = false
    )

    testPage.shouldHaveParaSeq(
      messages.line1,
      messages.line2
    )

    testPage.shouldHaveBulletSeq(
      messages.bullet1,
      messages.bullet2,
      messages.bullet3
    )

    testPage.shouldHaveALink("vatServices", messages.link1, appConfig.UnplannedOutagePage.vatServices)
    testPage.shouldHaveALink("help", messages.link2, appConfig.UnplannedOutagePage.help)
    testPage.shouldHaveALink("mtdReady", messages.link3, appConfig.UnplannedOutagePage.mtdReady)

  }

}

