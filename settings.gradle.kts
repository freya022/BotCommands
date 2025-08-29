rootProject.name = "BotCommands"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":BotCommands-core")
include(
    ":BotCommands-method-accessors:core",
    ":BotCommands-method-accessors:classfile",
    ":BotCommands-method-accessors:kotlin-reflect",
)
include(":BotCommands-jda-ktx")
include(":jda-ktx-deprecation-processor")
include(":spring-properties-processor")
include(":BotCommands-spring")
