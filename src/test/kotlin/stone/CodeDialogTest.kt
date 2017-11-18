package stone

import io.kotlintest.specs.StringSpec

class CodeDialogTest : StringSpec() {

    init {
        val dialog = CodeDialog()
        dialog.read()
    }

}