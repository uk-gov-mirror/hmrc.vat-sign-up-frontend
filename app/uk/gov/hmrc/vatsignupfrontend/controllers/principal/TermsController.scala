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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.SubmissionHttpParser.SubmissionFailureResponse
import uk.gov.hmrc.vatsignupfrontend.services.SubmissionService
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.terms

import scala.concurrent.Future

@Singleton
class TermsController @Inject()(val controllerComponents: ControllerComponents,
                                val submissionService: SubmissionService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(terms(routes.TermsController.submit()))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val acceptedDirectDebitTerms = request.session.get(SessionKeys.acceptedDirectDebitTermsKey).getOrElse("false").toBoolean

      optVatNumber match {
        case Some(vatNumber) if acceptedDirectDebitTerms =>
          submissionService.submit(vatNumber).map {
            case Right(_) => {
              Redirect(routes.InformationReceivedController.show())
            }
            case Left(SubmissionFailureResponse(status)) =>
              throw new InternalServerException(s"Submission failed, backend returned: $status")
          }
        case Some(_) => Future.successful(Redirect(routes.DirectDebitTermsAndConditionsController.show()))
        case _ => Future.successful(Redirect(routes.ResolveVatNumberController.resolve()))
      }
    }
  }
}
