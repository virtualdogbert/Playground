/*
 * So here is my take on an interview problem I saw  of reversing/flipping a graph. 
 * This is the  in-line version, but I have some commented out code that could make it non    
 * destructive.  In the Graphs.pdf is a drawing of my test graph and it's flipped version. 
 * At the bottom of this file are some asserts to make sure the graph is flipped/mirrored
 * Normally I would have the asserts in a unit test, and want to spend some time to figure 
 * out a more automated way of generating the graph, but I think this should suffice for now.
 */

import groovy.transform.AutoClone

@AutoClone //will do a deep clone in this instance
class Node {
    int number
    Node rightParent  //I could see replacing these with lists of nodes if you had nodes that could have more than two parents/children
    Node leftParent
    Node rightChild
    Node leftChild
    
    boolean isLeaf(){
        !(rightChild || leftChild)
    }
    
    boolean isRoot(){
        !(rightParent || leftParent)
    }
    
    void reverseNode(){
        Node nodeTemp = rightParent
        rightParent=rightChild
        rightChild = nodeTemp
        
        nodeTemp = leftParent
        leftParent = leftChild
        leftChild =  nodeTemp
    }

    //String toString(){
   //     return number
    //}
}

class TreeRevser{
    Map hasBeenSeen = [:] //used to avoid going in loops
    List newRootNodes = [] //used to capture the new root nodes

    void reverseTree(def tree){
        if(!tree){
            return
        }

        if (tree instanceof Collection){ //Root nodes come in as a collection
            tree.each{
                reverseTree(it)
                //reverseTree(it.clone)
            }
            return
        }
        
        hasBeenSeen[tree.number] = true
           
        if (tree.isLeaf()){
            newRootNodes << tree
        } else {
            
            if(tree.leftChild &&!hasBeenSeen[tree?.leftChild?.number]){
                reverseTree(tree.leftChild)
            }

            if(tree.rightChild && !hasBeenSeen[tree?.rightChild?.number]){
                reverseTree(tree.rightChild)
            }
        }

        tree.reverseNode()
    }
}


// Setup Graph ********************************************
def nodes = [:]

(1..15).each{
    nodes[it] =  new Node(number: it)
}

nodes[1].rightChild = nodes[15]
nodes[1].leftChild = nodes[2]

nodes[2].rightChild = nodes[3]
nodes[2].rightParent = nodes[1]


nodes[3].rightChild = nodes[5]
nodes[3].leftChild = nodes[4]
nodes[3].rightParent = nodes[15]
nodes[3].leftParent =nodes[2]

nodes[4].rightParent = nodes[3]

nodes[5].rightChild = nodes[12]
nodes[5].leftChild = nodes[6]
nodes[5].leftParent =nodes[3]

nodes[6].rightChild = nodes[9]
nodes[6].leftChild = nodes[7]
nodes[6].rightParent = nodes[5]

nodes[7].leftChild = nodes[8]
nodes[7].rightParent = nodes[6]

nodes[8].rightParent = nodes[7]

nodes[9].rightChild = nodes[11]
nodes[9].leftChild = nodes[10]
nodes[9].leftParent =nodes[6]

nodes[10].rightParent = nodes[9]

nodes[11].leftParent =nodes[9]

nodes[12].rightChild = nodes[13]
nodes[12].leftParent =nodes[5]

nodes[13].rightChild = nodes[14]
nodes[13].leftParent = nodes[12]


nodes[14].leftParent = nodes[13]

nodes[15].leftChild = nodes[3]
nodes[15].leftParent = nodes[1]

def tree = [nodes[1]]
// Setup Graph ********************************************

// Reverse the graph **************************************
def teversal = new TreeRevser()
teversal.reverseTree(tree)
println teversal.newRootNodes
// Reverse the graph **************************************

// Test the reversed graph ********************************
assert nodes[4].isRoot()
assert nodes[8].isRoot()
assert nodes[10].isRoot()
assert nodes[11].isRoot()
assert nodes[14].isRoot()

assert nodes[1].leftParent == nodes[2]
assert nodes[1].rightParent == nodes[15]

assert nodes[2].rightParent == nodes[3]
assert nodes[2].rightChild == nodes[1]

assert nodes[3].rightParent == nodes[5]
assert nodes[3].leftParent== nodes[4]
assert nodes[3].rightChild == nodes[15]
assert nodes[3].leftChild ==nodes[2]

assert nodes[4].rightChild == nodes[3]

assert nodes[5].rightParent == nodes[12]
assert nodes[5].leftParent == nodes[6]
assert nodes[5].leftChild ==nodes[3]

assert nodes[6].rightParent == nodes[9]
assert nodes[6].leftParent == nodes[7]
assert nodes[6].rightChild == nodes[5]

assert nodes[7].leftParent == nodes[8]
assert nodes[7].rightChild == nodes[6]

assert nodes[8].rightChild == nodes[7]

assert nodes[9].rightParent == nodes[11]
assert nodes[9].leftParent == nodes[10]
assert nodes[9].leftChild ==nodes[6]

assert nodes[10].rightChild == nodes[9]

assert nodes[11].leftChild ==nodes[9]

assert nodes[12].rightParent == nodes[13]
assert nodes[12].leftChild==nodes[5]

assert nodes[13].rightParent == nodes[14]
assert nodes[13].leftChild == nodes[12]

assert nodes[14].leftChild== nodes[13]

assert nodes[15].leftParent == nodes[3]
assert nodes[15].leftChild == nodes[1]

//test for deep clone commenting out toString()
def n1 = new Node(number: 1)
def n2= new Node(number: 2)
def n3 = new Node(number: 3)

n1.leftChild=n2
n1.rightChild=n3

println n1
println n1.leftChild
println n1.rightChild

n4= n1.clone()

println n4
println n4.leftChild
println n4.rightChild

