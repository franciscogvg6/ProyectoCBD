package sample.components

import java.awt.Color
import scala.swing.{Component, Graphics2D}

class PieChartComponent(data: Seq[(String,Double)], colors: Seq[Color]) extends Component {
  val total = data.map(d => d._2).sum

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    val centerX : Int = this.size.width / 2
    val centerY : Int = this.size.height / 2
    val radius = centerX.min(centerY) - 10

    var angle = 0.0
    var y = 10
    var it = 0
    data.zip(colors).foreach(d => {
      // DRAW THE ARC
      val sectionAngle = 360.0 * d._1._2 / 100
      g.setColor(d._2)
      g.fillArc(centerX-radius, centerY-radius, radius*2, radius*2, angle.round.toInt, sectionAngle.round.toInt)
      if (it == data.size-1){
        angle = 360.0 - angle
      } else {
        angle += sectionAngle
      }

      // DRAW THE LEGEND
      g.fillRect(10,y,20,20)
      g.setColor(Color.BLACK)
      g.drawString(f"${d._1._1}: ${d._1._2}%.2f",40,y+15)
      y += 25

      it += 1
    })

  }
}
