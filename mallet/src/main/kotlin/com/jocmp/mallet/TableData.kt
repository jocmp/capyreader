package com.jocmp.mallet

data class TableData private constructor(
    val cells: List<TableCell>,
) {
    constructor(row: Int, column: Int) : this(
        List(row * column) { index ->
            TableCell(
                row = index / column,
                rowSpan = 1,
                column = index % column,
                colSpan = 1,
            )
        },
    )

    init {
        var lastSpanned = false
        for (cell in cells) {
            val isSpanned = cell.rowSpan > 1 || cell.colSpan > 1
            check(!lastSpanned && !isSpanned || isSpanned) {
                "Spanned cells should come after non-spanned cells"
            }
            lastSpanned = isSpanned
        }
    }

    val rows: Int = if (cells.isEmpty()) 0 else cells.maxOf { it.row + it.rowSpan }
    val columns: Int = if (cells.isEmpty()) 0 else cells.maxOf { it.column + it.colSpan }

    companion object {
        fun fromCells(cells: List<TableCell>): TableData =
            TableData(cells = cells.sortedWith(spannedLastThenByPosition))

        private val spannedLastThenByPosition =
            compareBy<TableCell>(
                { it.rowSpan > 1 && it.colSpan > 1 },
                { it.rowSpan > 1 || it.colSpan > 1 },
                { it.row },
                { it.column },
            )
    }
}

data class TableCell(
    val row: Int,
    val rowSpan: Int,
    val column: Int,
    val colSpan: Int,
)

fun LinearTable.toTableData(): TableData =
    TableData.fromCells(
        cells =
            cells
                .asSequence()
                .filterNot { (_, cell) -> cell.isFiller }
                .map { (coord, cell) ->
                    TableCell(
                        row = coord.row,
                        column = coord.col,
                        colSpan = if (cell.colSpan == 0) colCount - coord.col else cell.colSpan,
                        rowSpan = if (cell.rowSpan == 0) rowCount - coord.row else cell.rowSpan,
                    )
                }.toList(),
    )
