package org.nspl

import data._

case class DataElem(
    data: DataSource,
    xAxis: Axis,
    yAxis: Axis,
    renderers: Seq[DataRenderer],
    originalBounds: Bounds,
    tx: AffineTransform = AffineTransform.identity
) extends Renderable[DataElem] {
  def transform(tx: Bounds => AffineTransform) = {
    val ntx = tx(bounds).concat(this.tx)
    this.copy(tx = ntx)
  }
  def bounds = tx.transform(originalBounds)
}

object DataElem {
  implicit def dataElemRenderer[RC <: RenderingContext[RC]](implicit
      re: Renderer[ShapeElem, RC],
      rt: Renderer[TextBox, RC]
  ) = new Renderer[DataElem, RC] {
    def render(r: RC, e: DataElem): Unit = {
      e.data.iterator.foreach { row =>
        e.renderers.foreach { dr =>
          dr.render(row, e.xAxis, e.yAxis, r, e.tx)
        }
      }
      e.renderers.foreach(_.clear(r))
    }
  }
}

trait Plots {
  // format: off
  type XYPlotAreaType = Elems5[Elems2[Elems2[Elems2[Elems5[ElemList[ShapeElem],ElemList[ShapeElem],ElemList[DataElem],Elems2[Elems3[ShapeElem,ElemList[Elems2[ShapeElem,TextBox]],ElemList[ShapeElem]],Elems3[ShapeElem,ElemList[Elems2[ShapeElem,TextBox]],ElemList[ShapeElem]]],ShapeElem],TextBox],TextBox],TextBox],ShapeElem,ShapeElem,ShapeElem,ShapeElem]
  // format: on
  case class XYPlotArea(
      elem: XYPlotAreaType,
      xMin: Double,
      xMax: Double,
      yMin: Double,
      yMax: Double
  ) extends Renderable[XYPlotArea] {
    def transform(v: Bounds => AffineTransform) =
      this.copy(elem = elem.transform(v))
    def bounds: Bounds = elem.bounds
    def frameElem = elem.m1.m1.m1.m1.m5
  }

  object XYPlotArea {
    implicit def renderer[RC <: RenderingContext[RC]](implicit
        re: Renderer[ShapeElem, RC],
        rt: Renderer[TextBox, RC]
    ) = new Renderer[XYPlotArea, RC] {
      def render(r: RC, e: XYPlotArea): Unit =
        implicitly[Renderer[XYPlotAreaType, RC]].render(r, e.elem)
    }
  }

