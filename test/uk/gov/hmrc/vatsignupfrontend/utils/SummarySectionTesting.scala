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

package uk.gov.hmrc.vatsignupfrontend.utils

import org.scalatest.Matchers._
import org.jsoup.nodes.Element
import org.jsoup.nodes.{Document, Element}

trait SummarySectionTesting {

  implicit class SectionTestDocument(doc: Document) {
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

    def sectionTest(sectionId: String,
                    expectedQuestion: String,
                    expectedAnswer: String,
                    expectedEditLink: Option[String]
                   ): Unit = {

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
  }

}
