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

case class PadConfig(length: Int, character: String)

trait StringPaddingUtil {

  def leftPad(string: String)(implicit config: PadConfig): String =
    leftPad(string, config.length, padCharacter = config.character)

  def leftPad(string: String, maxLength: Int, padCharacter: String): String =
    repeat(maxLength - string.length, padCharacter) + string

  def rightPad(string: String)(implicit config: PadConfig): String =
    rightPad(string, config.length, config.character)

  def rightPad(string: String, maxLength: Int, padCharacter: String): String =
    string + repeat(maxLength - string.length, padCharacter)

  def repeat(len: Int, repeatCharacter: String): String =
    (1 to len) map (_ => repeatCharacter) mkString

}
