package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import com.jocmp.mallet.TableData

@Composable
fun TableLayout(
    tableData: TableData,
    allowHorizontalScroll: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (row: Int, column: Int) -> Unit,
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }
    val rowHeights = remember { mutableStateMapOf<Int, Int>() }
    val horizontalScrollState: ScrollState = rememberScrollState()

    val scrollModifier = if (allowHorizontalScroll) {
        Modifier.horizontalScroll(horizontalScrollState)
    } else {
        Modifier
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Layout(
            modifier = scrollModifier,
            content = {
                tableData.cells.forEach { tableCell ->
                    if (tableCell.rowSpan > 0 && tableCell.colSpan > 0) {
                        content(tableCell.row, tableCell.column)
                    }
                }
            },
        ) { measurables, _ ->
            val placeables = measurables.mapIndexed { index, measurable ->
                val tableCell = tableData.cells[index]
                val minWidth = (0 until tableCell.colSpan).sumOf {
                    columnWidths.getOrDefault(tableCell.column + it, 0)
                }
                val minHeight = (0 until tableCell.rowSpan).sumOf {
                    rowHeights.getOrDefault(tableCell.row + it, 0)
                }

                measurable.measure(
                    Constraints(
                        minWidth = minWidth,
                        maxWidth = Constraints.Infinity,
                        minHeight = minHeight,
                        maxHeight = Constraints.Infinity,
                    )
                )
            }

            placeables.forEachIndexed { index, placeable ->
                val tableCell = tableData.cells[index]
                val widthPerColumn = placeable.width / tableCell.colSpan
                val heightPerRow = placeable.height / tableCell.rowSpan

                (tableCell.column until tableCell.column + tableCell.colSpan).forEach { col ->
                    columnWidths[col] = maxOf(columnWidths[col] ?: 0, widthPerColumn)
                }
                (tableCell.row until tableCell.row + tableCell.rowSpan).forEach { row ->
                    rowHeights[row] = maxOf(rowHeights[row] ?: 0, heightPerRow)
                }
            }

            val totalWidth = columnWidths.values.sum()
            val totalHeight = rowHeights.values.sum()

            layout(width = totalWidth, height = totalHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val tableCell = tableData.cells[index]
                    val x = (0 until tableCell.column).sumOf { columnWidths.getOrDefault(it, 0) }
                    val y = (0 until tableCell.row).sumOf { rowHeights.getOrDefault(it, 0) }

                    placeable.place(x, y)
                }
            }
        }
    }
}
