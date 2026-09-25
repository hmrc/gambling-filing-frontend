/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import models.ClientListStatus
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

class GamblingConnectorSpec extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with WireMockSupport {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val regime = "mgd"
  private val regNumber = "XGM00003122200"

  private def buildApp(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.gambling.host" -> wireMockHost,
        "microservice.services.gambling.port" -> wireMockPort
      )
      .build()

  "GamblingConnector" - {

    "startClientListRetrieval" - {

      "must POST to the retrieval start endpoint and map the returned status" in {
        stubFor(
          post(urlEqualTo(s"/gambling/agent/client-list/$regime/retrieval/start"))
            .willReturn(okJson("""{"result":"succeeded"}"""))
        )

        val app = buildApp()
        running(app) {
          val connector = app.injector.instanceOf[GamblingConnector]
          connector.startClientListRetrieval(regime).futureValue mustEqual ClientListStatus.Succeeded
        }
      }

      "must fail when the status payload is not recognised" in {
        stubFor(
          post(urlEqualTo(s"/gambling/agent/client-list/$regime/retrieval/start"))
            .willReturn(okJson("""{"result":"nonsense"}"""))
        )

        val app = buildApp()
        running(app) {
          val connector = app.injector.instanceOf[GamblingConnector]
          connector.startClientListRetrieval(regime).failed.futureValue mustBe a[RuntimeException]
        }
      }
    }

    "hasClient" - {

      "must GET the has-client endpoint and return the boolean" in {
        stubFor(
          get(urlEqualTo(s"/gambling/agent/has-client/$regime/$regNumber"))
            .willReturn(okJson("""{"hasClient":true}"""))
        )

        val app = buildApp()
        running(app) {
          val connector = app.injector.instanceOf[GamblingConnector]
          connector.hasClient(regime, regNumber).futureValue mustBe true
        }
      }
    }
  }
}
