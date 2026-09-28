/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.actions

import base.SpecBase
import models.Regime
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import services.{AgentClientAuthResult, AgentClientAuthService}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedActionSpec extends SpecBase {

  class Harness(authorisedAction: AuthorisedAction) {
    def onPageLoad: Action[AnyContent] = authorisedAction { request =>
      Results.Ok(request.regNum)
    }
  }

  val bodyParser: BodyParsers.Default = BodyParsers.Default(Helpers.stubPlayBodyParsers)

  private def agentAuth(result: AgentClientAuthResult): AgentClientAuthService =
    new AgentClientAuthService(null) {
      override def authoriseClient(regime: Regime, regNumber: String)(using hc: HeaderCarrier): Future[AgentClientAuthResult] =
        Future.successful(result)
    }

  private val defaultAgentAuth: AgentClientAuthService = agentAuth(AgentClientAuthResult.Authorised)

  private def authConnectorReturning(affinityGroup: Option[AffinityGroup], enrolments: Enrolments): AuthConnector = {
    val m: AuthConnector = mock[AuthConnector]
    (m
      .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using _: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(`~`(affinityGroup, enrolments)))
    m
  }

  private val mgdAgentEnrolments: Enrolments =
    Enrolments(Set(Enrolment("HMRC-MGD-AGNT", Seq(EnrolmentIdentifier("HMRCMGDAGENTREF", "XWA00003000000")), "Activated")))

  "AuthorisedAction" - {
    "create AuthorisedRequest when user has an Organisation affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(
                Set(Enrolment("HMRC-MGD-ORG", Seq(EnrolmentIdentifier("HMRCMGDRN", "XGM00003122200")), "Activated"))
              )
            )
          )
        )
      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe OK
      contentAsString(result) mustBe "XGM00003122200"
    }

    "create AuthorisedRequest for an agent authorised for the client (hasClient), using the client regNum from session" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Agent),
              Enrolments(
                Set(Enrolment("HMRC-MGD-AGNT", Seq(EnrolmentIdentifier("HMRCMGDAGENTREF", "XWA00003000000")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test").withSession("regNum" -> "XGM00003122200"))
      status(result) mustBe OK
      contentAsString(result) mustBe "XGM00003122200"
    }

    "redirect to access denied page when Organisation has a non-MGD HMRC-GTS-GBD enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(
                Set(Enrolment("HMRC-GTS-GBD", Seq(EnrolmentIdentifier("HMRCGTSGBRN", "XWA00003000000")), "Activated"))
              )
            )
          )
        )
      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when Organisation has a non-MGD HMRC-GTS-PBD enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(
                Set(Enrolment("HMRC-GTS-PBD", Seq(EnrolmentIdentifier("HMRCGTSGBRN", "XNA00003200000")), "Activated"))
              )
            )
          )
        )
      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when Organisation has a non-MGD HMRC-GTS-RGD enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(
                Set(Enrolment("HMRC-GTS-RGD", Seq(EnrolmentIdentifier("HMRCGTSGBRN", "XEA00003400000")), "Activated"))
              )
            )
          )
        )
      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when Agent has a non-MGD HMRC-GTS-AGNT enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Agent),
              Enrolments(
                Set(Enrolment("HMRC-GTS-AGNT", Seq(EnrolmentIdentifier("HMRCGTSAGENTREF", "XWA00003000000")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when user has no affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              None,
              Enrolments(Set())
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when an agent has no enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Agent),
              Enrolments(Set())
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when an organisation has no enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(Set())
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when user is agent but has enrolment for organisation" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Agent),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-ORG", Seq(EnrolmentIdentifier("CHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when user is an organisation but has enrolment for agent" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-AGENT", Seq(EnrolmentIdentifier("AGENTCHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when an Individual affinity group tries to access with incorrect enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Individual),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-IND", Seq(EnrolmentIdentifier("INDCHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page when an Individual affinity group tries to access with agent enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Individual),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-AGENT", Seq(EnrolmentIdentifier("AGENTCHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to login when user has no active session" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(Future.failed(new NoActiveSession("No session") {}))

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value must startWith(testFrontendAppConfig.loginUrl)
    }

    "redirect to access denied page for an agent who does not hold the client (NotAuthorised)" in {
      val mockAuthConnector = authConnectorReturning(Some(AffinityGroup.Agent), mgdAgentEnrolments)

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, agentAuth(AgentClientAuthResult.NotAuthorised), bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test").withSession("regNum" -> "XGM00003122200"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    "redirect to access denied page for an agent with no client regNum in session" in {
      val mockAuthConnector = authConnectorReturning(Some(AffinityGroup.Agent), mgdAgentEnrolments)

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, defaultAgentAuth, bodyParser)

      val controller = new Harness(authorisedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.routes.AccessDeniedController.onPageLoad().url
      )
    }

    Seq(AgentClientAuthResult.NotReady, AgentClientAuthResult.Failed).foreach { authResult =>
      s"redirect an agent to the journey recovery page (not access denied) when the client list result is $authResult" in {
        val mockAuthConnector = authConnectorReturning(Some(AffinityGroup.Agent), mgdAgentEnrolments)

        val authorisedAction =
          new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, agentAuth(authResult), bodyParser)

        val controller = new Harness(authorisedAction)
        val result = controller.onPageLoad(FakeRequest("GET", "/test").withSession("regNum" -> "XGM00003122200"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.JourneyRecoveryController.onPageLoad().url
        )
      }
    }

  }
}
