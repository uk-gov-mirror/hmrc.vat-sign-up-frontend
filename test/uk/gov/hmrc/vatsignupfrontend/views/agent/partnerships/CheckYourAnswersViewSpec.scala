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

import _root_.uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersPartnershipsIdConstants.CompanyNumberId
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentCheckYourAnswers => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships.{routes => partnershipRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedPartnership}
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersIdConstants._


class CheckYourAnswersViewSpec extends ViewSpec {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val questionId: String => String = (sectionId: String) => s"$sectionId-question"
  val answerId: String => String = (sectionId: String) => s"$sectionId-answer"
  val editLinkId: String => String = (sectionId: String) => s"$sectionId-edit"

  def questionStyleCorrectness(section: Element): Unit = {
    section.attr("class") shouldBe "tabular-data__heading tabular-data__heading--label"
  }

  def answerStyleCorrectness(section: Element): Unit = {
    section.attr("class") shouldBe "tabular-data__data-1"
  }

  def editLinkStyleCorrectness(section: Element): Unit = {
    section.attr("class") shouldBe "tabular-data__data-2"
  }

  def sectionTest(sectionId: String, expectedQuestion: String, expectedAnswer: String, expectedEditLink: Option[String], doc: Document): Unit = {
    val accountingPeriod = doc.getElementById(sectionId)
    val question = doc.getElementById(questionId(sectionId))
    val answer = doc.getElementById(answerId(sectionId))
    val editLink = doc.getElementById(editLinkId(sectionId))

    questionStyleCorrectness(question)
    answerStyleCorrectness(answer)
    if (expectedEditLink.nonEmpty) editLinkStyleCorrectness(editLink)
    question.text() shouldBe expectedQuestion
    answer.text() shouldBe expectedAnswer
    if (expectedEditLink.nonEmpty) {
      editLink.attr("href") shouldBe expectedEditLink.get
      editLink.select("span").text() shouldBe expectedQuestion
      editLink.select("span").hasClass("visuallyhidden") shouldBe true
    }
  }

  "the Check Your Answers View" when {
    val utrAnswer = "utr-answer"
    val businessEntityAnswer = "business-entity-answer"
    val crnAnswer = "company-number-answer"
    val pobAnswer = "business-post-code-answer"
    lazy val expectedUrlUtr = partnershipRoutes.CapturePartnershipUtrController.show().url
    lazy val expectedUrlPostCode = partnershipRoutes.PartnershipPostCodeController.show().url
    lazy val expectedUrlCompanyNumber = partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show().url

    "the saUtr and the post code are given for a general partnership" should {
      "render the page correctly" when {
        val testPageGeneralPartnershipFSEnabled = TestView(
          name = "Check your answers View - General parnership fs enabled",
          title = messages.title,
          heading = messages.heading,
          page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = GeneralPartnership,
            Some(testBusinessPostcode),
            companyNumber = None,
            generalPartnershipNoSAUTR = true,
            postAction = testCall
          )(
            request,
            messagesApi.preferred(request),
            appConfig
          )
        )

        val testPageGeneralPartnershipFSDisabled = TestView(
          name = "Check your answers View - General parnership fs disabled",
          title = messages.title,
          heading = messages.heading,
          page = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = GeneralPartnership,
            Some(testBusinessPostcode),
            companyNumber = None,
            generalPartnershipNoSAUTR = true,
            postAction = testCall
          )(
            request,
            messagesApi.preferred(request),
            appConfig
          )
        )

        testPageGeneralPartnershipFSEnabled.shouldHaveForm("Check your answers Form")(actionCall = testCall)

        testPageGeneralPartnershipFSDisabled.shouldHaveForm("Check your answers Form")(actionCall = testCall)

        "the GeneralPartnershipNoSAUTR feature switch is enabled" in {
          sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), testPageGeneralPartnershipFSEnabled.document)
          sectionTest(
            BusinessPostCodeId,
            messages.yourBusinessPostCode,
            testBusinessPostcode.checkYourAnswersFormat,
            Some(expectedUrlPostCode),
            testPageGeneralPartnershipFSEnabled.document
          )

