package top.azimkin.multiMessageBridge.test.utilities

import net.kyori.adventure.text.Component
import top.azimkin.multiMessageBridge.utilities.Translator
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslatorTest {

    @Test
    fun translateTest() {
        Translator.translations = mapOf(
            "first.key" to "firstKeyFr arg1: %1\$s; arg2: %2\$s",
            "second.key" to "secondKeyFr",
            "arg.first" to "argFirstFr",
            "arg.second" to "argSecondFr %1\$s",
            "arg.second.arg.first" to "argSecondArgSecondFr",
        )
        val res = Translator.translate(
            Component.translatable("first.key").args(
                Component.translatable("arg.first"),
                Component.translatable("arg.second")
                    .args(Component.translatable("arg.second.arg.first"))
            )
        )
        assertEquals(
            "firstKeyFr arg1: %1\$s; arg2: %2\$s".format(
                "argFirstFr",
                "argSecondFr %1\$s".format("argSecondArgSecondFr")
            ), res
        )
    }
}