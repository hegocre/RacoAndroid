package com.yara.raco.data.subject

import androidx.room.*
import org.json.JSONArray
import org.json.JSONException

@Entity(tableName = "Subjects", indices = [Index(value = ["id"], unique = true)])
data class Subject (
    @PrimaryKey
    var id: String,
    @ColumnInfo(name = "url")
    var url: String,
    @ColumnInfo(name = "guia")
    var guia: String,
    @ColumnInfo(name = "grup")
    var grup: String,
    @ColumnInfo(name = "sigles")
    var sigles: String,
    @ColumnInfo(name = "codi_upc")
    var codi_upc: Int,
    @ColumnInfo(name = "semestre")
    var semestre: String?,
    @ColumnInfo(name = "credits")
    var credits: Int,
    @ColumnInfo(name = "vigent")
    var vigent: Boolean,
    @ColumnInfo(name = "name")
    var name: String) {

    companion object {
        /**
         * Create a list of subjects from a JSON object.
         *
         * @param data The JSON object to parse.
         * @return A list of the parsed subjects.
         */
        fun listFromJson(data: String): List<Subject> {
            val subjects = ArrayList<Subject>()

            val array = try {
                JSONArray(data)
            } catch (ex: JSONException) {
                JSONArray()
            }

            for ( i in 0 until array.length()) {
                val subject = array.getJSONObject(i)

                val id = subject.getString("id")
                val url = subject.getString("url")
                val guia = subject.getString("guia")
                val grup = subject.getString("grup")
                val sigles = subject.getString("sigles")
                val codi_upc = subject.getInt("codi_upc")
                val semestre = try {
                    subject.getString("semestre")
                } catch (ex: JSONException) {
                    ""
                }
                val credits = subject.getInt("credits")
                val vigent = subject.getBoolean("vigent")
                val name = subject.getString("name")

                subjects.add(Subject(id, url, guia, grup, sigles, codi_upc, semestre, credits, vigent, name))
            }
            return subjects
        }
    }
}
