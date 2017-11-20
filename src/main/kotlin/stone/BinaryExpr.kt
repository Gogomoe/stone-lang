package stone

class BinaryExpr(list: List<ASTree>) : ASList(list) {

    val left = list[0]
    val right = list[2]

    val operator = (list[1] as ASLeaf).token.stringValue
}