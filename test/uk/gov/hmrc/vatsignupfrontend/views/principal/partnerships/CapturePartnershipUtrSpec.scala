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

package uk.gov.hmrc.vatsignupfrontend.views.principal.partnerships

import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships.routes
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{CapturePartnershipUtr => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.GeneralPartnershipNoSAUTR
import uk.gov.hmrc.vatsignupfrontend.forms.PartnershipUtrForm._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class CapturePartnershipUtrSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)
  val appConfig = new AppConfig(configuration, env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = (displayGeneralPartnershipAccordion: Boolean) => {
    uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.capture_partnership_utr(
      partnershipUtrForm = partnershipUtrForm.form,
      postAction = testCall,
      displayGeneralPartnershipAccordion = displayGeneralPartnershipAccordion
    )(
      FakeRequest(),
      applicationMessages,
      appConfig
    )
  }

  s"The Capture Partnership Utr view with showGeneralPartnershipSpecific true" should {
    val testPage = TestView(
      name = "Capture Partnership Utr View",
      title = messages.title,
      heading = messages.heading,
      page = page(true)
    )

    testPage.shouldHaveForm("Partnership Utr Form")(actionCall = testCall)

    testPage.shouldHaveAccordion(messages.accordionHeading, messages.accordionText)

    specificGeneralPartnershipNoSAUTRDrivenElementsShouldChangeForTrue(testPage)

    testPage.shouldHaveTextField(partnershipUtr, messages.heading, hideLabel = false)

    testPage.shouldHavePara(messages.line1)

    testPage.shouldHaveContinueButton()
  }

  s"The Capture Partnership Utr view with showGeneralPartnershipSpecific false" should {
    val testPage = TestView(
      name = "Capture Partnership Utr View",
      title = messages.title,
      heading = messages.heading,
      page = page(false)
    )

    testPage.shouldHaveForm("Partnership Utr Form")(actionCall = testCall)

    specificGeneralPartnershipNoSAUTRDrivenElementsShouldChangeForFalse(testPage)

    testPage.shouldHaveTextField(partnershipUtr, messages.heading, hideLabel = false)

    testPage.shouldHavePara(messages.line1)

    testPage.shouldHaveALink("partnershipUtr-limited-link", messages.cannotFind, appConfig.findLostUtrNumberUrl)

    testPage.shouldHaveContinueButton()

  }

  private def specificGeneralPartnershipNoSAUTRDrivenElementsShouldChangeForFalse(testPage: TestView): Unit = {
    s"certain elements should not exist as $GeneralPartnershipNoSAUTR is off" in {
      testPage.document.getElementById("partnershipUtr-accordion-link1") shouldBe null
      testPage.document.getElementById("partnershipUtr-accordion-link2") shouldBe null
      testPage.document.getElementsByTag("details").size shouldBe 0
    }
  }
  private def specificGeneralPartnershipNoSAUTRDrivenElementsShouldChangeForTrue(testPage: TestView): Unit = {
    s"hint exists, hrefs for links in summary are correct, line 1 no longer exists in view as $GeneralPartnershipNoSAUTR is on" in {
      testPage.document.getElementById("partnershipUtr-accordion-link1").attr("href") shouldBe appConfig.findLostUtrNumberUrl
      testPage.document.getElementById("partnershipUtr-accordion-link2").attr("href") shouldBe routes.CapturePartnershipUtrController.noUtrSelected().url
    }
  }
}
