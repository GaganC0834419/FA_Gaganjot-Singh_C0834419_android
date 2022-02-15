package com.project.finalexam.db.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.project.finalexam.db.entities.AddExpense;

import java.util.List;

/**
 * Created by admin on 07/03/2019.
 */
@Dao
public interface AddExpenseDao {

    @Query("SELECT * FROM AddExpense")
    List<AddExpense> getAll();

    @Insert
    void insert(AddExpense addExpense);

    @Delete
    void delete(AddExpense addExpense);

    @Update
    void update(AddExpense addExpense);



}
