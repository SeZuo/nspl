package org

package object nspl
    extends Tuples1
    with Tuples2
    with Colors
    with Shapes
    with Renderers
    with data.DataAdaptors
    with Plots
    with SimplePlots
    with ImplicitConversions
    with Events {

  type Build[A] = ((Option[A], Event)) => A

  implicit class defaultBuild[T](b: Build[T]) {
    def build: T = b(None -> BuildEvent)
  }

  type AxisElem = Elems3[ShapeElem, ElemList[Elems2[ShapeElem, TextBox]], ElemList[ShapeElem]]

  type FC[_] = FontConfiguration

  implicit def baseFont(implicit fc: FontConfiguration): BaseFontSize = BaseFontSize(fc.font.size)

  implicit def rel2ft(v: RelFontSize)(implicit s: FontConfiguration): Double = v.v * s.font.size

  implicit class ConvD(v: Double) {
    def fts = RelFontSize(v)
  }
  implicit class ConvI(v: Int) {
    def fts = RelFontSize(v.toDouble)
  }
  implicit class ConvRFS(v: RelFontSize) {
    def value(implicit bs: FontConfiguration) = rel2ft(v)(bs)
  }

  def importFont(name: String)(implicit gm: GlyphMeasurer[NamedFont#F]) = GenericFontConfig(NamedFont(name, 10))(gm)

  def mapEvent[A <: Renderable[A], B <: Renderable[B]](old: (Option[A], Event))(f: A => B): (Option[B], Event) = old match {
    case (None, BuildEvent) => None -> BuildEvent
    case (Some(old), e) =>
      val b = f(old)
      Some(b) -> e.mapBounds(old.bounds, b.bounds)
    case _ => throw new RuntimeException("should not happen")
  }

  /* Calculates the total bounds of the members. */
  def outline(members: Seq[Bounds]) = {
    val x = members.map(_.x).min
    val y = members.map(_.y).min
    val maxX = members.map(_.maxX).max
    val maxY = members.map(_.maxY).max
    val w = maxX - x
    val h = maxY - y
    Bounds(x, y, w, h)
  }

  def transform[T <: Renderable[T]](member: T, tx: Bounds => AffineTransform): T =
    member.transform(tx)

  def translate[T <: Renderable[T]](member: T, x: Double, y: Double): T =
    member.translate(x, y)

  def rotate[T <: Renderable[T]](
    member: T, rad: Double, x: Double, y: Double
  ): T = member.rotate(rad, x, y)

  def rotate[T <: Renderable[T]](member: T, rad: Double) =
    member.rotate(rad)

  def reflectOrigin[T <: Renderable[T]](member: T) =
    member.reflectOrigin

  def reflectX[T <: Renderable[T]](member: T) =
    member.reflectX

  def rotateCenter[T <: Renderable[T]](member: T, rad: Double) =
    member.rotateCenter(rad)

  def reflectY[T <: Renderable[T]](member: T) =
    member.reflectY

  def scale[T <: Renderable[T]](member: T, x: Double, y: Double) =
    member.scale(x, y)

  def fitToBounds[T <: Renderable[T]](member: T, bounds: Bounds) =
    {
      val current = member.bounds
      scale(
        translate(member, bounds.x - current.x, bounds.y - current.y),
        if (current.w != 0d) bounds.w / current.w else 1d,
        if (current.h != 0d) bounds.h / current.h else 1d
      )
    }

  implicit def renderable2build[T <: Renderable[T]](elem: T): Build[T] =
    Build.const(elem)

  def fitToWidth[T <: Renderable[T]](elem: T, width: Double) = {
    val aspect = elem.bounds.h / elem.bounds.w
    val height = (width * aspect).toInt
    val bounds = Bounds(0, 0, width, height)
    fitToBounds(elem, bounds)
  }

  def sequence[T <: Renderable[T]](members: Seq[T], layout: Layout): ElemList[T] =
    {
      val orig = members.map(_.bounds)
      val n = layout(orig)
      val transformed = n zip members map (x => fitToBounds(x._2, x._1))
      ElemList(transformed.toList)
    }

  def sequence[T <: Renderable[T]](members: Seq[T]): ElemList[T] = sequence(members, FreeLayout)

  def sequence[T <: Renderable[T]](members: Seq[Build[T]], layout: Layout): Build[ElemList[T]] =
    {
      case (Some(old), e: Event) =>
        val bounds = old.members.map(_.bounds)
        val n = layout(bounds)
        val members1 = (n zip bounds zip members).zipWithIndex map {
          case (((to, from), build), idx) =>
            build(Some(old.members(idx)), e.mapBounds(to, from))
        }
        val transformed = n zip members1 map (x => fitToBounds(x._2, x._1))
        ElemList(transformed.toList)
      case (None, BuildEvent) =>
        sequence(members.map(_.build), layout)
      case _ => throw new RuntimeException("should not happen")
    }

  def sequence[T <: Renderable[T]](members: Seq[Build[T]]): Build[ElemList[T]] = sequence(members, FreeLayout)

  def sequence2[T1 <: Renderable[T1], T2 <: Renderable[T2]](members: Seq[Either[T1, T2]], layout: Layout): ElemList2[T1, T2] =
    {
      val bounds = members.map(_.fold(_.bounds, _.bounds))

      val n = layout(bounds)

      val transformed = n zip members map (x => x._2 match {
        case scala.util.Left(y) => scala.util.Left(fitToBounds(y, x._1))
        case scala.util.Right(y) => scala.util.Right(fitToBounds(y, x._1))
      })
      ElemList2(transformed)
    }

  def sequence2[T1 <: Renderable[T1], T2 <: Renderable[T2]](members1: Seq[Either[Build[T1], Build[T2]]], layout: Layout): Build[ElemList2[T1, T2]] =
    {
      case (None, BuildEvent) => sequence2(members1.map(_.fold(x => scala.util.Left(x.build), x => scala.util.Right(x.build))), layout)
      case (Some(old), e: Event) =>
        val bounds = old.members.map(_.fold(_.bounds, _.bounds))

        val n = layout(bounds)

        val members: Seq[Either[T1, T2]] = (n zip bounds zip members1).zipWithIndex.map {
          case (((from, to), build), idx) =>
            build match {
              case scala.util.Left(x) => scala.util.Left(x(Some(old.members(idx).left.get) -> e.mapBounds(from, to)))
              case scala.util.Right(x) => scala.util.Right(x(Some(old.members(idx).right.get) -> e.mapBounds(from, to)))
            }
        }

        val transformed = n zip members map (x => x._2 match {
          case scala.util.Left(y) => scala.util.Left(fitToBounds(y, x._1))
          case scala.util.Right(y) => scala.util.Right(fitToBounds(y, x._1))
        })
        ElemList2(transformed)
      case _ => throw new RuntimeException("should not happen")
    }

  /* Normalized scientific notation. */
  def scientific(x: Double) = x / math.pow(10d, math.log10(x).round) -> math.log10(x).round

  def mapPoint(p: Point, from: Bounds, to: Bounds): Point =
    if (from.w == 0 || from.h == 0) Point(0d, 0d)
    else {
      val xF = to.w / from.w
      val yF = to.h / from.h
      Point(
        (p.x - from.x) * xF + to.x,
        (p.y - from.y) * yF + to.y
      )
    }

}
