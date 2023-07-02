package com.example.treeapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.treeapp.models.AppDatabase
import com.example.treeapp.models.TreeInfo
import com.example.treeapp.models.TreeNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    private lateinit var treeInfo: TreeInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // bind buttons and node name
        val nameView = findViewById<TextView>(R.id.nodeNameText)
        val homeBtn = findViewById<Button>(R.id.homeBtn)
        val parentBtn = findViewById<Button>(R.id.parentBtn)
        val deleteNodeBtn = findViewById<Button>(R.id.deleteNodeBtn)
        val createRootBtn = findViewById<Button>(R.id.createRootBtn)
        val leftChildBtn = findViewById<Button>(R.id.leftChildBtn)
        val leftChildCreateBtn = findViewById<Button>(R.id.leftChildCreateBtn)
        val rightChildBtn = findViewById<Button>(R.id.rightChildBtn)
        val rightChildCreateBtn = findViewById<Button>(R.id.rightChildCreateBtn)

        // load tree info from preferences
        getTreeInfo(nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
            leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)

        // return to root node
        homeBtn.setOnClickListener {
            homeFun(nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                 leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
        }

        // return to parent node
        parentBtn.setOnClickListener {
            parentFun(nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
        }

        // delete current node and all its children,
        // return to parent node
        deleteNodeBtn.setOnClickListener {
            deleteNodeFun(nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
        }

        // create root node if there are no nodes in tree
        createRootBtn.setOnClickListener {
            createRootFun(nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
        }

        // go to left child node
        leftChildBtn.setOnClickListener {
            leftChildFun(nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
        }

        // go to right child node
        rightChildBtn.setOnClickListener {
            rightChildFun(nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
        }

        // create left child node
        leftChildCreateBtn.setOnClickListener {
            leftChildCreateFun(leftChildBtn, leftChildCreateBtn)
        }

        // create right child note
        rightChildCreateBtn.setOnClickListener {
            rightChildCreateFun(rightChildBtn, rightChildCreateBtn)
        }
    }

    private fun getTreeInfo(
        nameView: TextView,
        deleteNodeBtn: Button,
        createRootBtn: Button,
        leftChildBtn: Button,
        leftChildCreateBtn: Button,
        rightChildBtn: Button,
        rightChildCreateBtn: Button
    ) {
        // load db and tree dao
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()
        treeInfo = TreeInfo(db.treeDao())

        // get previous nodes from preferences
        this.lifecycleScope.launch {
            val preferences = getPreferences(MODE_PRIVATE)
            if (preferences.contains("currentNodeId") &&
                preferences.contains("rootNodeId")
            ) {
                treeInfo.currentNode =
                    getNodeFromDb(preferences.getLong("currentNodeId", 0))
                treeInfo.rootNode =
                    getNodeFromDb(preferences.getLong("rootNodeId", 0))
            }

            // setup ui
            initialSetup(treeInfo.currentNode, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
        }
    }

    override fun onPause() {
        super.onPause()

        // save current nodes to preferences
        if (treeInfo.currentNode != null && treeInfo.rootNode != null) {
            val preferences = getPreferences(MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putLong("currentNodeId", treeInfo.currentNode!!.id)
            editor.putLong("rootNodeId", treeInfo.rootNode!!.id)
            editor.apply()
        }

    }

    private fun homeFun(nameView: TextView,
                        deleteNodeBtn: Button,
                        createRootBtn: Button,
                        leftChildBtn: Button,
                        leftChildCreateBtn: Button,
                        rightChildBtn: Button,
                        rightChildCreateBtn: Button
    ) {
        if (treeInfo.currentNode == treeInfo.rootNode) {
            showToastAlreadyRoot()
        }
        else {
            treeInfo.currentNode = treeInfo.rootNode
            treeInfo.currentNode?.let {
                setupWindow(
                    it, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                    leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
            }
        }
    }


    private fun parentFun(nameView: TextView,
                          deleteNodeBtn: Button,
                          createRootBtn: Button,
                          leftChildBtn: Button,
                          leftChildCreateBtn: Button,
                          rightChildBtn: Button,
                          rightChildCreateBtn: Button
    ) {
        if (treeInfo.currentNode == treeInfo.rootNode) {
            showToastAlreadyRoot()
        }
        else {
            this.lifecycleScope.launch {
                treeInfo.currentNode = getNodeFromDb(treeInfo.currentNode?.parentId)
                treeInfo.currentNode?.let {
                    setupWindow(
                        it, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                        leftChildCreateBtn, rightChildBtn, rightChildCreateBtn
                    )
                }
            }
        }
    }

    // load node and set its name (md5 hash function)
    private suspend fun getNodeFromDb(nodeId: Long?) : TreeNode? {
        val node = nodeId?.let { treeInfo.treeDao.loadNode(it) }
        node?.name = nodeId?.let { md5(it) } ?: ""
        return node
    }


    private fun deleteNodeFun(nameView: TextView,
                              deleteNodeBtn: Button,
                              createRootBtn: Button,
                              leftChildBtn: Button,
                              leftChildCreateBtn: Button,
                              rightChildBtn: Button,
                              rightChildCreateBtn: Button
    ) {
        this.lifecycleScope.launch {
            // if it's root node clear ui
            if (treeInfo.currentNode?.id == treeInfo.rootNode?.id) {
                treeInfo.currentNode?.let { treeInfo.treeDao.deleteNode(it) }
                treeInfo.rootNode = null
                treeInfo.currentNode = null
                initialSetup(
                    null, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                    leftChildCreateBtn, rightChildBtn, rightChildCreateBtn
                )
            } else {
                // if it isn't root delete all children and go to parent node
                withContext(Dispatchers.Default) {
                    treeInfo.currentNode?.let { deleteChilds(it) }
                }
                setNodeToParent()
                treeInfo.currentNode?.let {
                    setupWindow(
                        it, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                        leftChildCreateBtn, rightChildBtn, rightChildCreateBtn
                    )
                }
            }
        }
    }


    // recursive children delete
    private suspend fun deleteChilds(treeNode: TreeNode) {
        val leftChild = treeNode.leftChildId?.let { treeInfo.treeDao.loadNode(it) }
        leftChild?.let { deleteChilds(it) }
        treeNode.leftChildId = null
        val rightChild = treeNode.rightChildId?.let { treeInfo.treeDao.loadNode(it) }
        rightChild?.let { deleteChilds(it) }
        treeNode.rightChildId = null
        treeInfo.treeDao.deleteNode(treeNode)
    }

    private suspend fun setNodeToParent() {
        val prevNodeId = treeInfo.currentNode?.id
        treeInfo.currentNode = getNodeFromDb(treeInfo.currentNode?.parentId)
        if (treeInfo.currentNode?.leftChildId == prevNodeId)
            treeInfo.currentNode?.leftChildId = null
        else
            treeInfo.currentNode?.rightChildId = null
        treeInfo.currentNode?.let { treeInfo.treeDao.updateNode(it) }
    }

    private fun createRootFun(nameView: TextView,
                              deleteNodeBtn: Button,
                              createRootBtn: Button,
                              leftChildBtn: Button,
                              leftChildCreateBtn: Button,
                              rightChildBtn: Button,
                              rightChildCreateBtn: Button
    ) {
        this.lifecycleScope.launch {
            val rootNode = TreeNode(null)
            val rootId = treeInfo.treeDao.insertNode(rootNode)
            rootNode.id = rootId
            rootNode.name = md5(rootId)
            treeInfo.rootNode = rootNode
            treeInfo.currentNode = rootNode
            setupWindow(
                rootNode, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                leftChildCreateBtn, rightChildBtn, rightChildCreateBtn
            )
        }
    }

    private fun leftChildFun(nameView: TextView,
                             deleteNodeBtn: Button,
                             createRootBtn: Button,
                             leftChildBtn: Button,
                             leftChildCreateBtn: Button,
                             rightChildBtn: Button,
                             rightChildCreateBtn: Button
    ) {
        this.lifecycleScope.launch {
            treeInfo.currentNode = getNodeFromDb(treeInfo.currentNode?.leftChildId)
            treeInfo.currentNode?.let {
                setupWindow(
                    it, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                    leftChildCreateBtn, rightChildBtn, rightChildCreateBtn
                )
            }
        }
    }

    private fun rightChildFun(nameView: TextView,
                             deleteNodeBtn: Button,
                             createRootBtn: Button,
                             leftChildBtn: Button,
                             leftChildCreateBtn: Button,
                             rightChildBtn: Button,
                             rightChildCreateBtn: Button
    ) {
        this.lifecycleScope.launch {
            treeInfo.currentNode = getNodeFromDb(treeInfo.currentNode?.rightChildId)
            treeInfo.currentNode?.let {
                setupWindow(
                    it, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                    leftChildCreateBtn, rightChildBtn, rightChildCreateBtn
                )
            }
        }
    }

    private fun leftChildCreateFun(leftChildBtn: Button, leftChildCreateBtn: Button) {
        if (treeInfo.currentNode == null)
            return
        this.lifecycleScope.launch {
            val leftChild = createChild(::setLeftChild)
            setupChildBtn(leftChild.id, leftChildBtn, leftChildCreateBtn)
        }
    }

    private fun rightChildCreateFun(rightChildBtn: Button, rightChildCreateBtn: Button) {
        if (treeInfo.currentNode == null)
            return
        this.lifecycleScope.launch {
            val rightChild = createChild(::setRightChild)
            setupChildBtn(rightChild.id, rightChildBtn, rightChildCreateBtn)
        }
    }

    private suspend fun createChild(setChild: (TreeNode?, TreeNode) -> Unit) : TreeNode {
        val currentNode = treeInfo.currentNode
        val child = TreeNode(parent = currentNode?.id)
        val childId = treeInfo.treeDao.insertNode(child)
        child.id = childId
        child.name = md5(childId)
        currentNode?.let {
            setChild(it, child)
            treeInfo.treeDao.updateNode(it)
        }
        return child
    }

    private fun setLeftChild(currentNode: TreeNode?, leftChild: TreeNode) {
        currentNode?.leftChildId = leftChild.id
    }

    private fun setRightChild(currentNode: TreeNode?, rightChild: TreeNode) {
        currentNode?.rightChildId = rightChild.id
    }

    private fun initialSetup(
        currentNode: TreeNode?,
        nameView: TextView,
        deleteNodeBtn: Button,
        createRootBtn: Button,
        leftChildBtn: Button,
        leftChildCreateBtn: Button,
        rightChildBtn: Button,
        rightChildCreateBtn: Button
    ) {
        if (currentNode == null) {
            nameView.text = getString(R.string.noNodeText)
            createRootBtn.visibility = View.VISIBLE
            deleteNodeBtn.visibility = View.GONE
            leftChildBtn.visibility = View.GONE
            leftChildCreateBtn.visibility = View.GONE
            rightChildBtn.visibility = View.GONE
            rightChildCreateBtn.visibility = View.GONE
        }
        else {
            setupWindow(currentNode, nameView, deleteNodeBtn, createRootBtn, leftChildBtn,
                leftChildCreateBtn, rightChildBtn, rightChildCreateBtn)
        }
    }

    private fun md5(input: Long): String {
        val md = MessageDigest.getInstance("MD5")
        val buffer = longToByteArray(input)
        return BigInteger(1, md.digest(buffer)).toString(16)
            .padStart(32, '0')
            .drop(12)
    }

    private fun longToByteArray(data: Long) : ByteArray {
        val buffer = ByteBuffer.allocate(Long.SIZE_BYTES)
        buffer.putLong(data)
        return buffer.array()
    }

    private fun setupWindow(
        currentNode: TreeNode,
        nameView: TextView,
        deleteNodeBtn: Button,
        createRootBtn: Button,
        leftChildBtn: Button,
        leftChildCreateBtn: Button,
        rightChildBtn: Button,
        rightChildCreateBtn: Button
    ) {
        nameView.text = getString(R.string.defaultNodeName, currentNode.name)
        createRootBtn.visibility = View.GONE
        deleteNodeBtn.visibility = View.VISIBLE
        setupChildBtn(currentNode.leftChildId, leftChildBtn, leftChildCreateBtn)
        setupChildBtn(currentNode.rightChildId, rightChildBtn, rightChildCreateBtn)

    }

    private fun setupChildBtn(childId: Long? , goBtn: Button, createBtn: Button) {
        if (childId == null) {
            goBtn.visibility = View.GONE
            createBtn.visibility = View.VISIBLE
        }
        else {
            goBtn.visibility = View.VISIBLE
            createBtn.visibility = View.GONE
        }
    }

    private fun showToastNoNode() {
        Toast
            .makeText(
                this,
                getString(R.string.noNodeText),
                Toast.LENGTH_LONG)
            .show()
    }

    private fun showToastAlreadyRoot() {
        Toast
            .makeText(
                this,
                getString(R.string.alreadyRootText),
                Toast.LENGTH_LONG)
            .show()
    }

}