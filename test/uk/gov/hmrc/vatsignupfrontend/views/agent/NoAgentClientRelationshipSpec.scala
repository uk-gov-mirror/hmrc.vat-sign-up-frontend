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

package uk.gov.hmrc.vatsignupfrontend.views.agent

import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{NoAgentClientRelationship => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class NoAgentClientRelationshipSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.no_agent_client_relationship(
    postAction = testCall)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The No Agent Client Relationship view" should {

    val testPage = TestView(
      name = "No Agent Client Relationship View",
      title = messages.title,
      heading = messages.heading,
      page = page,
      haveSignOutInBanner = false
    )

    testPage.shouldHavePara(messages.line1)

    testPage.shouldHaveForm("No Agent Client Relationship Form")(actionCall = testCall)

    testPage.shouldHaveSignUpAnotherClientButton()

    testPage.shouldHaveSignOutLink(isAgent = true)

  }

}

