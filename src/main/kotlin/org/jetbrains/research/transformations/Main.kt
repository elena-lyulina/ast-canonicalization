package org.jetbrains.research.transformations

import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import java.io.File
import org.jetbrains.research.transformations.Transformations.*

fun setupParser() {
    //copy parser file into /tmp directory, required by Gumtree
    val sourcePath :String = "src/main/resources/pythonparser"
    val targetPath :String = "/tmp/pythonparser"
    val pythonparserFile :File = File(sourcePath)

    try {
        val targetFile :File = File(targetPath)
        pythonparserFile.copyTo(targetFile)
        val command = arrayOf("chmod", "+x", targetFile.getAbsolutePath())
        val b = ProcessBuilder(*command)
        b.directory(targetFile.parentFile)
        b.start()
    } catch (e: kotlin.io.FileAlreadyExistsException) {
        println("Pythonparser file already exists in ${targetPath} directory")
    }

    // add pythonparser's path into system path
    System.setProperty("gt.pp.path", targetPath)
}

fun printTreeFromRoot(tree : ITree) {
    for (node in tree.children) {
        println("${node.type}, ${node.label}, ${node.getMetadata(node.label)}")
        printTreeFromRoot(node)
    }
}


fun main() {
    val srcFile = "./src/main/resources/test_1.py"

    setupParser()

    val tree_cont : TreeContext = PythonTreeGenerator().generateFromFile(srcFile)

    val trans :Transformations = Transformations()
    trans.anonymize(tree_cont.root, true)
    print(tree_cont)
}