package com.yara.raco.database.subject

import androidx.room.Database
import com.yara.raco.data.subject.Subject

@Database(entities = [Subject::class], version = 8, exportSchema = false)
class SubjectDatabase {

}