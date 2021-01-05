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

package uk.gov.hmrc.vatsignupfrontend.testonly.forms

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import play.api.data.{FieldMapping, Form, FormError, Mapping}
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.DateMapping._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.ConstraintUtil.constraint
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns._
import uk.gov.hmrc.vatsignupfrontend.models.DateModel
import uk.gov.hmrc.vatsignupfrontend.testonly.models.StubIssuerRequest

import scala.util.Try

object StubIssuerRequestForm {

  val isSuccessful = "isSuccessful"
  val vatNumber = "vatNumber"
  val errorMessage = "errorMessage"

  val vatRegistrationDateInvalid: Constraint[DateModel] = constraint[DateModel](
    date => {
      Try[ValidationResult] {
        date.toLocalDate
        Valid
      }.getOrElse(Invalid("error.invalid_vat_registration_date"))
    }
  )

  private def dependsOn[T](isSuccessful: Boolean)(mapping: Mapping[T]): FieldMapping[Option[T]] = of(new Formatter[Option[T]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[T]] = {
      if (data.exists(_ == (StubIssuerRequestForm.isSuccessful -> isSuccessful.toString)))
        mapping.withPrefix(key).bind(data) match {
          case Right(res) => Right(Some(res))
          case Left(err) => Left(err)
        }
      else
        Right(None)
    }

    override def unbind(key: String, value: Option[T]): Map[String, String] = {
      value match {
        case Some(v) => mapping.unbind(v)
        case None => Map(key -> "")
      }
    }
  })

  val stubIssuerForm: Form[StubIssuerRequest] = Form(
    mapping(
      vatNumber -> text.verifying("error.invalid_vat_number", _ matches vatNumberRegex),
      isSuccessful -> boolean,
      errorMessage -> dependsOn(isSuccessful = false)(text.verifying("Please enter a valid error message", x=> x.nonEmpty && (x matches iso8859_1Regex)))
    )(StubIssuerRequest.apply)(StubIssuerRequest.unapply)
  )

}
