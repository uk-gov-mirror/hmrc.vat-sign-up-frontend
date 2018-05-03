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

//$COVERAGE-OFF$Disabling scoverage

package uk.gov.hmrc.vatsubscriptionfrontend.testonly.controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.vatsubscriptionfrontend.config.{AppConfig, ControllerComponents}
import uk.gov.hmrc.vatsubscriptionfrontend.testonly.forms.StubIssuerRequestForm._
import uk.gov.hmrc.vatsubscriptionfrontend.testonly.models.StubIssuerRequest
import uk.gov.hmrc.vatsubscriptionfrontend.testonly.services.StubIssuerService
import uk.gov.hmrc.vatsubscriptionfrontend.testonly.views.html.stub_issuer

import scala.concurrent.Future

@Singleton
class StubIssuerController @Inject()(val controllerComponents: ControllerComponents,
                                     stubIssuerService: StubIssuerService
                                    ) extends FrontendController with I18nSupport {

  override val messagesApi: MessagesApi = controllerComponents.messagesApi

  implicit val appConfig: AppConfig = controllerComponents.appConfig

  val show: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(
      Ok(stub_issuer(stubIssuerForm.fill(StubIssuerRequest(
        safeId = "",
        isSuccessful = true,
        vatNumber = None,
        postCode = None,
        registrationDate = None,
        errorMessage = None
      )), routes.StubIssuerController.submit()))
    )
  }


  val submit: Action[AnyContent] = Action.async { implicit request =>
    stubIssuerForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(
          BadRequest(stub_issuer(formWithErrors, routes.StubIssuerController.submit()))
        ),
      stubIssuerRequest =>
        stubIssuerService.callIssuer(stubIssuerRequest).map {
          case Right(_) => Ok("success")
          case Left(err) => BadGateway(err.toString)
        }
    )
  }

}

// $COVERAGE-ON$
