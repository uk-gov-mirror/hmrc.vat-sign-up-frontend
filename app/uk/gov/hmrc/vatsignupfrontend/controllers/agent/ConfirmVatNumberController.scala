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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.error.{routes => errorRoutes}
import uk.gov.hmrc.vatsignupfrontend.models.{BusinessEntity, Overseas}
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberOrchestrationService
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberOrchestrationService._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.ResultUtils
import uk.gov.hmrc.vatsignupfrontend.utils.VatNumberChecksumValidation
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.confirm_vat_number

import scala.concurrent.Future

@Singleton
class ConfirmVatNumberController @Inject()(val controllerComponents: ControllerComponents,
                                           storeVatNumberOrchestrationService: StoreVatNumberOrchestrationService)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val show: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        request.session.get(SessionKeys.vatNumberKey) match {
          case Some(vatNumber) if vatNumber.nonEmpty =>
            Future.successful(
              Ok(confirm_vat_number(vatNumber, routes.ConfirmVatNumberController.submit()))
            )
          case _ =>
            Future.successful(
              Redirect(routes.CaptureVatNumberController.show())
            )
        }
      }
  }

  val submit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised()(Retrievals.allEnrolments) {
        enrolments =>
          request.session.get(SessionKeys.vatNumberKey) match {
            case Some(vatNumber) if vatNumber.nonEmpty =>
              if (VatNumberChecksumValidation.isValidChecksum(vatNumber))
                storeVatNumberOrchestrationService.orchestrate(enrolments, vatNumber).map {
                  case VatNumberStored(isOverseas, isDirectDebit, isMigrated) if isOverseas =>
                    Redirect(routes.CaptureBusinessEntityController.show())
                      .addingToSession(SessionKeys.hasDirectDebitKey, isDirectDebit)
                      .addingToSession(SessionKeys.isMigratedKey, isMigrated)
                      .addingToSession(SessionKeys.businessEntityKey, Overseas.asInstanceOf[BusinessEntity])
                  case VatNumberStored(_, isDirectDebit, isMigrated) =>
                    Redirect(routes.CaptureBusinessEntityController.show())
                      .addingToSession(SessionKeys.hasDirectDebitKey, isDirectDebit)
                      .addingToSession(SessionKeys.isMigratedKey, isMigrated)
                  case NoAgentClientRelationship =>
                    Redirect(routes.NoAgentClientRelationshipController.show())
                  case AlreadySubscribed =>
                    Redirect(errorRoutes.AlreadySignedUpController.show())
                  case Ineligible =>
                    Redirect(routes.CannotUseServiceController.show())
                  case Deregistered =>
                    Redirect(errorRoutes.DeregisteredVatNumberController.show())
                  case Inhibited(migratableDates) =>
                    Redirect(errorRoutes.MigratableDatesController.show())
                      .addingToSession(SessionKeys.migratableDatesKey, migratableDates)
                  case MigrationInProgress =>
                    Redirect(errorRoutes.MigrationInProgressErrorController.show())
                  case InvalidVatNumber =>
                    Redirect(routes.CouldNotConfirmVatNumberController.show())
                  case errorResponse =>
                    throw new InternalServerException(s"storeVatNumberOrchestration failed due to $errorResponse")
                }
              else Future.successful(
                Redirect(routes.CouldNotConfirmVatNumberController.show())
              ).removeSessionKey(SessionKeys.vatNumberKey)
            case _ =>
              Future.successful(
                Redirect(routes.CaptureVatNumberController.show())
              )
          }
      }
  }

}
