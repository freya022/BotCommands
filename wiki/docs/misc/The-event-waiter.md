# Using the event waiter

You might have situations where you want to wait for any JDA event to occur, 
    for this you *could* use an event waiter, 
    even if sometimes a state machine is better, 
    in cases when you need to wait for multiple events sequentially

No setup is required to use the event waiter, just use the static method `EventWaiter#of`

## Example - Simon says
The following example uses most of what you'll need:

```java
@Description("Do what Simon says")
public class SlashWaiterTest extends TextCommand {
    @JDATextCommand(name = "simon")
    public void execute(CommandEvent event) {
        event.reply("Simon says: say `hi` in less than 5 seconds").queue(m -> {
            EventWaiter.of(GuildMessageReceivedEvent.class) //Listen to guild messages
                //Check for the same channel
                .addPrecondition(e -> e.getChannel().equals(event.getChannel()))
                
                //You can add as many preconditions,
                // this would avoid doing combinations of booleans in 1 predicate
                
                //Check for same author, with message "hi"
                .addPrecondition(e -> e.getAuthor().equals(event.getAuthor()) && e.getMessage().getContentRaw().equals("hi"))
                
                //Expire on 5 seconds
                .setTimeout(5, TimeUnit.SECONDS)
                
                //After event waiter has expired, send a timeout message
                .setOnTimeout(() -> m.reply("Timeout !").queue())
                
                //After the preconditions have been fulfilled, reply
                .setOnSuccess(e -> e.getMessage().reply("n i c e").queue())
                
                //Wait for the event (non-blocking)
                .submit();
        });
    }
}
```