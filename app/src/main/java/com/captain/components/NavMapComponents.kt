package com.captain.components

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.captain.services.navsvc.NavMap
import com.captain.control.BotCaptainEvents
import com.captain.control.BotCaptainState
import com.captain.services.navsvc.LidarLogEntry
import com.captain.services.navsvc.LidarMap
import com.captain.services.navsvc.PositionLogEntry
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlin.math.round

class NavMapComponents {
    val convertToInches = true

    @Composable
    fun MapContainer (botCaptainState: BotCaptainState, eventHandler: BotCaptainEvents) {
        val headerText =  remember { mutableStateOf(if (botCaptainState.newestLogEntry.equals(""))  "No Maps Yet" else botCaptainState.newestLogEntry) }
        eventHandler.setNavMapListener(headerText)

        Column (Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            // this will recompose when [header] changes, but not when [names] changes
            Text (text = headerText.value, style = MaterialTheme.typography.titleMedium)
            Button(onClick = { eventHandler.refreshSession() }) {
                Text(text = "Refresh")
            }
            Divider()

            val pointsData = ArrayList<PointF> ()
            for (pl in botCaptainState.positionLog) {
                pointsData.add(PointF(pl.positionX, pl.positionY))
            }

            val searchHitPoints = ArrayList<PointF> ()
            for (sh in botCaptainState.searchHits) {
                searchHitPoints.add(PointF(sh.estX, sh.estY))
            }

            if (pointsData.size > 0) {
                MapImage(pointsData, searchHitPoints, botCaptainState)
            }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    @Composable
    private fun MapImage(
        pointsData: List<PointF>, searchHitPoints: List<PointF>, botCaptainState: BotCaptainState
    ) {
        val navMap = botCaptainState.navMap
        val boundaryData = extractBoundaries(navMap)
        if (boundaryData.isEmpty()) return

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            val textMeasurer = rememberTextMeasurer()
            //Canvas(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.size(800.dp)) {
                // Total number of transactions.
                //val totalRecords = transactionsPerSecond.transactions.size

                // Maximum distance between dots (transactions)
                //val lineDistance = size.width / (totalRecords + 1)

                // Canvas height
                //val cHeight = size.height

                // Draw the boundaries, basically just a rectangle around the canvas. all
                // other points will be scaled within this rectangle
                val visualBoundaries = ArrayList<PointF>()
                val boundaryWidth = 3f
                visualBoundaries.add(PointF(1f, 1f));
                visualBoundaries.add(PointF(this.size.width-boundaryWidth, 1f));
                visualBoundaries.add(PointF(this.size.width-boundaryWidth, this.size.height-boundaryWidth));
                visualBoundaries.add(PointF(1.0f, this.size.height-boundaryWidth));


                var pointNum = 0
                for (b in visualBoundaries) {
                    pointNum += 1

                    // Draw a line from this boundary to the next
                    var nextPoint = visualBoundaries.get(0)
                    if (pointNum < visualBoundaries.size) {
                        nextPoint = visualBoundaries.get(pointNum)
                    }

                    Log.i(
                        "Drawing",
                        "Drawing boundary line ${b.x},${b.y} to ${nextPoint.x},${nextPoint.y}"
                    )

                    drawLine(
                        start = Offset(
                            x = b.x,
                            y = b.y
                        ),
                        end = Offset(
                            x = nextPoint.x,
                            y = nextPoint.y
                        ),
                        color = Color(40, 193, 218),
                        strokeWidth = 3f
                    )
                }

                drawGridLines(this, boundaryData = boundaryData, navMap = navMap, textMeasurer = textMeasurer)

                // Gray out obstacles
                for (obstacleName in navMap.obstacles.keys) {
                    val o = navMap.obstacles[obstacleName]
                    val leastPoint = PointF(o!!.xmin, o!!.ymin) // this is where he rectangle starts, since the screen x/y is flipped
                    val greatestPoint = PointF(o!!.xmax, o!!.ymax)

                    val androidTopLeftX = CalculateX(leastPoint.x, leastPoint.y, size.height, size.width, navMap)
                    val androidTopLeftY = CalculateY(leastPoint.x, leastPoint.y, size.height, size.width, navMap)

                    val androidBottomRightX = CalculateX(greatestPoint.x, greatestPoint.y, size.height, size.width, navMap)
                    val androidBottomRightY = CalculateY(greatestPoint.x, greatestPoint.y, size.height, size.width, navMap)

                    drawRect(
                        topLeft = Offset(
                            x = androidTopLeftX,
                            y = androidTopLeftY),
                        size = Size(width = androidBottomRightX - androidTopLeftX, height= androidBottomRightY - androidTopLeftY),
                        color = Color(220, 220, 220),
                        alpha = 0.45F
                    )
                }

                // Draw the position movements
                pointNum = 0
                for (p in pointsData) {
                    pointNum += 1

                    // Draw a line from this point to the next
                    if (pointNum < pointsData.size) {
                        val nextPoint = pointsData.get(pointNum)
                        drawLine(
                            start = Offset(
                                x = CalculateX(p.x, p.y, size.height, size.width, navMap),
                                y = CalculateY(p.x, p.y, size.height, size.width, navMap)
                            ),
                            end = Offset(
                                x = CalculateX(
                                    nextPoint.x,
                                    nextPoint.y,
                                    size.height,
                                    size.width,
                                    navMap
                                ),
                                y = CalculateY(
                                    nextPoint.x,
                                    nextPoint.y,
                                    size.height,
                                    size.width,
                                    navMap
                                )
                            ),
                            color = Color(200, 20, 20),
                            strokeWidth = Stroke.DefaultMiter
                        )
                    }
                }

                // Draw the search hits
                for (p in searchHitPoints) {
                    Log.i("scale","Circle for search hit at X ${p.x} , ${p.y}")
                    drawCircle(
                        radius = 4.0f,
                        center = Offset(
                            x = CalculateX(p.x, p.y, size.height, size.width, navMap),
                            y = CalculateY(p.x, p.y, size.height, size.width, navMap)
                        ),
                        color = Color(20, 200, 20),
                    )
                }

                // draw landmarks
                drawLandmarks(this, navMap = navMap, textMeasurer = textMeasurer)

                // draw lidar data, if available
                val lastPositionEntry = botCaptainState.positionLog[botCaptainState.positionLog.size - 1]
                val lidarMap : LidarMap? = getMatchingLidarMap (botCaptainState.lidarMaps, lastPositionEntry, 5000F)
                if (lidarMap != null) {
                    drawLidar(this, lidarMap = lidarMap, logEntry = lastPositionEntry, navMap = navMap)
                }
            }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    private fun drawGridLines (drawScope: DrawScope, boundaryData: List<PointF>, navMap: NavMap, textMeasurer: TextMeasurer) {
        val xMin = boundaryData.minWith(Comparator.comparing {it.x })
        val yMin = boundaryData.minWith(Comparator.comparing {it.y })
        val xMax = boundaryData.maxWith(Comparator.comparing {it.x })
        val yMax = boundaryData.maxWith(Comparator.comparing {it.y })
        val gridColor = Color(200,200,200)

        Log.i("Drawing", "XMin: ${xMin.x}, XMax: ${xMax.x}, YMin: ${yMin.y}, YMax: ${yMax.y}")

        // want even number of lines, no more than 2 per 300 pixels
        val horizontalLines = round((drawScope.size.height / 300) * 2)
        val verticalLines = round((drawScope.size.width / 300) * 2)

        val xSpacing = round((yMax.y - yMin.y) / horizontalLines)
        val ySpacing = round((xMax.x - xMin.x) / verticalLines)

        // find the first axis
        var startingPosition = 0f
        while (startingPosition + xSpacing < yMax.y) {
            startingPosition += xSpacing
        }

        // Draw each horizontal grid line
        var lastGridY = startingPosition
        while (lastGridY > yMin.y) {
            drawLine(
                xStart = xMin.x,
                yStart = lastGridY,
                xEnd = xMax.x,
                yEnd = lastGridY,
                drawScope = drawScope,
                isAxis = (round(lastGridY) == 0f),
                navMap = navMap,
                gridColor = gridColor)

            drawScope.drawText(
                textMeasurer.measure("  ${round(lastGridY).toInt()}"),
                topLeft = Offset(
                    x=CalculateX(xMin.x, lastGridY, drawScope.size.height, drawScope.size.width, navMap),
                    y=CalculateY(xMin.x, lastGridY, drawScope.size.height, drawScope.size.width, navMap)),
                color = gridColor
            )
            lastGridY -= xSpacing
        }


        // find the first y
        startingPosition = 0f
        while (startingPosition - ySpacing > xMin.x) {
            startingPosition -= ySpacing
        }

        // Draw each vertical grid line
        var lastGridX = startingPosition
        while (lastGridX < xMax.x) {
            drawLine(
                xStart = lastGridX,
                yStart = yMin.y,
                xEnd = lastGridX,
                yEnd = yMax.y,
                drawScope = drawScope,
                isAxis = (round(lastGridX) == 0f),
                navMap = navMap,
                gridColor = gridColor)

            drawScope.drawText(
                textMeasurer.measure("  ${round(lastGridX).toInt()}"),
                topLeft = Offset(
                    x=CalculateX(lastGridX, yMin.y+((yMax.y - yMin.y)*.05).toFloat(), drawScope.size.height, drawScope.size.width, navMap),
                    y=CalculateY(lastGridX, yMin.y+((yMax.y - yMin.y)*.05).toFloat(), drawScope.size.height, drawScope.size.width, navMap)),
                color = gridColor
            )
            lastGridX += ySpacing
        }
    }

    private fun drawLine(xStart : Float, yStart : Float, xEnd : Float, yEnd : Float, drawScope: DrawScope, isAxis: Boolean, navMap: NavMap, gridColor : Color) {
        drawScope.drawLine(
            start = Offset(
                x=CalculateX(xStart, yStart, drawScope.size.height, drawScope.size.width, navMap),
                y=CalculateY(xStart, yStart, drawScope.size.height, drawScope.size.width, navMap)),
            end = Offset(
                x=CalculateX(xEnd, yEnd, drawScope.size.height, drawScope.size.width, navMap),
                y=CalculateY(xEnd, yEnd, drawScope.size.height, drawScope.size.width, navMap)),
            color = gridColor,
            strokeWidth = if (isAxis) 3f else 2f,
            alpha = if (isAxis) 0.5f else 0.25f
        )
    }

    @OptIn(ExperimentalTextApi::class)
    private fun drawLandmarks (drawScope: DrawScope, navMap: NavMap, textMeasurer: TextMeasurer) {

        for (lid in navMap.landmarks.keys) {
            val l = navMap.landmarks[lid]
            if (l != null) {
                val boundaries = navMap.boundaries
                var landmarkX = l.x
                var landmarkY = l.y
                var outOfBounds = false
                // If the landmark is out of bounds, draw it right on the boundary, maybe hollow
                if (l.x > boundaries.xmax) {
                    outOfBounds = true
                    landmarkX = boundaries.xmax
                }
                else if (l.x < boundaries.xmin) {
                    outOfBounds = true
                    landmarkX = boundaries.xmin
                }
                if (l.y > boundaries.ymax) {
                    outOfBounds = true
                    landmarkY = boundaries.ymax
                }
                else if (l.y < boundaries.ymin) {
                    outOfBounds = true
                    landmarkY = boundaries.ymin
                }


                Log.i("scale","Landmark at X ${l.x} , ${l.y}")
                var drawStyle : DrawStyle = Stroke(5.0F)
                if (l.lidarVisible) {
                    drawStyle = Fill
                }

                drawScope.drawCircle(
                    radius = 12.0f,
                    center = Offset(
                        x = CalculateX(landmarkX, landmarkY, drawScope.size.height, drawScope.size.width, navMap),
                        y = CalculateY(landmarkX, landmarkY, drawScope.size.height, drawScope.size.width, navMap)
                    ),
                    color = Color(255, 228, 51),
                    alpha = .3F + (.7F * (.1F * l.priority)), // higher priority get more opacity
                    style = drawStyle
                )
            }
        }
    }

    private fun CalculateX(x: Float, y: Float, height : Float, width : Float, navMap: NavMap) : Float {
        val mapWidth = navMap.boundaries.xmax - navMap.boundaries.xmin
        val multiplier = (width / mapWidth)// - 0.2 // leave 10% on either side
        val bufferSize = 0//width * 0.1 // 5 % of screen width, should leave 5% on the right as well
        val mapXOffset = (0 - navMap.boundaries.xmin) + bufferSize // positive means we have to add to get to the coordinate start

        // Scale the x up
        val scaledX = ((x + mapXOffset) * multiplier).toFloat()

        //Log.i("scale","X sclaed ${x} to ${scaledX}, width: ${width}, map width: ${mapWidth}, mult: ${multiplier}, buff: ${bufferSize}, offset: ${mapXOffset}")
        return scaledX
    }

    private fun CalculateY(x: Float, y: Float, height : Float, width : Float, navMap: NavMap) : Float {
        val mapHeight = navMap.boundaries.ymax - navMap.boundaries.ymin
        var multiplier = (height / mapHeight)//0.2 // leave 10% on either side
        //multiplier *= -1 // y axis is flipped
        val bufferSize = 0//height * 0.1 // 5 % of screen height
        val mapYOffset = (0 - navMap.boundaries.ymin) + bufferSize // positive means we have to add to get to the coordinate start

        // Scale the x up
        val scaledY = ((y  + mapYOffset) * multiplier).toFloat()
        //Log.i("scale","Y sclaed ${y} to ${height-scaledY}, height: ${height}, map height: ${mapHeight}, mult: ${multiplier}, buff: ${bufferSize}, offset: ${mapYOffset}")

        return height - scaledY
    }

    private fun extractBoundaries (navMap: NavMap) : List<PointF> {
        val boundaryData = ArrayList<PointF>()
        val boundaries = navMap.boundaries
        boundaryData.add(PointF(boundaries.xmin, boundaries.ymin))
        boundaryData.add(PointF(boundaries.xmax, boundaries.ymin))
        boundaryData.add(PointF(boundaries.xmax, boundaries.ymax))
        boundaryData.add(PointF(boundaries.xmin, boundaries.ymax))
        return boundaryData
    }

    private fun getMatchingLidarMap (lidarMaps: List<LidarMap>, positionLogEntry: PositionLogEntry, maxSecondsDifference : Float) : LidarMap? {
        // find the nearest lidar log entry to the given position log entry
        // if there seems to be a lidar entry that matches up with it, that's the one we will use
        val sPositionTime = if (positionLogEntry.occurred.endsWith('Z')) positionLogEntry.occurred.substring(0, positionLogEntry.occurred.length - 1) else positionLogEntry.occurred
        val positionTime = LocalDateTime.parse(sPositionTime)
        for (lm in lidarMaps.reversed()) {
            val sOccurred = if (lm.occurred.endsWith('Z')) lm.occurred.substring(0, lm.occurred.length - 1) else lm.occurred
            val occurred = LocalDateTime.parse(sOccurred)

            val secondsDiff = ChronoUnit.SECONDS.between(occurred, positionTime)
            if (secondsDiff.absoluteValue <= maxSecondsDifference) {
                return lm
            }
        }

        return null
    }

    @OptIn(ExperimentalTextApi::class)
    private fun drawLidar (drawScope: DrawScope, lidarMap: LidarMap, logEntry: PositionLogEntry, navMap: NavMap) {
        //Log.i("Lidar", "Drawing lidar map: ${lidarMap.lidarData}")

        // All lidar points are drawn (anglewise) from the perspective of the vehicle, as reported
        // by the position
        // Convert vehicle heading to a 0:360 value for easier lidar comparison
        var heading = logEntry.heading
        if (heading < 0f) {
            heading += 360.0f
        }

        val rotatedLidarMap : HashMap<Float,Float> = HashMap<Float,Float>()
        for (sOriginalAngle in lidarMap.lidarData.keys) {
            if (lidarMap.lidarData[sOriginalAngle] != null && lidarMap.lidarData[sOriginalAngle]!! > 0f) {
                val vehicleRelativeLidarAngle = sOriginalAngle.toFloat()
                var mapRelativeLidarAngle = vehicleRelativeLidarAngle + heading

                if (mapRelativeLidarAngle > 360f) {
                    mapRelativeLidarAngle -= 360f
                }

                // convert to inches
                if (convertToInches) {
                    rotatedLidarMap[mapRelativeLidarAngle] = lidarMap!!.lidarData[sOriginalAngle]!! / 25.4f
                }
                else {
                    rotatedLidarMap[mapRelativeLidarAngle] = lidarMap!!.lidarData[sOriginalAngle]!!
                }
            }
        }

        //Log.i("Lidar", "Adjusted lidar map: ${rotatedLidarMap}")

        // Now draw the shape, scaled a little smaller so it shows up
        val boundaries = navMap.boundaries
        for (lidarAngle in rotatedLidarMap.keys) {
            if (rotatedLidarMap[lidarAngle] != null) {
                val lidarPoint = getRelativeXY(
                    logEntry.positionX,
                    logEntry.positionY,
                    rotatedLidarMap[lidarAngle]!!,
                    lidarAngle,
                    1.0f
                )

                Log.i("lidar","Lidar reading ${lidarAngle}:${rotatedLidarMap[lidarAngle]!!} makes point at X ${lidarPoint.x} , ${lidarPoint.y}")

                var outOfBounds = false
                // If the landmark is out of bounds, draw it right on the boundary, maybe hollow
                if (lidarPoint.x > boundaries.xmax) {
                    outOfBounds = true
                    lidarPoint.x = boundaries.xmax
                }
                else if (lidarPoint.x < boundaries.xmin) {
                    outOfBounds = true
                    lidarPoint.x = boundaries.xmin
                }
                if (lidarPoint.y > boundaries.ymax) {
                    outOfBounds = true
                    lidarPoint.y = boundaries.ymax
                }
                else if (lidarPoint.y < boundaries.ymin) {
                    outOfBounds = true
                    lidarPoint.y = boundaries.ymin
                }

                var drawStyle : DrawStyle = Stroke(2.0F)
                drawScope.drawCircle(
                    radius = 5.0f,
                    center = Offset(
                        x = CalculateX(lidarPoint.x, lidarPoint.y, drawScope.size.height, drawScope.size.width, navMap),
                        y = CalculateY(lidarPoint.x, lidarPoint.y, drawScope.size.height, drawScope.size.width, navMap)
                    ),
                    color = Color(35, 35, 255),
                    alpha = .5F,
                    style = drawStyle
                )

            }
        }

    }

    fun getRelativeXY (positionX: Float, positionY: Float, distance: Float, degrees: Float, scaleFactor : Float) : PointF {
        // for the cosine/sine calculations to work, 0 needs to be right, 180 left
        var cartesianDegrees : Double = degrees.toDouble() - 90
        if (cartesianDegrees < 0) {
            cartesianDegrees += 360
        }

        // Cartesian degrees goes opposite way as mine, that is... 0 right, 90 up, bigger number pushes angle to left
        // so need to reverse the degrees sort of
        cartesianDegrees = (360 - cartesianDegrees)

        val estimatedX = positionX + distance * scaleFactor * Math.cos(Math.toRadians(cartesianDegrees))
        val estimatedY = positionY + distance * scaleFactor * Math.sin(Math.toRadians(cartesianDegrees))

        return PointF(estimatedX.toFloat(), estimatedY.toFloat())
    }
}

