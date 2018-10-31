/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.GeneralPartnershipJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser.{StorePartnershipInformationFailureResponse, StorePartnershipInformationSuccess}
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.StorePartnershipInformationService
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.check_your_answers_partnerships

import scala.concurrent.Future

@Singleton
class CheckYourAnswersPartnershipsController @Inject()(val controllerComponents: ControllerComponents,
                                                       val storePartnershipInformationService: StorePartnershipInformationService)
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(GeneralPartnershipJourney)) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optPartnershipType = request.session.get(SessionKeys.partnershipTypeKey).filter(_.nonEmpty)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)

      (optPartnershipType, optPartnershipUtr, optPartnershipPostCode) match {
        case (Some(partnershipType), Some(partnershipUtr), Some(partnershipPostCode)) =>
          Future.successful(
            Ok(check_your_answers_partnerships(
              entityType = partnershipType,
              companyUtr = partnershipUtr,
              companyNumber = None,
              postCode = partnershipPostCode,
              postAction = routes.CheckYourAnswersPartnershipsController.submit()))
          ) //TODO add crn for limited partnership flow
        case (None, _, _) =>
          Future.successful(
            Redirect(principalRoutes.CaptureBusinessEntityController.show())
          )
        case (_, None, _) =>
          Future.successful(
            Redirect(routes.CapturePartnershipUtrController.show())
          )
        case (_, _, None) =>
          Future.successful(
            Redirect(routes.PrincipalPlacePostCodeController.show())
          )
      }
    }
  }

  private def storePartnershipInformation(vatNumber: String,
                                          sautr: String,
                                          companyNumber: Option[String],
                                          partnershipEntity: Option[String],
                                          postCode: Option[PostCode]
                                         )(implicit hc: HeaderCarrier): Future[Result] = {
    storePartnershipInformationService.storePartnershipInformation(
      vatNumber = vatNumber,
      sautr = sautr,
      companyNumber = companyNumber,
      partnershipEntity = partnershipEntity,
      postCode = postCode
    ) flatMap {
      case Right(StorePartnershipInformationSuccess) =>
        Future.successful(Redirect(principalRoutes.AgreeCaptureEmailController.show()))
      case Left(StorePartnershipInformationFailureResponse(status)) =>
        Future.failed(new InternalServerException("Store Partnership failed with status code: " + status))
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.allEnrolments) { enrolments =>
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optPartnershipType = request.session.get(SessionKeys.partnershipTypeKey).filter(_.nonEmpty)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)

      (optVatNumber, optPartnershipUtr, optPartnershipType, optPartnershipPostCode) match {
        case (Some(vatNumber), Some(partnershipUtr), Some(partnershipType), Some(partnershipPostCode)) =>
         storePartnershipInformation(
            vatNumber = vatNumber,
            sautr = partnershipUtr,
            companyNumber = None,
            partnershipEntity = Some(partnershipType),
            postCode = Some(partnershipPostCode)
          )
        //TODO add crn for limited partnership flow
        case (None, _, _, _) =>
          Future.successful(
            Redirect(principalRoutes.ResolveVatNumberController.resolve())
          )
        case (_, None, _, _) =>
          Future.successful(
            Redirect(routes.CapturePartnershipUtrController.show())
          )
        case (_, _, None, _) =>
          Future.successful(
            Redirect(principalRoutes.CaptureBusinessEntityController.show())
          )
        case (_, _, _, None) =>
          Future.successful(
            Redirect(routes.PrincipalPlacePostCodeController.show())
          )
      }
    }
  }

}