  def xyplotareaBuild[F: FC](
      data: Seq[(DataSource, List[DataRenderer])],
      xAxisSetting: AxisSettings,
      yAxisSetting: AxisSettings,
      origin: Option[Point] = None,
      xlim: Option[(Double, Double)] = None,
      ylim: Option[(Double, Double)] = None,
      xAxisMargin: Double = 0.05,
      yAxisMargin: Double = 0.05,
      xgrid: Boolean = true,
      ygrid: Boolean = true,
      frame: Boolean = true,
      xCustomGrid: Boolean = false,
      yCustomGrid: Boolean = false,
      main: String = "",
      mainFontSize: RelFontSize = 1.2 fts,
      mainDistance: RelFontSize = 1.2 fts,
      xlab: String = "",
      xlabFontSize: RelFontSize = 1.0 fts,
      xlabDistance: RelFontSize = 0.5 fts,
      xlabAlignment: Alignment = Center,
      ylab: String = "",
      ylabFontSize: RelFontSize = 1.0 fts,
      ylabDistance: RelFontSize = 0.5 fts,
      ylabAlignment: Alignment = Center,
      topPadding: RelFontSize = 0.2 fts,
      bottomPadding: RelFontSize = 0d fts,
      leftPadding: RelFontSize = 0d fts,
      rightPadding: RelFontSize = 0.2 fts,
      xNoTickLabel: Boolean = false,
      yNoTickLabel: Boolean = false
  ) = {
    val id = scala.util.Random.nextLong().toString
    Build(
      xyplotarea(
        id,
        data,
        xAxisSetting,
        yAxisSetting,
        origin,
        xlim,
        ylim,
        xAxisMargin,
        yAxisMargin,
        xgrid,
        ygrid,
        frame,
        xCustomGrid,
        yCustomGrid,
        main,
        mainFontSize,
        mainDistance,
        xlab,
        xlabFontSize,
        xlabDistance,
        xlabAlignment,
        ylab,
        ylabFontSize,
        ylabDistance,
        ylabAlignment,
        topPadding,
        bottomPadding,
        leftPadding,
        rightPadding,
        xNoTickLabel,
        yNoTickLabel
      )
    ) {
      case (Some(old), BuildEvent) =>
        import old._
        xyplotarea(
          id,
          data,
          xAxisSetting,
          yAxisSetting,
          origin,
          xlim,
          ylim,
          xAxisMargin,
          yAxisMargin,
          xgrid,
          ygrid,
          frame,
          xCustomGrid,
          yCustomGrid,
          main,
          mainFontSize,
          mainDistance,
          xlab,
          xlabFontSize,
          xlabDistance,
          xlabAlignment,
          ylab,
          ylabFontSize,
          ylabDistance,
          ylabAlignment,
          topPadding,
          bottomPadding,
          leftPadding,
          rightPadding,
          xNoTickLabel,
          yNoTickLabel
        )
      case (Some(old), Scroll(v1, p, plotAreaId)) if plotAreaId.id == id =>
        import old._
        val v = if (v1 > 0) 1.05 else if (v1 < 0) 0.95 else 1.0
        val mappedPoint = mapPoint(
          p,
          plotAreaId.bounds.get,
          Bounds(xMin, yMin, xMax - xMin, yMax - yMin),
          true
        )
        val xMid = mappedPoint.x
        val yMid = mappedPoint.y
        val xF = (xMid - xMin) / (xMax - xMin)
        val yF = (yMid - yMin) / (yMax - yMin)
        val xMin1 = xMid - (xMax - xMin) * xF * v
        val xMax1 = xMid + (xMax - xMin) * (1 - xF) * v
        val yMin1 = yMid - (yMax - yMin) * yF * v
        val yMax1 = yMid + (yMax - yMin) * (1 - yF) * v
        xyplotarea(
          id,
          data,
          xAxisSetting,
          yAxisSetting,
          origin,
          Some(xMin1 -> xMax1),
          Some(yMin1 -> yMax1),
          xAxisMargin,
          yAxisMargin,
          xgrid,
          ygrid,
          frame,
          xCustomGrid,
          yCustomGrid,
          main,
          mainFontSize,
          mainDistance,
          xlab,
          xlabFontSize,
          xlabDistance,
          xlabAlignment,
          ylab,
          ylabFontSize,
          ylabDistance,
          ylabAlignment,
          topPadding,
          bottomPadding,
          leftPadding,
          rightPadding,
          xNoTickLabel,
          yNoTickLabel
        )

      case (Some(old), Drag(dragStart, dragTo, plotAreaId))
          if plotAreaId.id == id =>
        import old._
        val dragStartWorld =
          mapPoint(
            dragStart,
            plotAreaId.bounds.get,
            Bounds(xMin, yMin, xMax - xMin, yMax - yMin),
            true
          )
        val dragToWorld = mapPoint(
          dragTo,
          plotAreaId.bounds.get,
          Bounds(xMin, yMin, xMax - xMin, yMax - yMin),
          true
        )
        val dragDirection = Point(
          dragStartWorld.x - dragToWorld.x,
          dragStartWorld.y - dragToWorld.y
        )

        val xT = dragDirection.x
        val yT = dragDirection.y

        val xMin1 = xMin + xT
        val xMax1 = xMax + xT
        val yMin1 = yMin + yT
        val yMax1 = yMax + yT
        xyplotarea(
          id,
          data,
          xAxisSetting,
          yAxisSetting,
          origin,
          Some(xMin1 -> xMax1),
          Some(yMin1 -> yMax1),
          xAxisMargin,
          yAxisMargin,
          xgrid,
          ygrid,
          frame,
          xCustomGrid,
          yCustomGrid,
          main,
          mainFontSize,
          mainDistance,
          xlab,
          xlabFontSize,
          xlabDistance,
          xlabAlignment,
          ylab,
          ylabFontSize,
          ylabDistance,
          ylabAlignment,
          topPadding,
          bottomPadding,
          leftPadding,
          rightPadding,
          xNoTickLabel,
          yNoTickLabel
        )
    }
  }

