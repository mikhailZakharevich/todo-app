package com.corp.snippet

import com.corp.model.Todo
import net.liftweb.common.{Failure, Full, Logger}
import net.liftweb.http.{RequestVar, S, SHtml}
import net.liftweb.sitemap.Loc.Hidden
import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._

import scala.xml._

object TodoOps {

  val listMenu = Menu.i("TODO List") / "todo" / "list"
  val createMenu = Menu.i("Create TODO") / "todo" / "create"
  val editMenu = Menu.i("Edit TODO") / "todo" / "edit" >> Hidden
  val deleteMenu = Menu.i("Delete TODO") / "todo" / "delete" >> Hidden
  val viewMenu = Menu.i("View TODO") / "todo" / "view" >> Hidden

  val menus: List[Menu.Menuable] =
    listMenu ::
      createMenu ::
      editMenu ::
      deleteMenu ::
      viewMenu ::
      Nil

}

class TodoOps extends Logger {

  import TodoOps._
  import net.liftweb.http.SHtml.ElemAttr._

  private val listMenuPath = listMenu.loc.calcDefaultHref
  private val editMenuPath = editMenu.loc.calcDefaultHref
  private val viewMenuPath = viewMenu.loc.calcDefaultHref
  private val deleteMenuPath = deleteMenu.loc.calcDefaultHref

  object todoVar extends RequestVar[Todo](Todo.create)

  def process = {
    todoVar.asValid match {
      case Full(todo) =>
        todo.description.get match {
          case e if e.isEmpty => S.error("errorText", "Text cannot be empty")
          case _ =>
            val saved = todo.saveMe
            S.redirectTo(listMenuPath)
            saved
        }
      case Failure(e, _, _) => S.error("errorText", e)
      case _ => S.error("errorText", "Unexpected error")
    }
  }

  def view = {

    isSet(todoVar)

    val todo = todoVar.is

    "#description" #> todo.description.asHtml &
      "#edit" #> SHtml.link(editMenuPath, () => todoVar(todo), Text("edit"), ("class", "btn"))
  }

  def list = {
    val todoList = Todo.findAll

    ".trow *" #> todoList.map { item =>
      ".description *" #> item.description.get &
        ".actions *" #> {
          SHtml.link(viewMenuPath, () => todoVar(item), Text("view"), ("class", "btn")) ++
            SHtml.link(editMenuPath, () => todoVar(item), Text("edit"), ("class", "btn")) ++
            SHtml.link(deleteMenuPath, () => todoVar(item), Text("delete"), ("class", "btn btn-outline-danger"))
        }
    }
  }

  def create = {
    val todo = todoVar.is
    "#hidden" #> SHtml.hidden(() => todoVar(todo)) &
      "#description" #>
        SHtml.textarea(todo.description.get, descr => todo.description(descr)) &
      "#submit" #> SHtml.onSubmitUnit(process _)
  }

  def edit = {
    isSet(todoVar)

    val todo = todoVar.is
    "#hidden" #> SHtml.hidden(() => todoVar(todo)) &
      "#description" #>
        SHtml.textarea(todo.description.get, descr => todo.description(descr)) &
      "#submit" #> SHtml.onSubmitUnit(process _)

  }

  def delete = {
    isSet(todoVar)

    val todo = todoVar.is
    "#yes" #> SHtml.link(listMenuPath, () => todo.delete_!, Text("Yes"), ("class", "btn btn-danger")) &
      "#no" #> SHtml.link(listMenuPath, () => (), Text("No"), ("class", "btn"))
  }

  def isSet(requestVar: RequestVar[Todo]): Unit =
    if (!todoVar.set_?) S.redirectTo(listMenuPath)

}
