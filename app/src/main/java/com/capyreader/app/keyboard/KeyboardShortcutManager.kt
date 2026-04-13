package com.capyreader.app.keyboard

import com.jocmp.capy.preferences.Preference
import com.jocmp.capy.preferences.getAndSet

class KeyboardShortcutManager(
    private val overridesPreference: Preference<ShortcutOverrides>,
) {
    fun resolve(keyCode: Int, meta: Int): ShortcutAction? {
        val key = ShortcutKey(keyCode = keyCode, meta = meta)
        val reverseLookup = buildReverseLookup()
        return reverseLookup[key]
    }

    fun effectiveBindings(): Map<ShortcutAction, List<ShortcutKey>> {
        val overrides = overridesPreference.get().bindings
        return ShortcutAction.entries.associateWith { action ->
            overrides[action] ?: action.defaultKeys
        }
    }

    fun updateBinding(action: ShortcutAction, keys: List<ShortcutKey>) {
        overridesPreference.getAndSet { current ->
            current.copy(bindings = current.bindings + (action to keys))
        }
    }

    fun resetBinding(action: ShortcutAction) {
        overridesPreference.getAndSet { current ->
            current.copy(bindings = current.bindings - action)
        }
    }

    fun resetAll() {
        overridesPreference.set(ShortcutOverrides())
    }

    fun findConflict(key: ShortcutKey, excludeAction: ShortcutAction): ShortcutAction? {
        return effectiveBindings()
            .filterKeys { it != excludeAction }
            .entries
            .firstOrNull { (_, keys) -> key in keys }
            ?.key
    }

    private fun buildReverseLookup(): Map<ShortcutKey, ShortcutAction> {
        val bindings = effectiveBindings()
        val lookup = mutableMapOf<ShortcutKey, ShortcutAction>()
        bindings.forEach { (action, keys) ->
            keys.forEach { key ->
                lookup[key] = action
            }
        }
        return lookup
    }
}