  def xyplotarea[F: FC](
      id: String,
      data: Seq[(DataSource, List[DataRenderer])],
      xAxisSetting: AxisSettings,
      yAxisSetting: AxisSettings,
      origin: Option[Point] = None,
      xlim: Option[(Double, Double)] = None,
      ylim: Option[(Double, Double)] = None,
      xAxisMargin: Double = 0.05,
      yAxisMargin: Double = 0.05,
      xgrid: Boolean = true,
      ygrid: Boolean = true,
      frame: Boolean = true,
      xCustomGrid: Boolean = false,
      yCustomGrid: Boolean = false,
      main: String = "",
      mainFontSize: RelFontSize = 1.2 fts,
      mainDistance: RelFontSize = 1.2 fts,
      xlab: String = "",
      xlabFontSize: RelFontSize = 1.0 fts,
      xlabDistance: RelFontSize = 0.5 fts,
      xlabAlignment: Alignment = Center,
      ylab: String = "",
      ylabFontSize: RelFontSize = 1.0 fts,
      ylabDistance: RelFontSize = 0.5 fts,
      ylabAlignment: Alignment = Center,
      topPadding: RelFontSize = 0.2 fts,
      bottomPadding: RelFontSize = 0d fts,
      leftPadding: RelFontSize = 0d fts,
      rightPadding: RelFontSize = 0.2 fts,
      xNoTickLabel: Boolean = false,
      yNoTickLabel: Boolean = false
  ) = {

    val xMinMax = data.flatMap { case (data, renderers) =>
      renderers.flatMap { renderer =>
        renderer.xMinMax(data)
      }
    }

    val yMinMax = data.flatMap { case (data, renderers) =>
      renderers.flatMap { renderer =>
        renderer.yMinMax(data)
      }
    }

    val xLimMin = xlim.map(_._1).filterNot(_.isNaN)
    val xLimMax = xlim.map(_._2).filterNot(_.isNaN)

    val yLimMin = ylim.map(_._1).filterNot(_.isNaN)
    val yLimMax = ylim.map(_._2).filterNot(_.isNaN)

    val dataXMin =
      if (xLimMin.isDefined) 0.0
      else if (xMinMax.isEmpty) 0d
      else xMinMax.map(_.min).min
    val dataXMax =
      if (xLimMax.isDefined) 0.0
      else if (xMinMax.isEmpty) 1d
      else xMinMax.map(_.max).max
    val dataYMin =
      if (yLimMin.isDefined) 0.0
      else if (xMinMax.isEmpty) 0d
      else yMinMax.map(_.min).min
    val dataYMax =
      if (yLimMax.isDefined) 0.0
      else if (xMinMax.isEmpty) 1d
      else yMinMax.map(_.max).max

    val xMin = xAxisSetting.axisFactory match {
      case LinearAxisFactory =>
        math.min(
          xLimMin.getOrElse {
            dataXMin - xAxisMargin * (dataXMax - dataXMin)
          },
          origin.map(_.x).getOrElse(Double.MaxValue)
        )
      case Log10AxisFactory =>
        math.min(
          xLimMin.getOrElse(math.pow(10d, math.log10(dataXMin).floor)),
          origin.map(_.x).getOrElse(Double.MaxValue)
        )
    }

    val xMax = xAxisSetting.axisFactory match {
      case LinearAxisFactory => {
        val xMax1 = xLimMax.getOrElse {
          dataXMax + xAxisMargin * (dataXMax - dataXMin)
        }
        if (xMax1 == xMin) {
          xMax1 + 1
        } else xMax1
      }
      case Log10AxisFactory =>
        val xMax1 = xLimMax.getOrElse {
          math.pow(10d, math.log10(dataXMax).ceil)
        }
        if (xMax1 == xMin) {
          xMax1 + 1
        } else xMax1

    }

    val yMin = yAxisSetting.axisFactory match {
      case LinearAxisFactory =>
        math.min(
          yLimMin.getOrElse {
            dataYMin - yAxisMargin * (dataYMax - dataYMin)
          },
          origin.map(_.y).getOrElse(Double.MaxValue)
        )
      case Log10AxisFactory =>
        math.min(
          yLimMin.getOrElse(math.pow(10d, math.log10(dataYMin).floor)),
          origin.map(_.y).getOrElse(Double.MaxValue)
        )
    }

    val yMax = yAxisSetting.axisFactory match {
      case LinearAxisFactory => {
        val yMax1 = yLimMax.getOrElse {
          dataYMax + yAxisMargin * (dataYMax - dataYMin)
        }
        if (yMax1 == yMin) yMax1 + 1
        else yMax1
      }
      case Log10AxisFactory =>
        val yMax1 = yLimMax.getOrElse {
          math.pow(10d, math.log10(dataYMax).ceil)
        }
        if (yMax1 == yMin) {
          yMax1 + 1
        } else yMax1
    }

    val xAxis =
      xAxisSetting.axisFactory.make(xMin, xMax, xAxisSetting.width.value, true)
    val yAxis =
      yAxisSetting.axisFactory.make(yMin, yMax, yAxisSetting.width.value, false)

    val xMinV = xAxis.worldToView(xMin)
    val xMaxV = xAxis.worldToView(xMax)
    val yMinV = yAxis.worldToView(yMin)
    val yMaxV = yAxis.worldToView(yMax)

    val yAxisViewMin = yAxisSetting.lineStartFraction * yAxis.width
    val yAxisViewMax =
      yAxisViewMin + yAxisSetting.width.value * yAxisSetting.lineLengthFraction

    val xAxisViewMin = xAxisSetting.lineStartFraction * xAxis.width
    val xAxisViewMax =
      xAxisViewMin + xAxisSetting.width.value * xAxisSetting.lineLengthFraction

    val originWX1 = origin.map { origin =>
      xlim.map { case (a, b) =>
        if (origin.x < a) a
        else if (origin.x > b) b
        else origin.x
      } getOrElse origin.x
    } getOrElse xMin

    val originWY1 = origin.map { origin =>
      ylim.map { case (a, b) =>
        if (origin.y < a) a
        else if (origin.y > b) b
        else origin.y
      } getOrElse origin.y
    } getOrElse yMin

    val noXTick = if (origin.isEmpty) Nil else List(originWX1)
    val noYTick = if (origin.isEmpty) Nil else List(originWY1)

    val (xMajorTicks, xCustomTicks, xAxisElem) =
      xAxisSetting.renderable(xAxis, xNoTickLabel, noXTick)
    val (yMajorTicks, yCustomTicks, yAxisElem) = yAxisSetting.renderable(
      yAxis,
      yNoTickLabel,
      noYTick
    )

    val originX = xAxis.worldToView(originWX1)
    val originY = yAxis.worldToView(originWY1)

    val axes = group(
      translate(xAxisElem, 0, originY),
      translate(yAxisElem, originX, 0),
      FreeLayout
    )

    val dataelem = sequence(data.toList.map { case (ds, drs) =>
      DataElem(ds, xAxis, yAxis, drs, axes.bounds, AffineTransform.identity)
    })

    val xgridPoints =
      if (xgrid) {
        if (xCustomGrid) (xMajorTicks ++ xCustomTicks).distinct else xMajorTicks
      } else Nil

    val ygridPoints =
      if (ygrid) {
        if (yCustomGrid) (yMajorTicks ++ yCustomTicks).distinct else yMajorTicks
      } else Nil

    val xgridElem = sequence(xgridPoints map { w =>
      val v = xAxis.worldToView(w)
      ShapeElem(
        Shape.line(Point(v, yAxisViewMin), Point(v, yAxisViewMax)),
        stroke =
          Some(Stroke(lineWidth.value * 0.5, dash = List((0.2 fts).value))),
        strokeColor = Color.gray5
      )
    })

    val frameStroke = if (frame) Some(Stroke(lineWidth.value)) else None
    val frameElem =
      ShapeElem(
        Shape.rectangle(
          xMinV,
          yMaxV,
          xMaxV - xMinV,
          math.abs(yMinV - yMaxV),
          anchor = Some(Point(xMinV, yMaxV))
        ),
        stroke = frameStroke,
        fill = Color.transparent
      ).withIdentifier(PlotAreaIdentifier(id, None))

    val ygridElem = sequence(ygridPoints map { w =>
      val v = yAxis.worldToView(w)
      ShapeElem(
        Shape.line(
          Point(xAxisViewMin, v),
          Point(xAxisViewMax, v)
        ),
        stroke =
          Some(Stroke(lineWidth.value * 0.5, dash = List((0.2 fts).value))),
        strokeColor = Color.gray5
      )
    })

    val renderedPlot =
      group(xgridElem, ygridElem, dataelem, axes, frameElem, FreeLayout)

    val mainBox =
      TextBox(main, fontSize = mainFontSize, width = Some(frameElem.bounds.w))
    val xlabBox =
      TextBox(xlab, fontSize = xlabFontSize, width = Some(frameElem.bounds.w))
    val ylabBox =
      TextBox(ylab, fontSize = ylabFontSize, width = Some(frameElem.bounds.h))

    val withHorizontalLabels = zgroup(
      (
        zgroup(
          (renderedPlot, 1),
          (
            AlignTo.verticalGapBeforeReference(
              AlignTo.horizontalCenter(mainBox, frameElem.bounds),
              frameElem.bounds,
              mainDistance.value
            ),
            0
          ),
          FreeLayout
        ),
        0
      ),
      (AlignTo.horizontal(xlabBox, frameElem.bounds, xlabAlignment), 1),
      VerticalStack(NoAlignment, xlabDistance)
    )

    val movedFrame = withHorizontalLabels.m1.m1.m5

    val plotWithAxisLabels =
      zgroup(
        (withHorizontalLabels, 1),
        (
          AlignTo.vertical(
            rotate(ylabBox, 0.5 * math.Pi),
            movedFrame.bounds,
            ylabAlignment
          ),
          0
        ),
        HorizontalStack(NoAlignment, ylabDistance)
      )

    val movedFrame2 = plotWithAxisLabels.m1.m1.m1.m5

    val padTop = AlignTo.verticalGapBeforeReference(
      AlignTo.horizontalCenter(
        ShapeElem(
          shape = Shape.line(Point(0d, 0d), Point(0d, topPadding.value)),
          fill = Color.transparent,
          strokeColor = Color.blue,
          stroke = None
        ),
        movedFrame2.bounds
      ),
      movedFrame2.bounds,
      0d
    )

    val padBottom = AlignTo.verticalGapAfterReference(
      AlignTo.horizontalCenter(
        ShapeElem(
          shape = Shape.line(Point(0d, 0d), Point(0d, bottomPadding.value)),
          fill = Color.transparent,
          strokeColor = Color.green,
          stroke = None
        ),
        movedFrame2.bounds
      ),
      movedFrame2.bounds,
      0d
    )

    val padLeft = AlignTo.horizontalGapBeforeReference(
      AlignTo.verticalCenter(
        ShapeElem(
          shape = Shape.line(Point(0d, 0d), Point(leftPadding.value, 0d)),
          fill = Color.transparent,
          strokeColor = Color.red,
          stroke = None
        ),
        movedFrame2.bounds
      ),
      movedFrame2.bounds,
      0d
    )

    val padRight = AlignTo.horizontalGapAfterReference(
      AlignTo.verticalCenter(
        ShapeElem(
          shape = Shape.line(Point(0d, 0d), Point(rightPadding.value, 0d)),
          fill = Color.transparent,
          strokeColor = Color.black,
          stroke = None
        ),
        movedFrame2.bounds
      ),
      movedFrame2.bounds,
      0d
    )

    val elem = group(
      plotWithAxisLabels,
      padLeft,
      padBottom,
      padTop,
      padRight,
      FreeLayout
    )

    XYPlotArea(elem, xMin, xMax, yMin, yMax)

  }

