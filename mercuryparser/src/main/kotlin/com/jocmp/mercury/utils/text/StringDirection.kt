package com.jocmp.mercury.utils.text

// Inspect a string and decide whether it reads left-to-right or right-to-left.
// Mercury delegates to the `string-direction` npm package; we do a minimal
// Unicode bidirectional check covering the cases that matter for article text.
fun getDirection(input: String?): String {
    if (input.isNullOrEmpty()) return "ltr"
    for (c in input) {
        when (Character.getDirectionality(c)) {
            Character.DIRECTIONALITY_RIGHT_TO_LEFT,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE,
            -> return "rtl"

            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
            Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE,
            -> return "ltr"

            else -> Unit
        }
    }
    return "ltr"
}
