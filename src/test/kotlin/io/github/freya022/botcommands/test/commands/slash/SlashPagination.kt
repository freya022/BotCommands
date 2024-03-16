package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.utils.ButtonContent
import io.github.freya022.botcommands.api.components.utils.SelectContent
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.pagination.AbstractPaginationBuilder
import io.github.freya022.botcommands.api.pagination.Paginators
import io.github.freya022.botcommands.api.pagination.menu.AbstractMenu
import io.github.freya022.botcommands.api.pagination.menu.AbstractMenuBuilder
import io.github.freya022.botcommands.api.pagination.menu.Menu
import io.github.freya022.botcommands.api.pagination.menu.RowPrefixSupplier
import io.github.freya022.botcommands.api.pagination.menu.buttonized.ButtonMenu
import io.github.freya022.botcommands.api.pagination.paginator.AbstractPaginatorBuilder
import io.github.freya022.botcommands.api.pagination.paginator.Paginator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

@Command
@Dependencies(Paginators::class, Components::class)
class SlashPagination(private val paginators: Paginators, private val buttons: Buttons) : ApplicationCommand() {
    private val menuEntries = listOf("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve")

    init {
        Paginator.Defaults.firstPageButtonContent = ButtonContent.fromLabel(ButtonStyle.SECONDARY, "[Default] First")
        Paginator.Defaults.previousPageButtonContent = ButtonContent.fromLabel(ButtonStyle.SECONDARY, "[Default] Previous")
        Paginator.Defaults.nextPageButtonContent = ButtonContent.fromLabel(ButtonStyle.SECONDARY, "[Default] Next")
        Paginator.Defaults.lastPageButtonContent = ButtonContent.fromLabel(ButtonStyle.SECONDARY, "[Default] Last")
        Paginator.Defaults.deleteButtonContent = ButtonContent.fromLabel(ButtonStyle.SUCCESS, "[Default] Delete")

        Menu.Defaults.maxEntriesPerPage = 5
        Menu.Defaults.rowPrefixSupplier = RowPrefixSupplier { entryNum, _ -> "[Default] $entryNum: " }

        ButtonMenu.Defaults.reusable = true
    }

    @TopLevelSlashCommandData
    @JDASlashCommand(name = "pagination", subcommand = "paginator")
    suspend fun onSlashPaginationPaginator(event: GuildSlashEvent, @SlashOption useDefaults: Boolean) {
        val paginator = paginators
            .paginator(5) { paginator, builder, embedBuilder, page ->
                val humanPageNumber = page + 1
                builder.setContent("Content of page $humanPageNumber")
                embedBuilder.setTitle("Pagination '${paginator.javaClass.simpleName}' page $humanPageNumber / ${paginator.maxPages}")
            }
            .configurePagination(event)
            .configurePaginator(useDefaults)
            .build()

        logger.info { "Created $paginator" }

        event.reply(paginator.getInitialMessage()).setEphemeral(true).await()
    }

    @JDASlashCommand(name = "pagination", subcommand = "custom_pagination")
    suspend fun onSlashPaginationCustomPagination(event: GuildSlashEvent) {
        val customPagination = paginators
            .customPagination(50) { paginator, builder, page ->
                val humanPageNumber = page + 1
                builder.addContent("Pagination '${paginator.javaClass.simpleName}' page $humanPageNumber / ${paginator.maxPages}")
                builder.addContent("\n\nContent of page $humanPageNumber")

                // Certified java callback moment
                runBlocking {
                    builder.addActionRow(buttons.primaryButton("Random page").ephemeral {
                        constraints(paginator.constraints)
                        bindTo {
                            paginator.page = Random.nextInt(0..<paginator.maxPages)
                            it.editMessage(paginator.getCurrentMessage()).await()
                        }
                    })
                }
            }
            .configurePagination(event)
            .build()

        logger.info { "Created $customPagination" }

        event.reply(customPagination.getInitialMessage()).setEphemeral(true).await()
    }

