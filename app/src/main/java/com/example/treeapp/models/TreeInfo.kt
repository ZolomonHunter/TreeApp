package com.example.treeapp.models

data class TreeInfo(
    var rootNode: TreeNode?,
    var currentNode: TreeNode?,
    val treeDao: TreeDao
) {
    constructor(treeDao: TreeDao) : this(null, null, treeDao)
}
