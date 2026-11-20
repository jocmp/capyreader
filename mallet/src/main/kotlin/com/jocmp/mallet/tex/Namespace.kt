package com.jocmp.mallet.tex

/**
 * Names that can be set globally or local to a nested group, with an undo stack like TeX's.
 */
class Namespace<V : Any>(
    private val builtins: Map<String, V> = emptyMap(),
    private val current: MutableMap<String, V> = mutableMapOf(),
) {
    private val undefStack = mutableListOf<MutableMap<String, V?>>()

    fun beginGroup() {
        undefStack.add(mutableMapOf())
    }

    fun endGroup() {
        if (undefStack.isEmpty()) {
            throw ParseError("Unbalanced namespace destruction: attempt to pop global namespace; please report this as a bug")
        }
        val undefs = undefStack.removeAt(undefStack.lastIndex)
        undefs.forEach { (key, value) ->
            if (value == null) {
                current.remove(key)
            } else {
                current[key] = value
            }
        }
    }

    fun endGroups() {
        while (undefStack.isNotEmpty()) {
            endGroup()
        }
    }

    fun has(name: String): Boolean = current.containsKey(name) || builtins.containsKey(name)

    fun get(name: String): V? = current[name] ?: builtins[name]

    fun set(name: String, value: V?, global: Boolean = false) {
        if (global) {
            undefStack.forEach { it.remove(name) }
            undefStack.lastOrNull()?.put(name, value)
        } else {
            val top = undefStack.lastOrNull()
            if (top != null && !top.containsKey(name)) {
                top[name] = current[name]
            }
        }
        if (value == null) {
            current.remove(name)
        } else {
            current[name] = value
        }
    }
}
