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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.OptionalSautrJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.models.{No, Yes}
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.do_you_have_a_utr
import uk.gov.hmrc.vatsignupfrontend.forms.DoYouHaveAUtrForm._

import scala.concurrent.Future

@Singleton
class DoYouHaveAUtrController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(OptionalSautrJourney)) {
  // TODO: change to OptionalSautrJourney feature switch

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(do_you_have_a_utr(
          doYouHaveAUtrForm(isAgent = false),
          routes.DoYouHaveAUtrController.submit()
        ))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        doYouHaveAUtrForm(isAgent = false).bindFromRequest.fold(
          formWithErrors =>
            BadRequest(do_you_have_a_utr(
              formWithErrors,
              routes.DoYouHaveAUtrController.submit()
            ))
          ,
          {
            case Yes =>
              Redirect(routes.CapturePartnershipUtrController.show())
                .addingToSession(SessionKeys.optionalUtrKey -> true.toString)
            case No =>
              Redirect(routes.CheckYourAnswersPartnershipsController.show())
                .removingFromSession(SessionKeys.partnershipSautrKey)
                .removingFromSession(SessionKeys.businessPostCodeKey)
                .addingToSession(SessionKeys.optionalUtrKey -> false.toString)
          }
        )
      )
    }
  }

}
