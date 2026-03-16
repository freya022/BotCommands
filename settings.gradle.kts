rootProject.name = "BotCommands"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":test-commons")
include(":BotCommands-core")
include(
    ":BotCommands-method-accessors",
    ":BotCommands-method-accessors:core",
    ":BotCommands-method-accessors:classfile",
    ":BotCommands-method-accessors:kotlin-reflect",
)
include(":BotCommands-jda-ktx")
include(":BotCommands-spring")
include(
    ":BotCommands-typesafe-messages:core",
    ":BotCommands-typesafe-messages:bc",
    ":BotCommands-typesafe-messages:spring",
)
include(":BotCommands-restarter")
include(":test-bot")
