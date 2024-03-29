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

package net.elytrium.limboqueue.commands;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import java.util.List;
import net.elytrium.commons.kyori.serialization.Serializer;
import net.elytrium.limboqueue.Config;
import net.elytrium.limboqueue.LimboQueue;
import net.kyori.adventure.text.Component;

public class LimboQueueCommand implements SimpleCommand {

  private final LimboQueue plugin;
  private final Component reload;
  private final Component reloadFailed;

  public LimboQueueCommand(LimboQueue plugin) {
    this.plugin = plugin;
    Serializer serializer = LimboQueue.getSerializer();
    this.reload = serializer.deserialize(Config.IMP.MESSAGES.RELOAD);
    this.reloadFailed = serializer.deserialize(Config.IMP.MESSAGES.RELOAD_FAILED);
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    String[] args = invocation.arguments();

    if (args.length == 0) {
      return ImmutableList.of("reload");
    } else {
      return ImmutableList.of();
    }
  }

  @Override
  public void execute(Invocation invocation) {
    CommandSource source = invocation.source();
    String[] args = invocation.arguments();

    if (args.length == 1) {
      String command = args[0];
      if (command.equalsIgnoreCase("reload") && source.hasPermission("limboqueue.reload")) {
        try {
          this.plugin.reload();
          source.sendMessage(this.reload);
        } catch (Exception e) {
          e.printStackTrace();
          source.sendMessage(this.reloadFailed);
        }
      }
    }
  }
}
