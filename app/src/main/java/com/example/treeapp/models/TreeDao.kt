package com.example.treeapp.models

import androidx.room.*

@Dao
interface TreeDao {
    @Insert
    suspend fun insertNode(node: TreeNode) : Long

    @Update
    suspend fun updateNode(node: TreeNode)

    @Delete
    suspend fun deleteNode(node: TreeNode)

    @Delete
    suspend fun deleteNodes(nodes: List<TreeNode>)

    @Query("SELECT * FROM TreeNode where id=:id")
    suspend fun loadNode(id: Long): TreeNode
}