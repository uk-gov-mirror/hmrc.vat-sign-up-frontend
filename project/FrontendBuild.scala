import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object FrontendBuild extends Build with MicroService {

  val appName = "vat-subscription-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  private val playWhitelistFilterVersion = "1.1.0"

  private val hmrcTestVersion = "3.0.0"
  private val scalaTestVersion = "3.0.1"
  private val scalaTestPlusVersion = "2.0.0"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion =  "1.8.1"
  private val mockitoVersion = "2.7.6"

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.18.0",
    "uk.gov.hmrc" %% "play-ui" % "7.13.0",
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "1.3.0",
    "uk.gov.hmrc" %% "play-whitelist-filter" % playWhitelistFilterVersion
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
    "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
    "org.pegdown" % "pegdown" % pegdownVersion % scope,
    "org.jsoup" % "jsoup" % jsoupVersion % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.mockito" % "mockito-core" % mockitoVersion % scope
  )

}
