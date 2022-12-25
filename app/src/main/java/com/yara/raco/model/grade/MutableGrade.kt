package com.yara.raco.model.grade

/**
 * Class that represents a grade.
 *
 * @param name Name of the grade.
 * @param weight Weight of the grade.
 * @param mark Mark of the grade.
 */
@kotlinx.serialization.Serializable
data class MutableGrade(
    var id: Int?,
    var name: String,
    var weight: String,
    var mark: String,
    var evaluationId: Int
) {
    fun toGrade() =
        Grade(
            id = id ?: 0,
            name = name,
            weight = weight.replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException(),
            mark = if (mark == "") null
            else if (mark.replace(",", ".")
                    .toDoubleOrNull() == null
            ) throw IllegalArgumentException()
            else mark.replace(",", ".").toDoubleOrNull(),
            evaluationId = evaluationId
        )
}