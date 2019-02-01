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

import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch._
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_business_entity

import scala.concurrent.Future

@Singleton
class CaptureBusinessEntityController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AdministratorRolePredicate) {
  val validateBusinessEntityForm: Form[BusinessEntity] = businessEntityForm(isAgent = false)

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(capture_business_entity(
          businessEntityForm = validateBusinessEntityForm,
          postAction = routes.CaptureBusinessEntityController.submit(),
          generalPartnershipEnabled = isEnabled(GeneralPartnershipJourney),
          limitedPartnershipEnabled = isEnabled(LimitedPartnershipJourney),
          vatGroupEnabled = isEnabled(VatGroupJourney),
          divisionEnabled = isEnabled(DivisionJourney),
          unincorporatedAssociationEnabled = isEnabled(UnincorporatedAssociationJourney),
          trustEnabled = isEnabled(TrustJourney),
          registeredSocietyEnabled = isEnabled(RegisteredSocietyJourney),
          charityEnabled = isEnabled(CharityJourney),
          govOrgEnabled = isEnabled(GovernmentOrganisationJourney)
        ))
      )
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      validateBusinessEntityForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(capture_business_entity(
              businessEntityForm = formWithErrors,
              postAction = routes.CaptureBusinessEntityController.submit(),
              generalPartnershipEnabled = isEnabled(GeneralPartnershipJourney),
              limitedPartnershipEnabled = isEnabled(LimitedPartnershipJourney),
              vatGroupEnabled = isEnabled(VatGroupJourney),
              divisionEnabled = isEnabled(DivisionJourney),
              unincorporatedAssociationEnabled = isEnabled(UnincorporatedAssociationJourney),
              trustEnabled = isEnabled(TrustJourney),
              registeredSocietyEnabled = isEnabled(RegisteredSocietyJourney),
              charityEnabled = isEnabled(CharityJourney),
              govOrgEnabled = isEnabled(GovernmentOrganisationJourney)
            ))
          ),
        businessEntity => {
          businessEntity match {
            case SoleTrader =>
              Future.successful(Redirect(soletrader.routes.SoleTraderResolverController.resolve()))
            case LimitedCompany =>
              Future.successful(Redirect(routes.CaptureCompanyNumberController.show()))
            case GeneralPartnership =>
              Future.successful(Redirect(partnerships.routes.ResolvePartnershipUtrController.resolve()))
            case LimitedPartnership =>
              Future.successful(Redirect(partnerships.routes.CapturePartnershipCompanyNumberController.show()))
            case VatGroup =>
              Future.successful(Redirect(routes.VatGroupResolverController.resolve()))
            case Division =>
              Future.successful(Redirect(routes.DivisionResolverController.resolve()))
            case UnincorporatedAssociation =>
              Future.successful(Redirect(routes.UnincorporatedAssociationResolverController.resolve()))
            case Trust =>
              Future.successful(Redirect(routes.TrustResolverController.resolve()))
            case RegisteredSociety =>
              Future.successful(Redirect(routes.CaptureRegisteredSocietyCompanyNumberController.show()))
            case Charity =>
              Future.successful(Redirect(routes.CharityResolverController.resolve()))
            case GovernmentOrganisation =>
              Future.successful(Redirect(routes.GovOrgResolverController.resolve()))
            case Other =>
              Future.successful(Redirect(routes.CannotUseServiceController.show()))
          }
        } map (_.addingToSession(SessionKeys.businessEntityKey, businessEntity))
      )
    }
  }

}
