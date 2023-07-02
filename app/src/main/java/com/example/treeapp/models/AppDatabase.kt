package com.example.treeapp.models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TreeNode::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun treeDao(): TreeDao
}