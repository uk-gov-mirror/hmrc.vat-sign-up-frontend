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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CharityJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreCharityInformationHttpParser.{StoreCharityInformationFailureResponse, StoreCharityInformationSuccess}
import uk.gov.hmrc.vatsignupfrontend.services.StoreCharityInformationService

import scala.concurrent.Future

@Singleton
class CharityResolverController @Inject()(val controllerComponents: ControllerComponents,
                                          storeCharityInformationService: StoreCharityInformationService)
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(CharityJourney)) {

  val resolve: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)

      optVatNumber match {
        case Some(vatNumber) =>
          storeCharityInformationService.storeCharityInformation(vatNumber = vatNumber) map {
            case Right(StoreCharityInformationSuccess) =>
              Redirect(routes.DirectDebitResolverController.show())
            case Left(StoreCharityInformationFailureResponse(status)) =>
              throw new InternalServerException("store charity information failed: status=" + status)
          }
        case _ =>
          Future.successful(Redirect(routes.ResolveVatNumberController.resolve()))
      }
    }
  }

}
