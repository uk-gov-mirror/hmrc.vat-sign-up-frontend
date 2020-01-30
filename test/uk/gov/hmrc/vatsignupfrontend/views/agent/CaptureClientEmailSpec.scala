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

package uk.gov.hmrc.vatsignupfrontend.views.agent

import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{CaptureClientEmail => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.EmailForm._
import uk.gov.hmrc.vatsignupfrontend.models.Digital
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class CaptureClientEmailSpec extends ViewSpec {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val page: HtmlFormat.Appendable = uk.gov.hmrc.vatsignupfrontend.views.html.agent.capture_client_email(
    hasDirectDebit = false,
    contactPreference = Some(Digital.toString),
    emailForm = emailForm(isAgent = true).form,
    postAction = testCall)(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  "The Capture Client Email view without direct debit and Digital contact preference" should {

    val testPage = TestView(
      name = "Capture Client Email View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHavePara(
      messages.line1
    )

    testPage.shouldHaveBulletSeq(
      messages.bullet2
    )

    testPage.shouldHaveHint(
      messages.hint
    )

    testPage.shouldHaveForm("Email Form")(actionCall = testCall)

    testPage.shouldHaveTextField(email, messages.heading, hideLabel = false)

    testPage.shouldHaveContinueButton()
  }

  lazy val ddPageAndDigital = uk.gov.hmrc.vatsignupfrontend.views.html.agent.capture_client_email(
    hasDirectDebit = true,
    contactPreference = Some(Digital.toString),
    emailForm = emailForm(isAgent = true).form,
    postAction = testCall)(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  "The Capture Client Email view with Direct Debit and has Digital Contact Preference" should {

    val testPage = TestView(
      name = "Capture Client Email View",
      title = messages.title,
      heading = messages.heading,
      page = ddPageAndDigital
    )

    testPage.shouldHaveParaSeq(
      messages.line1,
      messages.line2
    )

    testPage.shouldHaveBulletSeq(
      messages.bullet1,
      messages.bullet2
    )

    testPage.shouldHaveHint(
      messages.hint
    )

    testPage.shouldHaveForm("Email Form")(actionCall = testCall)

    testPage.shouldHaveTextField(email, messages.heading, hideLabel = false)

    testPage.shouldHaveContinueButton()
  }

  lazy val ddPage = uk.gov.hmrc.vatsignupfrontend.views.html.agent.capture_client_email(
    hasDirectDebit = true,
    contactPreference = Some(Digital.toString),
    emailForm = emailForm(isAgent = true).form,
    postAction = testCall)(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  "The Capture Client Email view with Direct Debit" should {

    val testPage = TestView(
      name = "Capture Client Email View",
      title = messages.title,
      heading = messages.heading,
      page = ddPage
    )

    testPage.shouldHaveParaSeq(
      messages.line1,
      messages.line2
    )

    testPage.shouldHaveBulletSeq(
      messages.bullet1
    )

    testPage.shouldHaveHint(
      messages.hint
    )

    testPage.shouldHaveForm("Email Form")(actionCall = testCall)

    testPage.shouldHaveTextField(email, messages.heading, hideLabel = false)

    testPage.shouldHaveContinueButton()
  }

}
