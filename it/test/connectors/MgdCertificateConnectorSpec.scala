package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import itutil.ApplicationWithWiremock
import models.MgdCertificate
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext

class MgdCertificateConnectorSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val connector: MgdCertificateConnector =
    app.injector.instanceOf[MgdCertificateConnector]

  private val mgdRegNumber = "MGD12345"

  "getCertificate" should {

    "return MgdCertificate when BE returns 200 with valid JSON" in {

      stubFor(
        get(urlEqualTo(s"/gambling/mgd/$mgdRegNumber/certificate"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(
                """{
                  |  "mgdRegNumber": "MGD12345",
                  |  "certificateStatus": "ACTIVE",
                  |  "issuedDate": "2026-01-01"
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getCertificate(mgdRegNumber).futureValue

      result.mgdRegNumber mustBe "MGD12345"
      result.certificateStatus mustBe "ACTIVE"
    }

    "fail when BE returns 200 with invalid JSON" in {

      stubFor(
        get(urlEqualTo(s"/gambling/mgd/$mgdRegNumber/certificate"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getCertificate(mgdRegNumber).futureValue
      }

      ex.getMessage.toLowerCase must include("jserror")
    }

    "propagate UpstreamErrorResponse when BE returns 500" in {

      stubFor(
        get(urlEqualTo(s"/gambling/mgd/$mgdRegNumber/certificate"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("boom")
          )
      )

      val ex = intercept[UpstreamErrorResponse] {
        connector.getCertificate(mgdRegNumber).futureValue
      }

      ex.statusCode mustBe INTERNAL_SERVER_ERROR
    }

    "fail when BE returns non-200 unexpected status (e.g. 404)" in {

      stubFor(
        get(urlEqualTo(s"/gambling/mgd/$mgdRegNumber/certificate"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
              .withBody("not found")
          )
      )

      val ex = intercept[UpstreamErrorResponse] {
        connector.getCertificate(mgdRegNumber).futureValue
      }

      ex.statusCode mustBe NOT_FOUND
      ex.getMessage must include("Unexpected status")
    }
  }
}
