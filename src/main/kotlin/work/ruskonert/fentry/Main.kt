package work.ruskonert.fentry

/*
  Ah Young Oh ahyoung.oh@stonybrook.edu all rights reservered.
  Recreated by Ruskonert (ruskonert@gmail.com)
*/
object Main {
    private var spacesLeft: Int = 0
    private var board: Array<CharArray>? = null

    // ===================== Additional descriptions ============================
    // The implementing the algorithm of automatic detecting draw
    // The stored array that is conditional of victory.
    // The meaning of number is location, That is Following:
    // - - - - -
    // 1 | 2 | 3
    // - + - + -
    // 4 | 5 | 6
    // - + - + -
    // 7 | 8 | 9
    // - - - - -
    private val conditionOfVictory = arrayOf(intArrayOf(1, 2, 3), intArrayOf(4, 5, 6), intArrayOf(7, 8, 9), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), intArrayOf(3, 6, 9), intArrayOf(1, 5, 9), intArrayOf(3, 5, 7))

    private val isBoardFull: Boolean
        get() = spacesLeft == 0

    // Detect the draw even the value is not fully.
    // Check the player have the conditional of victory with investigating the board.
    // It sames draw the game if no have it even one.
    // shapeOfPlayer is char, which is shape of player.
    private fun containsConditionalOfVictory(shapeOfPlayer: Char): Boolean {
        for (i in board!!.indices) {
            for (j in 0..2) {
                // Get the shape from 2d array (that is board)
                var shape = board!![i][j]

                //If the board position is empty, it is assumed that the player can fill.
                if (shape == ' ') shape = shapeOfPlayer

                // Replace the position of the two-dimensional array with the index number.
                // Converted for more efficient calculations.
                val index = getAreaNumber(i, j)

                // Gets a collection of conditions that can be defeated.
                val condition = getConditional(index)
                for (ints in condition) {
                    // If the array is empty (no more conditions to win)
                    if (ints[0] == 0) break

                    // If the player have a condition to win
                    if (duplicates(ints, shape)) return true
                }
            }
        }
        // If reached here, there are no more conditions to win.
        return false
    }

    //
    // Make sure that the index coordinates in the arr array all look the same.
    // If there is another shape, it means that it is blocked by another player.
    private fun duplicates(arr: IntArray, shareOfPlayer: Char): Boolean {
        for (value in arr) {
            val c = board!![(value - 1) / board!!.size][(value - 1) % 3]
            if (c == ' ') continue
            if (c != shareOfPlayer)
                return false
        }
        return true
    }

    // Get the coditional of victory included index number.
    // If the {1,4,7} is can be condition of victory and index is 1, include it.
    // Please refer to `conditionOfVictory` if you want to know these it.
    private fun getConditional(index: Int): Array<IntArray> {
        var lastOf = 0
        val victoryArr = Array(conditionOfVictory.size) { IntArray(3) }
        for (ints in conditionOfVictory) {
            for (j in 0..2) {
                if (index == ints[j]) {
                    victoryArr[lastOf] = ints
                    lastOf++
                    break
                }
            }
        }
        return victoryArr
    }

    // Convert 2D array's location to index number.
    // For example:
    // board[0][2] => 3
    // board[0][0] => 1
    private fun getAreaNumber(x: Int, y: Int): Int {
        var indexOf = 1
        for (i in board!!.indices) {
            for (j in 0 until board!![i].size) {
                if (i == x && j == y)
                    return indexOf
                else
                    indexOf++
            }
        }
        return -1
    }
    // ===================== End additional descriptions ============================

    @JvmStatic
    fun main(args: Array<String>) {

        println("Welcome to Tic Tac Toe")
        initializeBoard()
        firstDraw()
        var mark = 'X'
        while (true) {
            val square = getLegalMove(mark)
            move(square, mark)
            draw()
            if (is3InRow(mark)) {
                println("$mark wins!")
                break
            }

            if (isBoardFull) {
                println("Tie game!")
                break
            }

            // ===================== Additional descriptions ============================
            if (!containsConditionalOfVictory(mark)) {
                println("No longer more victory condition!")
                break
            }
            // ===================== End additional descriptions ============================

            mark = if (mark == 'X') {
                'O'
            } else {
                'X'
            }
        }
    }

    private fun getLegalMove(mark: Char): Int {
        val console = java.util.Scanner(System.`in`)
        while (true) {
            println("$mark's next move: ")
            val square = console.nextInt()
            if (square in 1..9 &&
                    isSquareEmpty(square)) {
                return square
            }
            println("\nIllegal move, try again\n")
        }
    }

    private fun initializeBoard() {
        spacesLeft = 9
        board = Array(3) { CharArray(3) }
        for (i in board!!.indices) {
            for (j in board!!.indices) {
                board!![i][j] = ' '
            }
        }
        /* s1 = s2 = s3 = s4 = s5 = ' ';
      s6 = s7 = s8 = s9 = ' ';     */
    }

    private fun firstDraw() {
        println()
        println("   |   |   ")
        println(" " + 1 + " | " + 2 + " | " + 3)
        println("   |   |   ")
        println("---+---+---")
        println("   |   |   ")
        println(" " + 4 + " | " + 5 + " | " + 6)
        println("   |   |   ")
        println("---+---+---")
        println("   |   |   ")
        println(" " + 7 + " | " + 8 + " | " + 9)
        println("   |   |   ")
        println()
    }

    private fun draw() {
        println()
        println("   |   |   ")
        println(" " + board!![0][0] + " | "
                + board!![0][1] + " | " + board!![0][2])
        println("   |   |   ")
        println("---+---+---")
        println("   |   |   ")
        println(" " + board!![1][0] + " | "
                + board!![1][1] + " | " + board!![1][2])
        println("   |   |   ")
        println("---+---+---")
        println("   |   |   ")
        println(" " + board!![2][0] + " | "
                + board!![2][1] + " | " + board!![2][2])
        println("   |   |   ")
        println()
    }

    private fun move(square: Int, mark: Char) {
        if (isSquareEmpty(square)) {
            spacesLeft -= 1
        }

        when (square) {
            1 -> board!![0][0] = mark
            2 -> board!![0][1] = mark
            3 -> board!![0][2] = mark
            4 -> board!![1][0] = mark
            5 -> board!![1][1] = mark
            6 -> board!![1][2] = mark
            7 -> board!![2][0] = mark
            8 -> board!![2][1] = mark
            9 -> board!![2][2] = mark
        }
    }

    private fun isSquareEmpty(square: Int): Boolean {
        return when (square) {
            1 -> board!![0][0] == ' '
            2 -> board!![0][1] == ' '
            3 -> board!![0][2] == ' '
            4 -> board!![1][0] == ' '
            5 -> board!![1][1] == ' '
            6 -> board!![1][2] == ' '
            7 -> board!![2][0] == ' '
            8 -> board!![2][1] == ' '
            9 -> board!![2][2] == ' '
            else -> false
        }
    }

    private fun is3InRow(mark: Char): Boolean {
        return board!![0][0] == mark && board!![0][1] == mark && board!![0][2] == mark ||
                board!![1][0] == mark && board!![1][1] == mark && board!![1][2] == mark ||
                board!![2][0] == mark && board!![2][1] == mark && board!![2][2] == mark ||
                board!![0][0] == mark && board!![1][0] == mark && board!![2][0] == mark ||
                board!![0][1] == mark && board!![1][1] == mark && board!![2][1] == mark ||
                board!![0][2] == mark && board!![1][2] == mark && board!![2][2] == mark ||
                board!![0][0] == mark && board!![1][1] == mark && board!![2][2] == mark ||
                board!![0][2] == mark && board!![1][1] == mark && board!![2][0] == mark
    }

}