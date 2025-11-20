rootProject.name = "BotCommands"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":BotCommands-core")
include(
    ":BotCommands-method-accessors",
    ":BotCommands-method-accessors:core",
    ":BotCommands-method-accessors:classfile",
    ":BotCommands-method-accessors:kotlin-reflect",
)
include(":BotCommands-jda-ktx")
include(":jda-ktx-deprecation-processor")
include(":spring-properties-processor")
include(":BotCommands-spring")
include(
    ":BotCommands-typesafe-messages:core",
    ":BotCommands-typesafe-messages:bc",
    ":BotCommands-typesafe-messages:spring",
)
include(":test-bot")
