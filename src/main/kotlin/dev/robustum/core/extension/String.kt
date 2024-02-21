package dev.robustum.core.extension

fun formatFormula(formulas: Iterable<String>): String = formatFormula(formulas.associateWith { 1 })

fun formatFormula(map: Map<String, Int>): String {
    val builder = StringBuilder()
    for ((formula: String, weight: Int) in map) {
        builder.append(formula)
        // 値が1の場合はパス
        if (weight == 1) continue
        // 化学式の下付き数字の桁数調整
        val subscript1: Char = '\u2080' + (weight % 10)
        val subscript10: Char = '\u2080' + (weight / 10)
        // 2桁目が0でない場合，下付き数字を2桁にする
        builder.append(
            StringBuilder().run {
                if (subscript10 != '\u2080') this.append(subscript10)
                this.append(subscript1)
            },
        )
    }
    return builder.toString()
}