    @JDASlashCommand(name = "pagination", subcommand = "menu")
    suspend fun onSlashPaginationMenu(event: GuildSlashEvent, @SlashOption useDefaults: Boolean) {
        val menu = paginators
            .menu(menuEntries)
            .configurePagination(event)
            .configurePaginator(useDefaults)
            .configureMenu(useDefaults)
            .build()

        logger.info { "Created $menu" }

        event.reply(menu.getInitialMessage()).setEphemeral(true).await()
    }

    @JDASlashCommand(name = "pagination", subcommand = "button_menu")
    suspend fun onSlashPaginationButtonMenu(event: GuildSlashEvent, @SlashOption useDefaults: Boolean) {
        val buttonMenu = paginators
            .buttonMenu(
                menuEntries,
                buttonContentSupplier = { item, _ -> ButtonContent.fromLabel(ButtonStyle.PRIMARY, item) },
                callback = { buttonEvent, entry ->
                    buttonEvent.reply_("You have chosen '$entry'", ephemeral = true).await()
                }
            )
            .configurePagination(event)
            .configurePaginator(useDefaults)
            .configureMenu(useDefaults)
            .apply {
                if (!useDefaults) {
                    setReusable(false)
                }
            }
            .build()

        logger.info { "Created $buttonMenu" }

        event.reply(buttonMenu.getInitialMessage()).setEphemeral(true).await()
    }

    @JDASlashCommand(name = "pagination", subcommand = "nested")
    suspend fun onSlashPaginationNestedPagination(event: GuildSlashEvent, @SlashOption useDefaults: Boolean) {
        val paginationWrapper = paginators.nestedPagination()
            .configurePagination(event)
            .configurePaginator(useDefaults)
            .addMenu(SelectContent.of("Foo"), 1) { _, _, embedBuilder, page -> embedBuilder.setTitle("Foo @ page ${page + 1}") }
            .addMenu(SelectContent.of("Bar"), 3) { _, _, embedBuilder, page -> embedBuilder.setTitle("Bar @ page ${page + 1}") }
            .addMenu(SelectContent.of("Baz"), 5) { _, _, embedBuilder, page -> embedBuilder.setTitle("Baz @ page ${page + 1}") }
            .usePaginatorControls(true)
            .build()

        logger.info { "Created $paginationWrapper" }

        event.reply(paginationWrapper.getInitialMessage()).setEphemeral(true).await()
    }

    private fun <T : AbstractPaginationBuilder<T, *>> T.configurePagination(event: IDeferrableCallback): T =
        this.setConstraints(InteractionConstraints.ofUsers(event.user))
            .setTimeout(10.seconds) { pagination ->
                logger.info { "Timeout of $pagination" }
                event.hook.deleteOriginal().await()
            }

    private fun <T : AbstractPaginatorBuilder<T, *>> T.configurePaginator(useDefaults: Boolean): T =
        if (!useDefaults) {
            this.setFirstContent(ButtonContent.fromLabel(ButtonStyle.PRIMARY, "First"))
                .setPreviousContent(ButtonContent.fromLabel(ButtonStyle.PRIMARY, "Previous"))
                .setNextContent(ButtonContent.fromLabel(ButtonStyle.PRIMARY, "Next"))
                .setLastContent(ButtonContent.fromLabel(ButtonStyle.PRIMARY, "Last"))
                .setDeleteContent(ButtonContent.fromLabel(ButtonStyle.DANGER, "Delete"))
                .useDeleteButton(true)
        } else {
            this
        }

    private fun <T : AbstractMenuBuilder<*, T, R>, R : AbstractMenu<*, R>> T.configureMenu(useDefaults: Boolean): T =
        this.setPageEditor { _, builder, _, _ -> builder.addContent("My menu") }
            .setTransformer { "$it (index: ${menuEntries.indexOf(it)})" }
            .run {
                if (!useDefaults) {
                    this.setRowPrefixSupplier { entryNum, _ -> "${entryNum + 1}: " }
                        .setMaxEntriesPerPage(7)
                } else {
                    this
                }
            }
}