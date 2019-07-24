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

import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{GotSoftware => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class GotSoftwareViewSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  val conf = new AppConfig(configuration, env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.got_software(testCall)(
    FakeRequest(),
    applicationMessages,
    conf
  )

  lazy val document = Jsoup.parse(page.body)

  "Got Software" should {

    val testPage = TestView(
      name = "Got Software View",
      title = messages.title,
      heading = messages.heading,
      page = page,
      haveSignOutInBanner = false
    )

    testPage.shouldHavePara(messages.para)

    testPage.shouldHaveBulletSeq(
      messages.bullet1,
      messages.bullet2
    )

    testPage.shouldHaveALink(messages.linkId, messages.softwareCompatible, conf.makingTaxDigitalSoftwareUrl)

    testPage shouldHaveSubmitButton messages.signUp

  }

}
