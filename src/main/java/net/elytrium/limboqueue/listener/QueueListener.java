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
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.elytrium.limboqueue.LimboQueue;

public class QueueListener {

  private final LimboQueue plugin;

  public QueueListener(LimboQueue plugin) {
    this.plugin = plugin;
  }

  @Subscribe
  public void onLoginLimboRegister(LoginLimboRegisterEvent event) {
    if (!event.getPlayer().hasPermission("limboqueue.bypass") && (this.plugin.isFull || this.plugin.isOffline)) {
      event.addOnJoinCallback(() -> this.plugin.queuePlayer(event.getPlayer()));
    }
  }
}
