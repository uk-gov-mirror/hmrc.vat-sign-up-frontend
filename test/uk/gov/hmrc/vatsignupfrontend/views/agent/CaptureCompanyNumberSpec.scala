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
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{CaptureCompanyNumber => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class CaptureCompanyNumberSpec extends ViewSpec {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.capture_company_number(
    companyNumberForm = companyNumberForm(isAgent = true, isPartnership = false).form,
    postAction = testCall)(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  "The Capture Company Number view" should {

    val testPage = TestView(
      name = "Capture Company Number View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHaveForm("Company Number Form")(actionCall = testCall)

    testPage.shouldHavePara(messages.line1)

    testPage.shouldHaveALink("companiesHouse", messages.link, appConfig.companiesHouse)

    testPage.shouldHaveTextField(companyNumber, messages.heading, hideLabel = false)

    testPage.shouldHaveContinueButton()
  }

}

