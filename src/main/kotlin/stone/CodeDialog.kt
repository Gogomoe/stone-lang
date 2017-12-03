package stone

import java.awt.TextArea
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.Reader
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.UIManager

class CodeDialog : Reader() {

    private var buffer: String? = null
    private var pos: Int = 0

    override fun read(cbuf: CharArray?, off: Int, len: Int): Int {
        if (buffer == null) {
            val input = showDialog()
            if (input == null) {
                return -1
            } else {
                print(input)
                buffer = input + '\n'
                pos = 0
            }
        }
        var size = 0
        val length = buffer!!.length
        while (pos < length && size < len) {
            cbuf!![off + size++] = buffer!![pos++]
        }
        if (pos == length) {
            buffer = null
        }
        return size
    }

    private fun showDialog(): String? {
        val area = TextArea(20, 40)
        val pane = JScrollPane(area)
        val result = JOptionPane.showOptionDialog(
                null, pane, "Input",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, null, null)
        return if (result == JOptionPane.OK_OPTION) {
            area.text
        } else {
            null
        }
    }

    override fun close() {}

    companion object {

        init {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }

        fun file(): Reader {
            val chooser = JFileChooser()
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                return BufferedReader(FileReader(chooser.selectedFile))
            } else {
                throw FileNotFoundException("File not found")
            }
        }
    }
}