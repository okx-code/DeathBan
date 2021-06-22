package sh.okx.deathban.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import sh.okx.deathban.database.PlayerData;

public class PlayerDeathBanEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final PlayerData data;
    private boolean cancelled;

    /**
     * Event fired when a player dies and should be banned.
     * To prevent the player involved in this event from getting banned, this event must be cancelled.
     * @param player The player involved in this event.
     * @param data The PlayerData before the player was banned.
     * @param cancelled Whether the event should be cancelled or not.
     */
    public PlayerDeathBanEvent(Player player, PlayerData data, boolean cancelled){
        super(player);
        this.player = player;
        this.data = data;
        this.cancelled = cancelled;
    }


    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public PlayerData getData() {
        return data;
    }
}
