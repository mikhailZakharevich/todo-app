package com.corp.model

import net.liftweb.mapper._

object Todo extends Todo with LongKeyedMetaMapper[Todo]

class Todo extends LongKeyedMapper[Todo] with IdPK {

  override def getSingleton: KeyedMetaMapper[Long, Todo] = Todo

  object description extends MappedTextarea(this, 100) with ValidateLength {
    override def textareaRows  = 5
    override def textareaCols = 10
  }

}
