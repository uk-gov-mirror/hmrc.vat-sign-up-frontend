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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService._
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService
import uk.gov.hmrc.vatsignupfrontend.utils.VatNumberChecksumValidation
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.confirm_vat_number
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.ResultUtils

import scala.concurrent.Future

@Singleton
class ConfirmVatNumberController @Inject()(val controllerComponents: ControllerComponents,
                                           val storeVatNumberService: StoreVatNumberService)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
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

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      request.session.get(SessionKeys.vatNumberKey) match {
        case Some(vatNumber) if vatNumber.nonEmpty =>
          if (VatNumberChecksumValidation.isValidChecksum(vatNumber))
            storeVatNumberService.storeVatNumberDelegated(vatNumber) map {
              case Right(VatNumberStored) =>
                Redirect(routes.CaptureBusinessEntityController.show())
              case Left(NoAgentClientRelationship) =>
                Redirect(routes.NoAgentClientRelationshipController.show())
              case Left(AlreadySubscribed) =>
                Redirect(routes.AlreadySignedUpController.show())
              case Left(IneligibleVatNumber(MigratableDates(None, None))) => Redirect(routes.CannotUseServiceController.show())
              case Left(IneligibleVatNumber(migratableDates)) => Redirect(routes.MigratableDatesController.show())
                .addingToSession(SessionKeys.migratableDatesKey, migratableDates)
              case Left(VatMigrationInProgress) => Redirect(routes.MigrationInProgressErrorController.show())
              case Left(errResponse: StoreVatNumberFailureResponse) =>
                throw new InternalServerException("storeVatNumber failed: status=" + errResponse.status)
            }
          else Future.successful(Redirect(routes.CouldNotConfirmVatNumberController.show())).removeSessionKey(SessionKeys.vatNumberKey)
        case _ =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
      }
    }
  }

}
