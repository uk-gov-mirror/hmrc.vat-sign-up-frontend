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
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.MultipleVatCheckForm._
import uk.gov.hmrc.vatsignupfrontend.models.{No, Overseas, Yes}
import uk.gov.hmrc.vatsignupfrontend.services.VatNumberOrchestrationService
import uk.gov.hmrc.vatsignupfrontend.services.VatNumberOrchestrationService._
import uk.gov.hmrc.vatsignupfrontend.utils.EnrolmentUtils._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.ResultUtils
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.multiple_vat_check

import scala.concurrent.Future

@Singleton
class MultipleVatCheckController @Inject()(val controllerComponents: ControllerComponents,
                                           vatNumberOrchestrationService: VatNumberOrchestrationService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(Ok(multiple_vat_check(multipleVatCheckForm, routes.MultipleVatCheckController.submit())))
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.allEnrolments) { enrolments =>

      multipleVatCheckForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(multiple_vat_check(formWithErrors, routes.MultipleVatCheckController.submit()))
          ), {
          case Yes =>
            Future.successful(Redirect(routes.CaptureVatNumberController.show()))
          case No =>
            enrolments.getAnyVatNumber match {
              case None =>
                Future.successful(Redirect(routes.ResolveVatNumberController.resolve()))
              case Some(vatNumber) =>
                vatNumberOrchestrationService.orchestrate(enrolments, None, isFromBta = false) map {
                  case VatNumberStored(isOverseas, isDirectDebit, isMigrated) if isOverseas =>
                    Redirect(routes.CaptureBusinessEntityController.show())
                      .addingToSession(SessionKeys.vatNumberKey -> vatNumber)
                      .addingToSession(SessionKeys.isMigratedKey, isMigrated)
                      .addingToSession(SessionKeys.hasDirectDebitKey, isDirectDebit)
                      .addingToSession(SessionKeys.businessEntityKey -> Overseas.toString)
                  case VatNumberStored(_, isDirectDebit, isMigrated) =>
                    Redirect(routes.CaptureBusinessEntityController.show())
                      .addingToSession(SessionKeys.vatNumberKey -> vatNumber)
                      .addingToSession(SessionKeys.isMigratedKey, isMigrated)
                      .addingToSession(SessionKeys.hasDirectDebitKey, isDirectDebit)
                  case ClaimedSubscription =>
                    Redirect(routes.SignUpCompleteClientController.show())
                  case Ineligible =>
                    Redirect(routes.CannotUseServiceController.show())
                  case Inhibited(migratableDates) =>
                    Redirect(routes.MigratableDatesController.show())
                      .addingToSession(SessionKeys.migratableDatesKey, migratableDates)
                  case MigrationInProgress =>
                    Redirect(routes.MigrationInProgressErrorController.show())
                  case AlreadyEnrolledOnDifferentCredential =>
                    Redirect(bta.routes.BusinessAlreadySignedUpController.show())
                  case AlreadySubscribed =>
                    Redirect(routes.AlreadySignedUpController.show())
                  case _ =>
                    throw new InternalServerException("Unexpected response from vat number orchestration service")
                }
            }
        }
      )
    }
  }
}
