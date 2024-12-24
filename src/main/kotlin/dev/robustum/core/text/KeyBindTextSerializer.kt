package dev.robustum.core.text

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.text.KeybindText
import net.minecraft.util.Identifier

object KeyBindTextSerializer : TextSerializer<KeybindText> {
    override val id: Identifier = Identifier("keybind")
    override val codec: Codec<KeybindText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("keybind").forGetter(KeybindText::getKey),
            ).apply(instance, ::KeybindText)
    }
}
