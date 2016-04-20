package me.nickpowns.uhc;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class signs implements Listener {
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (e.getPlayer().hasPermission("uhc.signcreate")) {
			if (e.getLine(0).equalsIgnoreCase("[green]")) {
				e.setLine(0, "");
				e.setLine(1, "Join the");
				e.setLine(2, "§2green §0team!");
				return;
			}
			else if (e.getLine(0).equalsIgnoreCase("[blue]")) {
				e.setLine(0, "");
				e.setLine(1, "Join the");
				e.setLine(2, "§1blue §0team!");
				return;
			}
			else if (e.getLine(0).equalsIgnoreCase("[red]")) {
				e.setLine(0, "");
				e.setLine(1, "Join the");
				e.setLine(2, "§4red §0team!");
				return;
			}
			else if (e.getLine(0).equalsIgnoreCase("[yellow]")) {
				e.setLine(0, "");
				e.setLine(1, "Join the");
				e.setLine(2, "§eyellow §0team!");
				return;
			}
			else if (e.getLine(0).equalsIgnoreCase("[purple]")) {
				e.setLine(0, "");
				e.setLine(1, "Join the");
				e.setLine(2, "§5purple §0team!");
				return;
			}
			else if (e.getLine(0).equalsIgnoreCase("[black]")) {
				e.setLine(0, "");
				e.setLine(1, "Join the");
				e.setLine(2, "black team!");
				return;
			}
			else if (e.getLine(0).equalsIgnoreCase("[leave]")) {
				e.setLine(0, "");
				e.setLine(1, "§cLeave §0your");
				e.setLine(2, "current team!");
				return;
			}
			else {
				return;
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if ((e.getClickedBlock().getState() instanceof Sign)) {
			Sign s = (Sign) e.getClickedBlock().getState();
			if (s.getLine(2).equalsIgnoreCase("black team!")) {
				p.performCommand("joinblack");
			}
			else if (s.getLine(2).equalsIgnoreCase("§2green §0team!")) {
				p.performCommand("joingreen");
			}
			else if (s.getLine(2).equalsIgnoreCase("§4red §0team!")) {
				p.performCommand("joinred");
			}
			else if (s.getLine(2).equalsIgnoreCase("§1blue §0team!")) {
				p.performCommand("joinblue");
			}
			else if (s.getLine(2).equalsIgnoreCase("current team!")) {
				p.performCommand("leaveteam");
			}
			else if (s.getLine(2).contains("yellow")) {
				p.performCommand("joinyellow");
			}
			else if (s.getLine(2).contains("purple")) {
				p.performCommand("joinpurple");
			}
		}
	}
}
