package com.ruchij.shared.utils

object CollectionUtils {
  def getIndex[A](array: Array[A], index: Int): Option[A] =
    if (array.length > index && index >= 0) Some(array(index)) else None
}