  sealed trait LegendElem
  case class PointLegend(shape: Shape, color: Color) extends LegendElem
  case class LineLegend(stroke: Stroke, color: Color) extends LegendElem

  type Legend = ElemList[Elems2[ElemList[ShapeElem], TextBox]]

  def legend[F: FC](
      entries: List[(String, Seq[LegendElem])],
      fontSize: RelFontSize = 1.0 fts,
      width: RelFontSize = 30 fts,
      layout: Layout
  ): Legend = {
    val lineHeight =
      TextBox("A", fontSize = fontSize, width = Some(width.value)).bounds.h

    sequence(
      entries.map { case (text, legendElems) =>
        val renderables = legendElems.map {
          case PointLegend(s, c) =>
            fitToHeight(
              ShapeElem(s, fill = c),
              lineHeight * 0.8
            )
          case LineLegend(s, c) =>
            fitToHeight(
              ShapeElem(
                Shape.rectangle(0d, 0d, fontSize.value * 2, 1d),
                strokeColor = c,
                stroke = Some(s)
              ),
              lineHeight * 0.1
            )

        }

        // needs centering on the vertical axis
        val aligned = sequence(renderables, HorizontalStack(Center))

        val textbox =
          TextBox(text, fontSize = fontSize, width = Some(width.value))

        group(
          aligned,
          TextBox(text, fontSize = fontSize, width = Some(width.value)),
          HorizontalStack(Center, fontSize)
        )
      },
      layout
    )
  }

