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

package uk.gov.hmrc.vatsignupfrontend.views.agent.partnerships

import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentCapturePartnershipCompanyNumber => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class CapturePartnershipCompanyNumberSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]
  lazy val appConfig = app.injector.instanceOf[AppConfig]

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.capture_partnership_company_number(
    partnershipCompanyNumberForm = companyNumberForm(isAgent = true, isPartnership = true).form,
    postAction = testCall)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Capture Partnership company number view" should {

    val testPage = TestView(
      name = "Capture Partnership company number View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHaveForm("Partnership company number Form")(actionCall = testCall)

    testPage.shouldHaveTextField(companyNumber, messages.heading, hideLabel = false)

    testPage.shouldHavePara(messages.line1)

    testPage.shouldHaveALink("companiesHouse", messages.link, appConfig.companiesHouse)

    testPage.shouldHaveContinueButton()
  }

}

