package sh.okx.deathban.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import sh.okx.deathban.database.PlayerData;

public class PlayerDeathBanEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final PlayerData data;
    private boolean cancelled;


    public PlayerDeathBanEvent(Player player, PlayerData data, boolean cancelled){
        this.player = player;
        this.data = data;
        this.cancelled = cancelled;
    }



    @Override
    public HandlerList getHandlers() {
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

    public Player getPlayer() {
        return player;
    }

    public PlayerData getData() {
        return data;
    }
}
