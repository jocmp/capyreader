package com.jocmp.malletmath

/**
 * Builds the table atom for a `\begin{...}` environment, or for bare `&` and `\\` rows.
 */
internal object MathEnvironments {
    fun table(
        environment: String?,
        rows: MutableList<MutableList<MathList>>,
        columnSpec: List<ColumnAlignment>?,
    ): MathAtom {
        val table = MathTable(rows, environment)

        matrixDelimiters[environment]?.let { (left, right) ->
            return matrix(table, left, right, cellStyle = LineStyle.TEXT)
        }

        return when (environment) {
            null -> {
                table.interRowAdditionalSpacing = 1f
                (0 until table.numColumns).forEach { table.setAlignment(it, ColumnAlignment.LEFT) }
                table
            }

            "smallmatrix" -> matrix(table, left = "", right = "", cellStyle = LineStyle.SCRIPT)

            "aligned", "align", "align*", "split", "eqalign", "alignat", "alignat*", "alignedat" -> aligned(table)

            "gather", "gather*", "gathered", "displaylines", "equation", "equation*" -> {
                requireColumns(table, environment, max = 1)
                table.interRowAdditionalSpacing = 1f
                table.setAlignment(0, ColumnAlignment.CENTER)
                table
            }

            "eqnarray", "eqnarray*" -> {
                requireColumns(table, environment, max = 3)
                table.interRowAdditionalSpacing = 1f
                table.interColumnSpacing = 18f
                table.setAlignment(0, ColumnAlignment.RIGHT)
                table.setAlignment(1, ColumnAlignment.CENTER)
                table.setAlignment(2, ColumnAlignment.LEFT)
                table
            }

            "cases", "dcases", "rcases", "drcases" -> cases(table, environment)

            "array", "darray" -> {
                table.interColumnSpacing = 18f
                columnSpec.orEmpty().forEachIndexed { column, alignment -> table.setAlignment(column, alignment) }
                prefixCells(table) { MathStyle(LineStyle.TEXT) }
                table
            }

            else -> throw MathParseException("Unknown environment $environment")
        }
    }

    private fun matrix(table: MathTable, left: String, right: String, cellStyle: LineStyle): MathAtom {
        table.environment = "matrix"
        table.interColumnSpacing = 18f
        prefixCells(table) { MathStyle(cellStyle) }

        if (left.isEmpty() && right.isEmpty()) {
            return table
        }
        return Inner(innerList = mutableListOf(table), leftBoundary = left, rightBoundary = right)
    }

    /**
     * Columns alternate right and left aligned, with 1em between each pair, as in amsmath.
     */
    private fun aligned(table: MathTable): MathTable {
        table.interRowAdditionalSpacing = 1f
        (0 until table.numColumns).forEach { column ->
            if (column % 2 == 0) {
                table.setAlignment(column, ColumnAlignment.RIGHT)
                if (column > 0) {
                    table.columnGaps[column] = 18f
                }
            } else {
                table.setAlignment(column, ColumnAlignment.LEFT)
            }
        }

        // An empty ordinary before the right-hand column keeps relation spacing after the &.
        table.cells.forEach { row ->
            row.forEachIndexed { column, cell ->
                if (column % 2 == 1) {
                    cell.add(0, MathAtom(AtomType.ORDINARY, ""))
                }
            }
        }
        return table
    }

    private fun cases(table: MathTable, environment: String): MathAtom {
        requireColumns(table, environment, max = 2)
        table.interColumnSpacing = 18f
        table.setAlignment(0, ColumnAlignment.LEFT)
        table.setAlignment(1, ColumnAlignment.LEFT)

        val style = if (environment.startsWith("d")) LineStyle.DISPLAY else LineStyle.TEXT
        prefixCells(table) { MathStyle(style) }

        val reversed = environment.endsWith("rcases")
        return Inner(
            innerList = mutableListOf(MathSpace(3f), table),
            leftBoundary = if (reversed) "" else "{",
            rightBoundary = if (reversed) "}" else "",
        )
    }

    private fun prefixCells(table: MathTable, atom: () -> MathAtom) {
        table.cells.forEach { row ->
            row.forEach { cell -> cell.add(0, atom()) }
        }
    }

    private fun requireColumns(table: MathTable, environment: String, max: Int) {
        if (table.numColumns > max) {
            throw MathParseException("$environment allows at most $max columns")
        }
    }

    private val matrixDelimiters: Map<String?, Pair<String, String>> =
        mapOf(
            "matrix" to ("" to ""),
            "pmatrix" to ("(" to ")"),
            "bmatrix" to ("[" to "]"),
            "Bmatrix" to ("{" to "}"),
            "vmatrix" to ("|" to "|"),
            "Vmatrix" to ("‖" to "‖"),
        )
}
