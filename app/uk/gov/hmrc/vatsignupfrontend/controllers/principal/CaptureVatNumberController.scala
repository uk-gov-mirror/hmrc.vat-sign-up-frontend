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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.VatNumberForm.vatNumberForm
import uk.gov.hmrc.vatsignupfrontend.models.Overseas
import uk.gov.hmrc.vatsignupfrontend.services.VatNumberOrchestrationService
import uk.gov.hmrc.vatsignupfrontend.services.VatNumberOrchestrationService._
import uk.gov.hmrc.vatsignupfrontend.utils.EnrolmentUtils._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.utils.VatNumberChecksumValidation
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_vat_number
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureVatNumberController @Inject()(val controllerComponents: ControllerComponents,
                                           vatNumberOrchestrationService: VatNumberOrchestrationService
                                          )(implicit ec: ExecutionContext) extends AuthenticatedController(AdministratorRolePredicate) {

  private val validateVatNumberForm = vatNumberForm(isAgent = false)

  def show: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(Ok(capture_vat_number(validateVatNumberForm.form, routes.CaptureVatNumberController.submit())))
      }
  }

  def submit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised()(Retrievals.allEnrolments) {
        enrolments =>
          validateVatNumberForm.bindFromRequest.fold(
            formWithErrors =>
              Future.successful(
                BadRequest(capture_vat_number(formWithErrors, routes.CaptureVatNumberController.submit()))
              ),
            formVatNumber => {
              if (VatNumberChecksumValidation.isValidChecksum(formVatNumber)) {
                enrolments.getAnyVatNumber match {
                  case Some(vrn) if vrn != formVatNumber =>
                    Future.successful(Redirect(routes.IncorrectEnrolmentVatNumberController.show()))
                  case _ =>
                    vatNumberOrchestrationService.orchestrate(enrolments, Some(formVatNumber), isFromBta = false).map {
                      case Eligible(isOverseas, _) if isOverseas =>
                        Redirect(routes.CaptureVatRegistrationDateController.show())
                          .addingToSession(vatNumberKey -> formVatNumber)
                          .addingToSession(businessEntityKey -> Overseas.toString)
                      case Eligible(_, isMigrated) =>
                        Redirect(routes.CaptureVatRegistrationDateController.show())
                          .addingToSession(vatNumberKey -> formVatNumber)
                          .addingToSession(isMigratedKey, isMigrated)
                          .removingFromSession(businessEntityKey)
                      case VatNumberStored(isOverseas, isDirectDebit, _) if isOverseas =>
                        Redirect(routes.CaptureBusinessEntityController.show())
                          .addingToSession(vatNumberKey -> formVatNumber)
                          .addingToSession(hasDirectDebitKey, isDirectDebit)
                          .addingToSession(businessEntityKey -> Overseas.toString)
                      case VatNumberStored(_, isDirectDebit, isMigrated) =>
                        Redirect(routes.CaptureBusinessEntityController.show())
                          .addingToSession(vatNumberKey -> formVatNumber)
                          .addingToSession(hasDirectDebitKey, isDirectDebit)
                          .addingToSession(isMigratedKey, isMigrated)
                      case AlreadySubscribed =>
                        Redirect(routes.AlreadySignedUpController.show())
                      case ClaimedSubscription =>
                        Redirect(routes.SignUpCompleteClientController.show())
                      case Ineligible =>
                        Redirect(routes.CannotUseServiceController.show())
                          .removingFromSession(businessEntityKey)
                      case Inhibited(migratablDates) =>
                        Redirect(routes.MigratableDatesController.show())
                          .addingToSession(migratableDatesKey, migratablDates)
                          .removingFromSession(businessEntityKey)
                      case MigrationInProgress =>
                        Redirect(routes.MigrationInProgressErrorController.show())
                      case AlreadyEnrolledOnDifferentCredential =>
                        Redirect(bta.routes.BusinessAlreadySignedUpController.show())
                      case InvalidVatNumber =>
                        Redirect(routes.InvalidVatNumberController.show())
                          .removingFromSession(businessEntityKey)
                    }.map {
                      _.removingFromSession(
                        vatRegistrationDateKey,
                        businessPostCodeKey,
                        previousVatReturnKey,
                        lastReturnMonthPeriodKey,
                        box5FigureKey
                      )
                    }
                }
              } else {
                Future.successful(Redirect(routes.InvalidVatNumberController.show()))
              }
            }

          )
      }
  }
}