          testPageGeneralPartnershipFSEnabled.document.getElementById(utrAnswer).text shouldBe testSaUtr
          testPageGeneralPartnershipFSEnabled.document.getElementById(businessEntityAnswer).text shouldBe messages.generalPartnership
          testPageGeneralPartnershipFSEnabled.document.getElementById(pobAnswer).text shouldBe testBusinessPostcode.checkYourAnswersFormat
        }

        "the GeneralPartnershipNoSAUTR feature switch is disabled" in {
          sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), testPageGeneralPartnershipFSDisabled.document)
          sectionTest(
            BusinessPostCodeId,
            messages.yourBusinessPostCode,
            testBusinessPostcode.checkYourAnswersFormat,
            Some(expectedUrlPostCode),
            testPageGeneralPartnershipFSDisabled.document
          )

          testPageGeneralPartnershipFSDisabled.document.getElementById(utrAnswer).text shouldBe testSaUtr
          testPageGeneralPartnershipFSDisabled.document.getElementById(businessEntityAnswer).text shouldBe messages.generalPartnership
          testPageGeneralPartnershipFSDisabled.document.getElementById(pobAnswer).text shouldBe testBusinessPostcode.checkYourAnswersFormat
        }
      }
    }

    "the General Partnership has an optional SA UTR" should {
      "render the page correctly" when {
        "the GeneralPartnershipNoSAUTR feature switch is disabled" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = GeneralPartnership,
            postCode = None,
            companyNumber = None,
            generalPartnershipNoSAUTR = false,
            postAction = testCall
          )(
            request,
            messagesApi.preferred(request),
            appConfig
          )

          lazy val doc = Jsoup.parse(page.body)

          Option(doc.getElementById(utrAnswer)).map(_.text) shouldBe Some(testSaUtr)
          sectionTest(
            sectionId = UtrId,
            expectedQuestion = messages.yourUtr,
            expectedAnswer = testSaUtr,
            expectedEditLink = Some(expectedUrlUtr),
            doc = doc
          )
        }

        "the GeneralPartnershipNoSAUTR feature switch is enabled" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = None,
            entityType = GeneralPartnership,
            postCode = None,
            companyNumber = None,
            generalPartnershipNoSAUTR = true,
            postAction = testCall
          )(
            request,
            messagesApi.preferred(request),
            appConfig
          )

          lazy val doc = Jsoup.parse(page.body)

          sectionTest(
            sectionId = UtrId,
            expectedQuestion = messages.yourUtr,
            expectedAnswer = messages.noSAUTR,
            expectedEditLink = Some(expectedUrlUtr),
            doc = doc
          )
        }
      }
    }

    "the saUtr, company number and the post code are given for a limited partnership" should {
      "render the page correctly" when {
        "the GeneralPartnershipNoSAUTR feature switch is enabled" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = LimitedPartnership,
            Some(testBusinessPostcode),
            companyNumber = Some(testCompanyNumber),
            generalPartnershipNoSAUTR = true,
            postAction = testCall
          )(
            request,
            messagesApi.preferred(request),
            appConfig
          )

          lazy val doc = Jsoup.parse(page.body)

          sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
          sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.checkYourAnswersFormat, Some(expectedUrlPostCode), doc)
          sectionTest(CompanyNumberId, messages.yourCompanyNumber, testCompanyNumber, Some(expectedUrlCompanyNumber), doc)

          doc.getElementById(utrAnswer).text shouldBe testSaUtr
          doc.getElementById(businessEntityAnswer).text shouldBe messages.limitedPartnership
          doc.getElementById(pobAnswer).text shouldBe testBusinessPostcode.checkYourAnswersFormat
        }
        "the GeneralPartnershipNoSAUTR feature switch is disabled" in {
          lazy val page: Html = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.check_your_answers(
            utr = Some(testSaUtr),
            entityType = LimitedPartnership,
            Some(testBusinessPostcode),
            companyNumber = Some(testCompanyNumber),
            generalPartnershipNoSAUTR = false,
            postAction = testCall
          )(
            request,
            messagesApi.preferred(request),
            appConfig
          )

          lazy val doc = Jsoup.parse(page.body)

          sectionTest(UtrId, messages.yourUtr, testSaUtr, Some(expectedUrlUtr), doc)
          sectionTest(BusinessPostCodeId, messages.yourBusinessPostCode, testBusinessPostcode.checkYourAnswersFormat, Some(expectedUrlPostCode), doc)
          sectionTest(CompanyNumberId, messages.yourCompanyNumber, testCompanyNumber, Some(expectedUrlCompanyNumber), doc)

          doc.getElementById(utrAnswer).text shouldBe testSaUtr
          doc.getElementById(businessEntityAnswer).text shouldBe messages.limitedPartnership
          doc.getElementById(pobAnswer).text shouldBe testBusinessPostcode.checkYourAnswersFormat
        }
      }
    }
  }

}
