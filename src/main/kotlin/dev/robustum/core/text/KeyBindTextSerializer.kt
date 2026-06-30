package dev.robustum.core.text

import com.mojang.serialization.Codec
import dev.robustum.core.codec.RobustumCodecs
import net.minecraft.text.KeybindText
import net.minecraft.util.Identifier

object KeyBindTextSerializer : TextSerializer<KeybindText> {
    override val id: Identifier = Identifier("keybind")
    override val codec: Codec<KeybindText> = RobustumCodecs.record { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("keybind").forGetter(KeybindText::getKey),
            ).apply(instance, ::KeybindText)
    }
}
