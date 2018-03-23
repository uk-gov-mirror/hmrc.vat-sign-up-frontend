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

package uk.gov.hmrc.vatsubscriptionfrontend.views.principal

import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsubscriptionfrontend.assets.MessageLookup.{FailedMatching => messages}
import uk.gov.hmrc.vatsubscriptionfrontend.config.AppConfig
import uk.gov.hmrc.vatsubscriptionfrontend.views.ViewSpec

class FailedMatchingSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsubscriptionfrontend.views.html.principal.failed_matching(
    postAction = testCall)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Failed failed matching view" should {

    val testPage = TestView(
      name = "Failed failed matching View",
      title = messages.title,
      heading = messages.heading,
      page = page,
      haveSignOutInBanner = false
    )

    testPage.shouldHavePara(
      messages.line1
    )

    testPage.shouldHaveForm("Failed failed matching Form")(actionCall = testCall)

    testPage.shouldHaveSubmitButton(messages.tryAgain)

    testPage.shouldHaveSignOutLink()
  }

}

