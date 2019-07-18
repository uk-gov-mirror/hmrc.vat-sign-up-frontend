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

package uk.gov.hmrc.vatsignupfrontend.views.agent.partnerships

import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentCapturePartnershipUtr => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships.routes
import uk.gov.hmrc.vatsignupfrontend.forms.PartnershipUtrForm._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class CapturePartnershipUtrSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)
  lazy val appConfig = new AppConfig(configuration, env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  def page(displayNoSautrLink: Boolean) = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.capture_partnership_utr(
    partnershipUtrForm = partnershipUtrForm.form,
    postAction = testCall,
    displayNoSautrLink = displayNoSautrLink)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Capture Partnership Utr view" when {
    "displayNoSautrLink is false" should {

      lazy val testPage = TestView(
        name = "Capture Partnership Utr View",
        title = messages.title,
        heading = messages.heading,
        page = page(false)
      )

      testPage.shouldHaveForm("Partnership Utr Form")(actionCall = testCall)

      testPage.shouldHaveTextField(partnershipUtr, messages.heading)

      testPage.shouldHavePara(messages.line)

      testPage.shouldHaveContinueButton()

      testPage.shouldHaveHint(messages.hint)

      "not have the No SAUTR accordion links" in {
        testPage.document.getElementById("partnershipUtr-accordion-link1") shouldBe null

        testPage.document.getElementById("partnershipUtr-accordion-link2") shouldBe null

        testPage.document.getElementsByTag("details").size shouldBe 0
      }
    }
  }

  "The Capture Partnership Utr view" when {
    "displayNoSautrLink is true" should {

      val testPage = TestView(
        name = "Capture Partnership Utr View",
        title = messages.title,
        heading = messages.heading,
        page = page(true)
      )

      testPage.shouldHaveForm("Partnership Utr Form")(actionCall = testCall)

      testPage.shouldHaveTextField(partnershipUtr, messages.heading)

      testPage.shouldHavePara(messages.line)

      testPage.shouldHaveContinueButton()

      testPage.shouldHaveHint(messages.hint)

      "have the No SAUTR accordion links" in {
        val accordion = testPage.document.getElementsByTag("details").get(0)

        accordion.getElementsByTag("p").text() should include(messages.accordionText)

        accordion.getElementById("partnershipUtr-accordion-link1").text shouldBe messages.accordionLink1

        accordion.getElementById("partnershipUtr-accordion-link1").attr("href") shouldBe appConfig.findLostUtrNumberUrl

        accordion.getElementById("partnershipUtr-accordion-link2").text shouldBe messages.accordionLink2

        accordion.getElementById("partnershipUtr-accordion-link2").attr("href") shouldBe routes.CapturePartnershipUtrController.noUtrSelected().url
      }
    }
  }

}

