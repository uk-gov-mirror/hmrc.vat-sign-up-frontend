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
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, NotFoundException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.businessEntityKey
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
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
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(GeneralPartnershipJourney, LimitedPartnershipJourney)) {

  override protected def featureEnabled[T](func: => T): T =
    if (featureSwitches exists isEnabled) func
    else throw new NotFoundException(featureSwitchError)


  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optBusinessEntityType = request.session.getModel[BusinessEntity](businessEntityKey)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipType = request.session.get(SessionKeys.partnershipTypeKey).filter(_.nonEmpty)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)

      (optBusinessEntityType, optPartnershipUtr, optPartnershipPostCode) match {
        case (Some(entityType), Some(partnershipUtr), Some(partnershipPostCode)) =>
          (entityType, optPartnershipCrn, optPartnershipType) match {
            case (GeneralPartnership, _, _) | (_, Some(_), Some(_)) =>
              Future.successful(
                Ok(check_your_answers_partnerships(
                  entityType = entityType,
                  companyUtr = partnershipUtr,
                  companyNumber = optPartnershipCrn,
                  postCode = partnershipPostCode,
                  postAction = routes.CheckYourAnswersPartnershipsController.submit()))
              )
            case _ =>
              Future.successful(Redirect(routes.CapturePartnershipCompanyNumberController.show()))
          }
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
    authorised() {
      val optBusinessEntityType = request.session.getModel[BusinessEntity](businessEntityKey)
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optPartnershipType = request.session.get(SessionKeys.partnershipTypeKey).filter(_.nonEmpty)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)

      (optVatNumber, optPartnershipUtr, optBusinessEntityType, optPartnershipPostCode) match {
        case (Some(vatNumber), Some(partnershipUtr), Some(entityType), Some(partnershipPostCode)) =>
          (entityType, optPartnershipType, optPartnershipCrn) match {
            case (GeneralPartnership, _, _) =>
              storePartnershipInformation(
                vatNumber = vatNumber,
                sautr = partnershipUtr,
                companyNumber = None,
                partnershipEntity = Some(PartnershipEntityType.GeneralPartnership.toString),
                postCode = Some(partnershipPostCode)
              )
            case (LimitedPartnership, Some(partnershipType), Some(_)) =>
              storePartnershipInformation(
                vatNumber = vatNumber,
                sautr = partnershipUtr,
                companyNumber = optPartnershipCrn,
                partnershipEntity = Some(partnershipType),
                postCode = Some(partnershipPostCode)
              )
            case (LimitedPartnership, _,_) =>
              Future.successful(Redirect(routes.CapturePartnershipCompanyNumberController.show()))
          }
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
