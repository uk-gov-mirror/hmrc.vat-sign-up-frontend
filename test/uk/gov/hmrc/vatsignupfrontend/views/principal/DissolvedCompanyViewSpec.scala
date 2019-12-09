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

import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalDissolvedCompany => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class DissolvedCompanyViewSpec extends ViewSpec {

  val env: Environment = Environment.simple()
  val configuration: Configuration = Configuration.load(env)

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page: HtmlFormat.Appendable = uk.gov.hmrc.vatsignupfrontend.views.html.principal.dissolved_company(
    postAction = testCall)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Dissolved Company view" should {

    val testPage = TestView(
      name = "Confirm Company View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHavePara(messages.paragraph1)
    testPage.shouldHavePara(messages.paragraph2)

    testPage.shouldHaveForm("Dissolved Company Form")(actionCall = testCall)

    testPage.shouldHaveSubmitButton(messages.confirm)
  }
}
