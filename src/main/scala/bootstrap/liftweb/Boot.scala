package bootstrap.liftweb

import com.corp.model.Todo
import com.corp.snippet.TodoOps
import net.liftmodules.JQueryModule
import net.liftweb.common._
import net.liftweb.http.ContentSourceRestriction.UnsafeInline
import net.liftweb.http._
import net.liftweb.http.js.jquery.JQueryArtifacts
import net.liftweb.mapper.{DB, Schemifier, StandardDBVendor}
import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap._
import net.liftweb.util
import net.liftweb.util.Props

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot extends Logger {
  def boot {

    if (!DB.jndiJdbcConnAvailable_?) {
      sys.props.put("h2.implicitRelativePath", "true")
      val vendor = new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
        Props.get("db.url") openOr
          "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
        Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(() => vendor.closeAllConnections_!())

      DB.defineConnectionManager(util.DefaultConnectionIdentifier, vendor)
    }

    Schemifier.schemify(true, Schemifier.infoF _, Todo)

    // where to search snippet
    LiftRules.addToPackages("com.corp")

    val entries = List(
      Menu.i("Home") / "home",
      Menu.i("Static") / "static" / ** >> Hidden
    ) ::: TodoOps.menus

    // Build SiteMap
    def sitemap = SiteMap(entries: _*)

    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery = JQueryModule.JQuery172
    JQueryModule.init()

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemap)

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    //Lift CSP settings see http://content-security-policy.com/ and
    //Lift API for more information.
    LiftRules.securityRules = () => {
      SecurityRules(content = Some(ContentSecurityPolicy(
        scriptSources = List(
          ContentSourceRestriction.Self),
        styleSources = List(
          ContentSourceRestriction.All, UnsafeInline)
      )))
    }

  }
}
