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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.soletrader

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, RequestHeader, Result}
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.SkipCidCheck
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.CitizenDetailsHttpParser.CitizenDetailsRetrievalSuccess
import uk.gov.hmrc.vatsignupfrontend.models.{AuthProfile, IRSA, NinoSource}
import uk.gov.hmrc.vatsignupfrontend.services.{CitizenDetailsService, StoreNinoService}
import uk.gov.hmrc.vatsignupfrontend.utils.EnrolmentUtils._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._

import scala.concurrent.Future

@Singleton
class SoleTraderResolverController @Inject()(val controllerComponents: ControllerComponents,
                                             citizenDetailsService: CitizenDetailsService,
                                             storeNinoService: StoreNinoService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val resolve: Action[AnyContent] = Action.async {
    implicit request =>
      authorised()(Retrievals.allEnrolments and Retrievals.nino) {
        case enrolments ~ optNino => {
          val optUtr = enrolments.selfAssessmentUniqueTaxReferenceNumber
          val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)

          if (isEnabled(SkipCidCheck)) resolveWithoutCidCheck(optVatNumber, optNino)
          else resolveWithCidCheck(optUtr, optNino)
        }
      }
  }

  private def resolveWithCidCheck(utr: Option[String], nino: Option[String])(implicit rh: RequestHeader) : Future[Result] = {
    (utr, nino) match {
      case (None, None) =>
        Future.successful(Redirect(principalRoutes.CaptureYourDetailsController.show()))
      case (Some(utr), _) =>
        citizenDetailsService.getCitizenDetailsBySautr(utr) map {
          case Right(CitizenDetailsRetrievalSuccess(detailsModel)) =>
            Redirect(principalRoutes.ConfirmYourRetrievedUserDetailsController.show())
              .addingToSession(SessionKeys.userDetailsKey, detailsModel)
              .addingToSession(SessionKeys.ninoSourceKey, IRSA: NinoSource)
          case Left(reason) =>
            throw new InternalServerException(s"calls to CID received unexpected failure $reason")
        }
      case (None, Some(nino)) =>
        citizenDetailsService.getCitizenDetailsByNino(nino) map {
          case Right(CitizenDetailsRetrievalSuccess(detailsModel)) =>
            Redirect(principalRoutes.ConfirmYourRetrievedUserDetailsController.show())
              .addingToSession(SessionKeys.userDetailsKey, detailsModel)
              .addingToSession(SessionKeys.ninoSourceKey, AuthProfile: NinoSource)
          case Left(reason) =>
            throw new InternalServerException(s"calls to CID received unexpected failure $reason")
        }
    }
  }

  private def resolveWithoutCidCheck(vatNumber: Option[String], nino: Option[String])(implicit rh: RequestHeader): Future[Result] = {
    (vatNumber, nino) match {
      case (None, _) =>
        Future.successful(Redirect(principalRoutes.CaptureVatNumberController.show()))
      case (_, None) =>
        Future.successful(Redirect(routes.CaptureNinoController.show()))
      case (Some(vatNumber), Some(nino)) =>
        storeNinoService.storeNino(vatNumber, nino, AuthProfile) map {
          case Right(_) =>
            Redirect(principalRoutes.DirectDebitResolverController.show())
          case Left(reason) =>
            throw new InternalServerException(s"Failed to store NINO with reason $reason")
        }
    }
  }
}
