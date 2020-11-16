package com.example.socialmediaappv2.data

class AVLTree {
    private var root: Node? = null

    private class Node(var key: ImageBitmap, var parent: Node?) {
        var balance: Int = 0
        var left: Node? = null
        var right: Node? = null
    }

    private fun setBalance(vararg nodes: Node) {
        for (n in nodes) n.balance = height(n.right) - height(n.left)
    }

    fun insert(key: ImageBitmap): Boolean {
        if (root == null)
            root = Node(key, null)
        else {
            var n: Node? = root
            var parent: Node
            while (true) {
                if (n!!.key == key) return false
                parent = n
                val goLeft = n.key > key
                n = if (goLeft) n.left else n.right
                if (n == null) {
                    if (goLeft)
                        parent.left = Node(key, parent)
                    else
                        parent.right = Node(key, parent)
                    rebalance(parent)
                    break
                }
            }
        }
        return true
    }

    private fun height(n: Node?): Int {
        if (n == null) return -1
        return 1 + Math.max(height(n.left), height(n.right))
    }

    private fun rotateLeft(a: Node): Node {
        val b: Node? = a.right
        b!!.parent = a.parent
        a.right = b.left
        if (a.right != null) a.right!!.parent = a
        b.left = a
        a.parent = b
        if (b.parent != null) {
            if (b.parent!!.right == a)
                b.parent!!.right = b
            else
                b.parent!!.left = b
        }
        setBalance(a, b)
        return b
    }


    private fun rotateRight(a: Node): Node {
        val b: Node? = a.left
        b!!.parent = a.parent
        a.left = b.right
        if (a.left != null) a.left!!.parent = a
        b.right = a
        a.parent = b
        if (b.parent != null) {
            if (b.parent!!.right == a)
                b.parent!!.right = b
            else
                b.parent!!.left = b
        }
        setBalance(a, b)
        return b
    }

    private fun rotateLeftThenRight(n: Node): Node {
        n.left = rotateLeft(n.left!!)
        return rotateRight(n)
    }

    private fun rotateRightThenLeft(n: Node): Node {
        n.right = rotateRight(n.right!!)
        return rotateLeft(n)
    }

    private fun rebalance(n: Node) {
        setBalance(n)
        var nn = n
        if (nn.balance == -2)
            if (height(nn.left!!.left) >= height(nn.left!!.right))
                nn = rotateRight(nn)
            else
                nn = rotateLeftThenRight(nn)
        else if (nn.balance == 2)
            if (height(nn.right!!.right) >= height(nn.right!!.left))
                nn = rotateLeft(nn)
            else
                nn = rotateRightThenLeft(nn)
        if (nn.parent != null) rebalance(nn.parent!!)
        else root = nn
    }

    fun delete(delKey: ImageBitmap) {
        if (root == null) return
        var n: Node? = root
        var parent: Node? = root
        var delNode: Node? = null
        var child: Node? = root
        while (child != null) {
            parent = n
            n = child
            child = if (delKey >= n.key) n.right else n.left
            if (delKey == n.key) delNode = n
        }
        if (delNode != null) {
            delNode.key = n!!.key
            child = if (n.left != null) n.left else n.right
            if (0 == root!!.key.compareTo(delKey)) {
                root = child

                if (null != root) {
                    root!!.parent = null
                }

            } else {
                if (parent!!.left == n)
                    parent.left = child
                else
                    parent.right = child

                if (null != child) {
                    child.parent = parent
                }

                rebalance(parent)
            }
        }
    }

}