package com.example.treeapp.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class TreeNode(
    @PrimaryKey(autoGenerate = true)
    var id : Long,
    @Ignore
    var name : String,
    var parentId : Long?,
    var leftChildId : Long?,
    var rightChildId : Long?
) {
    constructor(parent: Long?) : this(0, "", parent, null, null)
    constructor() : this(0, "", null, null, null)

}
