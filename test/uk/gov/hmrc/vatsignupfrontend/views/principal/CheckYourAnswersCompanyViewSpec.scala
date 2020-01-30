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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{CaptureBusinessEntity, PrincipalCheckYourAnswersCompanySummary => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.{BusinessEntity, SoleTrader}
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.helpers.CheckYourAnswersCompanyConstants._

class CheckYourAnswersCompanyViewSpec extends ViewSpec {

  val testEntity: BusinessEntity = SoleTrader

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  def page(): Html = uk.gov.hmrc.vatsignupfrontend.views.html.principal.check_your_answers_company(
    companyNumber = testCompanyNumber,
    companyUtr = testCompanyUtr,
    businessEntity = testEntity,
    postAction = testCall
  )(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  lazy val doc: Document = Jsoup.parse(page().body)

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
      page = page()
    )

    testPage.shouldHaveH2(messages.subHeading)

    testPage.shouldHaveForm("Check your answers Form")(actionCall = testCall)
  }

  def sectionTest(sectionId: String, expectedQuestion: String, expectedAnswer: String, expectedEditLink: Option[String]): Unit = {
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
    val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureCompanyUtrController.show().url

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer,
      expectedEditLink = Some(expectedEditLink)
    )
  }

  "display the correct info for BusinessEntity" in {
    val sectionId = BusinessEntityId
    val expectedQuestion = messages.businessEntity
    val expectedAnswer = CaptureBusinessEntity.radioSoleTrader
    val expectedEditLink = uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes.CaptureBusinessEntityController.show().url

    sectionTest(
      sectionId = sectionId,
      expectedQuestion = expectedQuestion,
      expectedAnswer = expectedAnswer,
      expectedEditLink = Some(expectedEditLink)
    )
  }
}
