/*
 * Copyright (C) 2022 - 2023 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.limboqueue.listener;

import com.velocitypowered.api.event.Subscribe;
import net.elytrium.commons.kyori.serialization.Serializer;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.elytrium.limboqueue.Config;
import net.elytrium.limboqueue.LimboQueue;

public class QueueListener {

  private final LimboQueue plugin;
  private final Serializer serializer = LimboQueue.getSerializer();

  public QueueListener(LimboQueue plugin) {
    this.plugin = plugin;
  }

  @Subscribe
  public void onLoginLimboRegister(LoginLimboRegisterEvent event) {
    event.setOnKickCallback((kickEvent) -> {
      if (!kickEvent.getServer().equals(this.plugin.targetServer)) {
        return false;
      }

      if (kickEvent.getServerKickReason().isEmpty()) {
        return false;
      }

      String reason = this.serializer.serialize(kickEvent.getServerKickReason().get());
      if (reason.contains(Config.IMP.MAIN.KICK_MESSAGE)) {
        this.plugin.queuePlayer(kickEvent.getPlayer());
        return true;
      }
      return false;
    });
  }
}
