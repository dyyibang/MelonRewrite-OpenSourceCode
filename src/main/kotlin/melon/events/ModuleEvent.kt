package melon.events

import dev.zenhao.melon.module.AbstractModule
import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting

sealed class ModuleEvent(val module: AbstractModule) : Event {
    class Toggle(module: AbstractModule) : ModuleEvent(module), IEventPosting by Companion {
        companion object : EventBus()
    }

    class VisibleChange(module: AbstractModule) : ModuleEvent(module),
        IEventPosting by Companion {
        companion object : EventBus()
    }
}