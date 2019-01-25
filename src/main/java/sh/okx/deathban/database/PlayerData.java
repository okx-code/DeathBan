package sh.okx.deathban.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PlayerData {
  private final UUID uuid;
  private Timestamp ban;
  private int deaths = 0;
  private int bans = 0;
}