  type HeatmapLegend = Elems2[Elems2[ElemList[ShapeElem], AxisElem], TextBox]

  def heatmapLegend[F: FC](
      min: Double,
      max: Double,
      color: Colormap = HeatMapColors(0d, 1d),
      fontSize: RelFontSize = 1.0 fts,
      width: RelFontSize = 10 fts,
      height: RelFontSize = 1 fts,
      labelText: String = "",
      numTicks: Int = 2
  ): HeatmapLegend = {

    val color1 = color.withRange(min, max)

    val axisSettings = AxisSettings(
      LinearAxisFactory,
      fontSize = fontSize,
      width = width,
      numTicks = numTicks,
      tickAlignment = 1,
      forceMajorTickOnMax = true,
      forceMajorTickOnMin = true
    )
    val axis = axisSettings.axisFactory.make(min, max, width.value, false)

    val (_, _, axisElem) =
      axisSettings.renderable(
        axis,
        false,
        Nil
      )

    val axisLabel = {
      val box =
        TextBox(labelText, fontSize = fontSize, width = Some(width.value))
      rotate(box, -0.5 * math.Pi)
    }

    val n = 500
    val space = (max - min) / n.toDouble
    val ticks = sequence(
      ((0 until n toList) map { i =>
        val world = axis.min + i * space
        val view = axis.worldToView(world)
        ShapeElem(
          Shape.line(Point(1d, view), Point(height.value, view)),
          stroke = Some(Stroke(lineWidth.value)),
          strokeColor = color1(world)
        )
      })
    )

    group(
      group(ticks, axisElem, FreeLayout),
      axisLabel,
      HorizontalStack(Center, 1 fts)
    )

  }

}
