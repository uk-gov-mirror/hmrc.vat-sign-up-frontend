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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalCaptureBusinessEntity, PrincipalNoCtEnrolmentSummary => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.SoleTrader
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.NoCtEnrolmentSummaryIdConstants._

class NoCtEnrolmentSummarySpec extends ViewSpec {

  val testEntity = SoleTrader
  val env = Environment.simple()
  val configuration = Configuration.load(env)

  def page(): Html = uk.gov.hmrc.vatsignupfrontend.views.html.principal.no_ct_enrolment_summary(
    company_number = testCompanyNumber,
    company_utr = testCompanyUtr,
    businessEntity = testEntity,
    postAction = testCall
  )(FakeRequest(), applicationMessages, new AppConfig(configuration, env))

  lazy val doc = Jsoup.parse(page.body)

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

  "Check your answers view" should {

    val testPage = TestView(
      name = "Check your answers View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )

    testPage.shouldHaveH2(messages.subHeading)

    testPage.shouldHaveForm("Check your answers Form")(actionCall = testCall)
  }

  def sectionTest(sectionId: String, expectedQuestion: String, expectedAnswer: String, expectedEditLink: Option[String]) = {
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

  "display the correct info for CompanyNumber" in {
    val sectionId = CompanyNumberId
    val expectedQuestion = messages.companyNumber
    val expectedAnswer = testCompanyNumber
    val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureCompanyNumberController.show().url

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer,
      expectedEditLink = Some(expectedEditLink)
    )
  }

  "display the correct info for CompanyUtr" in {
    val sectionId = CompanyUtrId
    val expectedQuestion = messages.companyUtr
    val expectedAnswer = testCompanyUtr
    //TODO: link to capture company utr controller
    //val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureCompanyUtrController.show().url

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer,
      expectedEditLink = None
    )
  }

  "display the correct info for BusinessEntity" in {
    val sectionId = BusinessEntityId
    val expectedQuestion = messages.businessEntity
    val expectedAnswer = PrincipalCaptureBusinessEntity.radioSoleTrader
    val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureBusinessEntityController.show().url

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer,
      expectedEditLink = Some(expectedEditLink)
    )
  }
}
