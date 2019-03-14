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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, JointVenturePropertyJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
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

      val optBusinessEntityType = request.session.getModel[BusinessEntity](SessionKeys.businessEntityKey)

      Future.successful(optBusinessEntityType match {
        case Some(GeneralPartnership) => showGeneralPartnershipAnswers()
        case Some(entity: LimitedPartnershipBase) => showLimitedPartnershipAnswers(entity)
        case None => Redirect(principalRoutes.CaptureBusinessEntityController.show())
      })
    }
  }

  private def showGeneralPartnershipAnswers()(implicit request: Request[_]): Result = {

    val optJointVentureProperty = request.session.getModel[YesNo](SessionKeys.jointVentureOrPropertyKey)

    (isEnabled(JointVenturePropertyJourney), optJointVentureProperty) match {
      case (true, None) => Redirect(routes.JointVentureOrPropertyController.show())
      case (true, Some(Yes)) =>
        Ok(check_your_answers_partnerships(
          entityType = GeneralPartnership,
          companyUtr = None,
          companyNumber = None,
          postCode = None,
          jointVentureProperty = optJointVentureProperty,
          postAction = routes.CheckYourAnswersPartnershipsController.submit()
        ))
      case (_, _) => utrPostcodePredicate { (utr, postcode) =>
        Ok(check_your_answers_partnerships(
          entityType = GeneralPartnership,
          companyUtr = Some(utr),
          companyNumber = None,
          postCode = Some(postcode),
          jointVentureProperty = optJointVentureProperty,
          postAction = routes.CheckYourAnswersPartnershipsController.submit()
        ))
      }
    }
  }

  private def showLimitedPartnershipAnswers(entity: LimitedPartnershipBase)(implicit request: Request[_]): Result = {
    crnPredicate { crn =>
      utrPostcodePredicate { (utr, postcode) =>
        Ok(check_your_answers_partnerships(
          entityType = entity,
          companyUtr = Some(utr),
          companyNumber = Some(crn),
          postCode = Some(postcode),
          jointVentureProperty = None,
          postAction = routes.CheckYourAnswersPartnershipsController.submit()
        ))
      }
    }
  }

  private def utrPostcodePredicate(f: (String, PostCode) => Result)(implicit request: Request[_]): Result = {

    val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
    val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)

    (optPartnershipUtr, optPartnershipPostCode) match {
      case (Some(utr), Some(postcode)) => f(utr, postcode)
      case (None, _) => Redirect(routes.CapturePartnershipUtrController.show())
      case (_, None) => Redirect(routes.PrincipalPlacePostCodeController.show())
    }
  }

  private def crnPredicate(f: String => Result)(implicit request: Request[_]): Result = {

    val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)
    val optPartnershipType = request.session.getModel[PartnershipEntityType](SessionKeys.partnershipTypeKey)

    (optPartnershipCrn, optPartnershipType) match {
      case (Some(crn), Some(_)) => f(crn)
      case (_, _) => Redirect(routes.CapturePartnershipCompanyNumberController.show())
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val optPartnershipType = request.session.getModel[PartnershipEntityType](SessionKeys.partnershipTypeKey)
      val optPartnershipUtr = request.session.get(SessionKeys.partnershipSautrKey).filter(_.nonEmpty)
      val optPartnershipPostCode = request.session.getModel[PostCode](SessionKeys.partnershipPostCodeKey)
      val optPartnershipCrn = request.session.get(SessionKeys.companyNumberKey).filter(_.nonEmpty)

      (optVatNumber, optPartnershipUtr, optPartnershipType, optPartnershipCrn, optPartnershipPostCode) match {
        case (Some(vatNumber), Some(partnershipUtr), Some(partnershipType: LimitedPartnershipEntityType), Some(companyNumber), Some(partnershipPostCode)) =>
          storePartnershipInformationService.storePartnershipInformation(
            vatNumber = vatNumber,
            sautr = partnershipUtr,
            companyNumber = companyNumber,
            partnershipEntity = partnershipType,
            postCode = Some(partnershipPostCode)
          ) map {
            case Right(StorePartnershipInformationSuccess) =>
              Redirect(principalRoutes.DirectDebitResolverController.show())
            case Left(PartnershipUtrNotFound) =>
              Redirect(routes.CouldNotConfirmKnownFactsController.show())
            case Left(StorePartnershipKnownFactsFailure) =>
              Redirect(routes.CouldNotConfirmKnownFactsController.show())
            case Left(StorePartnershipInformationFailureResponse(status)) =>
              throw new InternalServerException("Store Partnership failed with status code: " + status)
          }
        case (Some(vatNumber), Some(partnershipUtr), None, None, Some(partnershipPostCode)) =>
          storePartnershipInformationService.storePartnershipInformation(
            vatNumber = vatNumber,
            sautr = partnershipUtr,
            postCode = Some(partnershipPostCode)
          ) map {
            case Right(StorePartnershipInformationSuccess) =>
              Redirect(principalRoutes.DirectDebitResolverController.show())
            case Left(StorePartnershipKnownFactsFailure) =>
              Redirect(routes.CouldNotConfirmKnownFactsController.show())
            case Left(PartnershipUtrNotFound) =>
              Redirect(routes.CouldNotConfirmKnownFactsController.show())
            case Left(StorePartnershipInformationFailureResponse(status)) =>
              throw new InternalServerException("Store Partnership failed with status code: " + status)
          }
        case (None, _, _, _, _) =>
          Future.successful(
            Redirect(principalRoutes.ResolveVatNumberController.resolve())
          )
        case _ =>
          Future.successful(
            Redirect(routes.CapturePartnershipUtrController.show())
          )
      }
    }
  }

}
