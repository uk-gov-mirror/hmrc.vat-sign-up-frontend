/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{VerifyAgentEmail => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.routes
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class VerifyAgentEmailSpec extends ViewSpec {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val page: HtmlFormat.Appendable = uk.gov.hmrc.vatsignupfrontend.views.html.agent.verify_agent_email(
    email = testEmail)(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  "The Verify Agent email view" should {

    val testPage = TestView(
      name = "Verify Agent Email View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHaveParaSeq(
      messages.line1(testEmail),
      messages.line2,
      messages.line3
    )

    testPage.shouldHaveAccordion(
      heading = messages.accordionHeading,
      text = messages.accordionText
    )

    testPage.shouldHaveALink("change", messages.linkText1, routes.CaptureAgentEmailController.show().url)

    testPage.shouldHaveALink("resend", messages.linkText2, routes.ConfirmAgentEmailController.show().url)

  }

}

