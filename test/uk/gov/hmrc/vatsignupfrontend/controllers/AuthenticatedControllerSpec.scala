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

package uk.gov.hmrc.vatsignupfrontend.controllers

import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.{EmptyRetrieval, Retrieval, Retrievals, ~}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents

import scala.concurrent.Future

class AuthenticatedControllerSpec extends UnitSpec with MockControllerComponents {

  object TestAuthenticatedController extends AuthenticatedController() {
    override def controllerComponents: ControllerComponents = mockControllerComponents
  }

  implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(FakeRequest().headers)

  "authorise" when {
    "a class level predicate has been set" when {
      "there are no method level retrievals set" should {
        "perform the provided block when auth returns the correct retrievals" in {
          mockAuthorise(EmptyPredicate, Retrievals.nino)(Future.successful(Some("")))

          val redirect = SeeOther("/")

          object TestAuthenticatedController extends AuthenticatedController(
            new RetrievalPredicate[Option[String]] {
              override def retrieval: Retrieval[Option[String]] = Retrievals.nino

              override def function(block: => Future[Result]): Option[String] => Future[Result] = {
                case Some(nino) => block
                case None => Future.successful(redirect)
              }
            }
          ) {
            override def controllerComponents: ControllerComponents = mockControllerComponents
          }

          val res = await(TestAuthenticatedController.authorised()(Future.successful(Ok)))
          res shouldBe Ok
        }

        "handle the failure when auth does not return the correct retrieval" in {
          mockAuthorise(EmptyPredicate, Retrievals.nino)(Future.successful(None))

          val redirect = SeeOther("/")

          object TestAuthenticatedController extends AuthenticatedController(
            new RetrievalPredicate[Option[String]] {
              override def retrieval: Retrieval[Option[String]] = Retrievals.nino

              override def function(block: => Future[Result]): Option[String] => Future[Result] = {
                case Some(nino) => block
                case None => Future.successful(redirect)
              }
            }
          ) {
            override def controllerComponents: ControllerComponents = mockControllerComponents
          }

          val res = await(TestAuthenticatedController.authorised()(Future.successful(Ok)))
          res shouldBe redirect
        }
      }
      "there is a method level retrieval" should {
        "perform the provided block when auth returns the correct retrievals with the requested retrieval" in {
          mockAuthorise(EmptyPredicate, Retrievals.nino and Retrievals.affinityGroup)(
            Future.successful(new ~(
              Some(""),
              Some(AffinityGroup.Agent)
            ))
          )

          val redirect = SeeOther("/")

          object TestAuthenticatedController extends AuthenticatedController(
            new RetrievalPredicate[Option[String]] {
              override def retrieval: Retrieval[Option[String]] = Retrievals.nino

              override def function(block: => Future[Result]): Option[String] => Future[Result] = {
                case Some(nino) => block
                case None => Future.successful(redirect)
              }
            }
          ) {
            override def controllerComponents: ControllerComponents = mockControllerComponents
          }

          val res = await(TestAuthenticatedController.authorised()(Retrievals.affinityGroup) {
            case Some(AffinityGroup.Agent) => Future.successful(Ok)
            case _ => fail()
          })
          res shouldBe Ok
        }
      }
    }
    "a class level predicate has not been set" when {
      "a method level retrieval has been set" should {
        "perform the method level retrieval only" in {
          mockAuthorise(EmptyPredicate, EmptyRetrieval and Retrievals.nino)(Future.successful(new ~(Unit, Some(""))))

          object TestAuthenticatedController extends AuthenticatedController() {
            override def controllerComponents: ControllerComponents = mockControllerComponents
          }

          val res = await(TestAuthenticatedController.authorised()(Retrievals.nino){
            case Some(_) => Future.successful(Ok)
            case _ => fail()
          })
          res shouldBe Ok
        }
      }
    }
  }
}
