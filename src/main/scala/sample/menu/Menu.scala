package main.scala.sample.menu

import scala.io.StdIn

class Menu {
  //ADD CLASSES TO MENU
  private var options : Map[Int, MenuOption] = Map(
    1 -> new ListMinInstalledCount(),
    2 -> new SearchByDeveloper(),
  )

  def getOption(i: Int): MenuOption = {
    options.get(i).orNull
  }

  def getOptions(): Map[Int, MenuOption] = {
    options
  }

  def addOption(index: Int, menuOption: MenuOption): Unit = {
    options = options + (index -> menuOption)
  }

  def removeOption(index: Int): Unit = {
    options = options - (index)
  }

  def printOptions(): Unit = {
    println("============ MENU ==============================")
    for ((k, _) <- options) {
      val menuOption = getOption(k).toString
      println(f"$k -- $menuOption")
    }
    println("================================================")
  }

  def askOption(): Int = {
    printOptions()
    print("Please select an option: ")
    var selectedOption = StdIn.readLine().toInt
    while (!getOptions().contains(selectedOption)) {
      println("\nThat option does not exist")
      print("Please select an option: ")
      selectedOption = StdIn.readLine().toInt
    }
    selectedOption
  }

}
