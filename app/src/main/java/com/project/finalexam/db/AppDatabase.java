package com.project.finalexam.db;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.project.finalexam.date_converter.DateTypeConverter;
import com.project.finalexam.db.dao.AddExpenseDao;
import com.project.finalexam.db.entities.AddExpense;


/**
 * Created by admin on 05/03/2019.
 */

@Database(entities = {AddExpense.class}, version = 1,exportSchema = false)
@TypeConverters({DateTypeConverter.class})

public abstract class AppDatabase extends RoomDatabase {

    public abstract AddExpenseDao addExpenseDao();
}
