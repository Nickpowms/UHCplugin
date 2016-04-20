package me.nickpowns.uhc;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class main extends JavaPlugin implements Listener {
	// Player roles
	ArrayList<String> Ingame = new ArrayList<>();
	ArrayList<String> Indagame = new ArrayList<>();
	ArrayList<String> Spectator = new ArrayList<>();
	// Player is already in a team!
	ArrayList<String> Inteam = new ArrayList<>();

	// Declare the teams
	ArrayList<String> Blue = new ArrayList<>();
	ArrayList<String> Red = new ArrayList<>();
	ArrayList<String> Green = new ArrayList<>();
	ArrayList<String> Yellow = new ArrayList<>();
	ArrayList<String> Purple = new ArrayList<>();
	ArrayList<String> Black = new ArrayList<>();

	// Arena is not set yet, + game is not started yet.
	// That's why both int's has been set to 0
	// 0 is false
	// 1 is true
	int started = 0;
	int ArenaSet = 0;

	// Size of the border (distance from 0,0 to border)
	double bordersize = getConfig().getInt("bordersize");
	// Height of the border
	double borderheight = getConfig().getInt("borderheight");
	// Time till dm in seconds converted to ticks
	int timetilldm = getConfig().getInt("timetilldm") * 20;
	// Disable Notch apples
	int disablenotch = getConfig().getInt("disablenotch");

	// Amount of players that can join a team
	int teamSize = getConfig().getInt("teamsize");

	public void onEnable() {
		getConfig().options().copyDefaults(true);
	    saveConfig();
		// Register the events + send message to console when done.
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new signs(), this);
		Bukkit.getServer().getLogger()
				.info(ChatColor.GREEN + "UHC is enabled!");
		this.setupRecipes();
	}
	
	public void setupRecipes() {
        ItemStack goldenHead = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta gMeta = goldenHead.getItemMeta();
        gMeta.setDisplayName((Object)ChatColor.GOLD + "Golden Head");
        gMeta.setLore(Arrays.asList("You've crafted a Golden Head!", "Consuming this will grant you even greater effects", "than a normal Golden Apple!", "It regens 4 hearts instead of 2!"));
        goldenHead.setItemMeta(gMeta);
        ShapedRecipe goldenHeadRecipe = new ShapedRecipe(goldenHead);
        goldenHeadRecipe.shape(new String[]{"@@@", "@#@", "@@@"});
        goldenHeadRecipe.setIngredient('@', Material.GOLD_INGOT);
        goldenHeadRecipe.setIngredient('#', Material.SKULL_ITEM);
        Bukkit.getServer().addRecipe((Recipe)goldenHeadRecipe);
        
        ItemStack speedHead = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta sMeta = speedHead.getItemMeta();
        sMeta.setDisplayName((Object)ChatColor.AQUA + "Speed Head");
        sMeta.setLore(Arrays.asList("You've crafted a Golden Head!", "Consuming this will grant you even greater effects", "than a normal Golden Apple!", "Beside the normal effects", "it Gives you speed for 8 seconds"));
        speedHead.setItemMeta(sMeta);
        ShapedRecipe speedHeadRecipe = new ShapedRecipe(speedHead);
        speedHeadRecipe.shape(new String[]{"@@@", "@#@", "@@@"});
        speedHeadRecipe.setIngredient('@', Material.FEATHER);
        speedHeadRecipe.setIngredient('#', Material.SKULL_ITEM);
        Bukkit.getServer().addRecipe((Recipe)speedHeadRecipe);
    }
	
	@SuppressWarnings("deprecation")
	@EventHandler
    public void craftItem(PrepareItemCraftEvent e) {
        Material itemType = e.getRecipe().getResult().getType();
        Byte itemData = e.getRecipe().getResult().getData().getData();
        if (disablenotch == 1) {
	        if((itemType==Material.GOLDEN_APPLE&&itemData==1)) {
	            e.getInventory().setResult(new ItemStack(Material.AIR));
	        }
        }
        else {
        	return;
        }
    }
	
	@EventHandler
	public void onConsume(PlayerItemConsumeEvent e) {        
        if (e.getItem().getType().equals(Material.GOLDEN_APPLE)) {
            Player p = e.getPlayer();
        	if (e.getItem().getItemMeta().getDisplayName().contains("Head")) {
        		if (e.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
		            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 180, 1));
        		}
        		else if (e.getItem().getItemMeta().getDisplayName().contains("Speed Head")) {
		            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 180, 1));
        		}
        	}
        }
    }

	@EventHandler
	public void onPlayerRegensHealth(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			if (started == 1) {
				if(e.getRegainReason() == RegainReason.SATIATED || e.getRegainReason() == RegainReason.REGEN) {
		            e.setCancelled(true);
				}
				return;
			} else {
				return;
			}
		} else {
			return;
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = (Player) e.getPlayer();
		this.Ingame.add(p.getName());
		if (started == 0) {
			e.setJoinMessage(ChatColor.GRAY + "[" + ChatColor.RED
					+ "UHC" + ChatColor.GRAY + "]" + ChatColor.GRAY + " "
					+ p.getName() + " has joined the " + ChatColor.RED + "UHC"
					+ ChatColor.GRAY + "!");
			if (this.Ingame.contains(p.getName()) && started == 0) {
				World w = Bukkit.getServer().getWorld("world");
				p.teleport(new Location(w, 0, 202, 0));
				return;
			} else if (this.Ingame.contains(p.getName()) && started == 1) {
				return;
			}
		} else {
			return;
		}
		return;
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = (Player) e.getPlayer();
		e.setQuitMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
				+ ChatColor.GRAY + "]" + ChatColor.GRAY + " " + p.getName()
				+ " left :(");
		return;
	}
	
	@EventHandler
	public void onPlayerHitsPlayer(EntityDamageByEntityEvent e){
		if (started == 0) {
			if (e.getDamager() instanceof Player) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerDies(PlayerDeathEvent e) {
		Player p = (Player) e.getEntity().getPlayer();

		if (started == 1) {

			if (e.getEntity() instanceof Player) {
				if (this.Indagame.contains(e.getEntity().getPlayer().getName())) {
					e.setDeathMessage("");
					this.Spectator.add(p.getName());

					ItemStack playerhead = new ItemStack(Material.SKULL_ITEM, 1);
					ItemMeta playerheadmeta = playerhead.getItemMeta();
					playerheadmeta.setDisplayName(ChatColor.RED + p.getName()
							+ "'s head");
					playerhead.setItemMeta(playerheadmeta);
					if (p.getKiller() instanceof Player
							&& p.getKiller() != null) {
						Bukkit.broadcastMessage(ChatColor.GRAY + "["
								+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
								+ ChatColor.RED + p.getName() + ChatColor.GRAY
								+ " was killed by" + ChatColor.RED
								+ p.getKiller().getName());
					} else {
						Bukkit.broadcastMessage(ChatColor.GRAY + "["
								+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
								+ ChatColor.RED + p.getName() + ChatColor.GRAY
								+ " died!");
					}
					this.Indagame.remove(p.getName());
					e.getDrops().add(playerhead);

					if (Indagame.size() == 1) {
						String Winner = ChatColor.RED + Indagame.get(0) + ChatColor.GRAY;
						String CheckWinner = Indagame.get(0);
						if (this.Red.contains(CheckWinner)) {
						Bukkit.broadcastMessage(ChatColor.GRAY + "["
								+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
								+ Winner + " is the last man standing!");
						Bukkit.broadcastMessage(ChatColor.GRAY + "["
								+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
								+ "Team " + ChatColor.RED 
								+ "RED" + ChatColor.GRAY + " has won the UHC!");
						}
						
						else if (this.Blue.contains(CheckWinner)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ Winner + " is the last man standing!");
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.BLUE 
									+ "BLUE" + ChatColor.GRAY + " has won the UHC!");
						}
						
						else if (this.Green.contains(CheckWinner)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ Winner + " is the last man standing!");
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.GREEN 
									+ "GREEN" + ChatColor.GRAY + " has won the UHC!");
						}
						
						else if (this.Purple.contains(CheckWinner)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ Winner + " is the last man standing!");
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.DARK_PURPLE 
									+ "PURPLE" + ChatColor.GRAY + " has won the UHC!");
						}
						
						else if (this.Black.contains(CheckWinner)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ Winner + " is the last man standing!");
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.BLACK 
									+ "BLACK" + ChatColor.GRAY + " has won the UHC!");
						}
						
						else if (this.Yellow.contains(CheckWinner)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ Winner + " is the last man standing!");
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.YELLOW 
									+ "YELLOW" + ChatColor.GRAY + " has won the UHC!");
						}
						
						Player[] players = Bukkit.getOnlinePlayers();
						for (Player player : players) {
							World w = Bukkit.getServer().getWorld("world");
							this.Ingame.add(player.getName());
							player.getInventory().clear();
							player.teleport(new Location(w, 0, 202, 0));
						}
						this.Indagame.clear();
						this.Inteam.clear();
						this.Red.clear();
						this.Blue.clear();
						this.Green.clear();
						this.Yellow.clear();
						this.Purple.clear();
						this.Black.clear();
						this.Spectator.clear();
						started = 0;
						return;
					}
					
					else if (Indagame.size() == 2) {
						String p1 = Indagame.get(0);
						String p2 = Indagame.get(0);
						if (this.Red.contains(p1) && this.Red.contains(p2)) {
						Bukkit.broadcastMessage(ChatColor.GRAY + "["
								+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
								+ "Team " + ChatColor.RED 
								+ "RED" + ChatColor.GRAY + " has won the UHC!");
						}
						else if (this.Blue.contains(p1) && this.Blue.contains(p2)) {
						Bukkit.broadcastMessage(ChatColor.GRAY + "["
								+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
								+ "Team " + ChatColor.BLUE 
								+ "BLUE" + ChatColor.GRAY + " has won the UHC!");
						}
						else if (this.Green.contains(p1) && this.Green.contains(p2)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.GREEN 
									+ "GREEN" + ChatColor.GRAY + " has won the UHC!");
							}
						else if (this.Purple.contains(p1) && this.Purple.contains(p2)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.DARK_PURPLE 
									+ "PURPLE" + ChatColor.GRAY + " has won the UHC!");
							}
						else if (this.Black.contains(p1) && this.Black.contains(p2)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.BLACK 
									+ "BLACK" + ChatColor.GRAY + " has won the UHC!");
							}
						else if (this.Yellow.contains(p1) && this.Yellow.contains(p2)) {
							Bukkit.broadcastMessage(ChatColor.GRAY + "["
									+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
									+ "Team " + ChatColor.YELLOW 
									+ "YELLOW" + ChatColor.GRAY + " has won the UHC!");
							}
						Player[] players = Bukkit.getOnlinePlayers();
						for (Player player : players) {
							World w = Bukkit.getServer().getWorld("world");
							this.Ingame.add(player.getName());
							player.getInventory().clear();
							player.teleport(new Location(w, 0, 202, 0));
						}
						this.Indagame.clear();
						this.Inteam.clear();
						this.Red.clear();
						this.Blue.clear();
						this.Green.clear();
						this.Yellow.clear();
						this.Purple.clear();
						this.Black.clear();
						this.Spectator.clear();
						started = 0;
						return;
					}
				}
			}
			return;
		} else {
			return;
		}
	}

	@EventHandler
	public void onPlayerRespawns(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		World w = Bukkit.getServer().getWorld("world");
		if (this.Spectator.contains(p.getName())) {
			this.Indagame.remove(p.getName());
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					p.teleport(new Location(w, 0, 202, 0));
				}
			}, 20L);
			p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
					+ ChatColor.GRAY + "]" + ChatColor.GRAY
					+ " You died, quit the server when you are ready!");
			return;
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					p.teleport(new Location(w, 0, 201, 0));
				}
			}, 20L);
			return;
		}
	}

	@EventHandler
	public void onPlayerBreaksBlock(BlockBreakEvent e) {
		Player p = (Player) e.getPlayer();
		if (this.Ingame.contains(p.getName())) {
			if (!(p.isOp())) {
				e.setCancelled(true);
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "]" + ChatColor.GRAY
						+ "You cannot break blocks which belong to the lobby!");
				return;
			}
		} else {
			return;
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "This command is for players only!");
			return true;
		}
		Player p = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("arrays")) {
			if (args.length == 0) {
				if (this.Inteam.contains(p.getName())) {
					p.sendMessage("inteam");
				}
				if (this.Ingame.contains(p.getName())) {
					p.sendMessage("ingame");
				}
				if (this.Indagame.contains(p.getName())) {
					p.sendMessage("indagame");
				}
				if (this.Spectator.contains(p.getName())) {
					p.sendMessage("spectator");
				}
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("uhccreate")) {
			World w = Bukkit.getServer().getWorld("world");
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "]" + " Game already started!");
				return true;
			}
			if (ArenaSet == 1) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "]"
						+ " Arena has already been created!");
				return true;
			}
			if (!sender.hasPermission("uhc.create")) {
				p.sendMessage(ChatColor.RED
						+ "You dont have the permission to perform this command!");
				return true;
			}
			if (args.length == 0) {

				// CREATE THE BORDER
				// I IS LENGTH OF BORDER
				// F IS HEIGHT OF BORDER
				for (double i = 0; i < bordersize * 2; i++) {
					for (double f = 0; f < borderheight; f++) {
						new Location(w, bordersize, 0, -(bordersize))
								.add(0, f, i).getBlock()
								.setType(Material.BEDROCK);
						new Location(w, -(bordersize), 0, -(bordersize))
								.add(0, f, i).getBlock()
								.setType(Material.BEDROCK);
						new Location(w, -(bordersize), 0, -(bordersize))
								.add(i, f, 0).getBlock()
								.setType(Material.BEDROCK);
						new Location(w, -(bordersize), 0, bordersize)
								.add(i, f, 0).getBlock()
								.setType(Material.BEDROCK);
					}
				}

				// CREATE THE LOBBY

				// FLOOR OF LOBBY
				for (double g = 0; g < 20; g++) {
					for (double h = 0; h < 20; h++) {
						new Location(w, -10, 200, -10).add(h, 0, g).getBlock()
								.setType(Material.GLASS);

						Player[] players = Bukkit.getOnlinePlayers();
						for (Player player : players) {
							player.teleport(new Location(w, 0, 202, 0));
						}
					}
				}
				
				// ROOF OF LOBBY
				for (double g = 0; g < 20; g++) {
					for (double h = 0; h < 20; h++) {
						new Location(w, -10, 207, -10).add(h, 0, g).getBlock()
								.setType(Material.GLASS);

						Player[] players = Bukkit.getOnlinePlayers();
						for (Player player : players) {
							player.teleport(new Location(w, 0, 202, 0));
						}
					}
				}

				// WALLS OF LOBBY
				for (double j = 0; j < 20; j++) {
					for (double k = 0; k < 7; k++) {
						new Location(w, 10, 200, -10).add(0, k, j).getBlock()
								.setType(Material.GLASS);
						new Location(w, -10, 200, -10).add(0, k, j).getBlock()
								.setType(Material.GLASS);
						new Location(w, -10, 200, -10).add(j, k, 0).getBlock()
								.setType(Material.GLASS);
						new Location(w, -10, 200, 10).add(j, k, 0).getBlock()
								.setType(Material.GLASS);
					}
				}
				ArenaSet = 1;
				Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED
						+ "UHC" + ChatColor.GRAY + "] Arena has been created!");
				return true;
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("ghead")) {
			if (args.length == 0) {
				ItemStack goldenHead = new ItemStack(Material.GOLDEN_APPLE);
		        ItemMeta gMeta = goldenHead.getItemMeta();
		        gMeta.setDisplayName((Object)ChatColor.GOLD + "Golden Head");
		        gMeta.setLore(Arrays.asList("You've crafted a Golden Head!", "Consuming this will grant you even greater effects", "than a normal Golden Apple!", "It regens 4 hearts instead of 2!" ));
		        goldenHead.setItemMeta(gMeta);
				
		        for (int i = 0; i <= 63; i++) {
		        	p.getInventory().addItem(goldenHead);
		        }
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("shead")) {
			if (args.length == 0) {
				ItemStack speedHead = new ItemStack(Material.GOLDEN_APPLE);
		        ItemMeta sMeta = speedHead.getItemMeta();
		        sMeta.setDisplayName((Object)ChatColor.AQUA + "Speed Head");
		        sMeta.setLore(Arrays.asList("You've crafted a Golden Head!", "Consuming this will grant you even greater effects", "than a normal Golden Apple!", "Beside the normal effects", "it Gives you speed for 8 seconds"));
		        speedHead.setItemMeta(sMeta);
				
		        for (int i = 0; i <= 63; i++) {
		        	p.getInventory().addItem(speedHead);
		        }
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("uhcclear")) {
			if (!sender.hasPermission("uhc.clear")) {
				p.sendMessage(ChatColor.RED
						+ "You dont have the permission to perform this command!");
				return true;
			}
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "]" + " Game already started!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "]" + " No arena found to clear!");
				return true;
			}
			if (args.length == 0) {

				World w = Bukkit.getServer().getWorld("world");

				// CLEAR THE ARENA BY SETTING ALL THE BLOCKS OF THE BORDER
				// AND LOBBY TO Material.AIR

				// BORDER
				for (double i = 0; i < bordersize * 2; i++) {
					for (double f = 0; f < borderheight; f++) {
						new Location(w, bordersize, 0, -(bordersize))
								.add(0, f, i).getBlock().setType(Material.AIR);
						new Location(w, -(bordersize), 0, -(bordersize))
								.add(0, f, i).getBlock().setType(Material.AIR);
						new Location(w, -(bordersize), 0, -(bordersize))
								.add(i, f, 0).getBlock().setType(Material.AIR);
						new Location(w, -(bordersize), 0, bordersize)
								.add(i, f, 0).getBlock().setType(Material.AIR);
					}
				}
				
				Player[] players = Bukkit.getOnlinePlayers();
				for (Player player : players) {
					int xcleared = 0;
					int zcleared = 0;
					int ycleared = player.getWorld().getHighestBlockYAt(xcleared,zcleared);
					player.teleport(new Location(w, xcleared, ycleared, zcleared));
				}
				
				// ROOF OF LOBBY
				for (double g = 0; g < 20; g++) {
					for (double h = 0; h < 20; h++) {
						new Location(w, -10, 207, -10).add(h, 0, g).getBlock()
								.setType(Material.AIR);
					}
				}

				// FLOOR OF LOBBY
				for (double g = 0; g < 20; g++) {
					for (double h = 0; h < 20; h++) {
						new Location(w, -10, 200, -10).add(h, 0, g).getBlock()
								.setType(Material.AIR);
					}
				}

				// WALLS OF LOBBY
				for (double j = 0; j < 20; j++) {
					for (double k = 0; k < 7; k++) {
						new Location(w, 10, 200, -10).add(0, k, j).getBlock()
								.setType(Material.AIR);
						new Location(w, -10, 200, -10).add(0, k, j).getBlock()
								.setType(Material.AIR);
						new Location(w, -10, 200, -10).add(j, k, 0).getBlock()
								.setType(Material.AIR);
						new Location(w, -10, 200, 10).add(j, k, 0).getBlock()
								.setType(Material.AIR);
					}
				}
				ArenaSet = 0;
				Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED
						+ "UHC" + ChatColor.GRAY + "] Arena has been removed!");
				return true;
			}
		}

		// STARTING AND STOPPING THE GAME
		if (cmd.getName().equalsIgnoreCase("uhcstart")) {
			if (!sender.hasPermission("uhc.start")) {
				p.sendMessage(ChatColor.RED
						+ "You dont have the permission to start the game!");
				return true;
			}
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "] The game already started!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "] You havent created an arena yet!");
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "] Create an arena by typing "
						+ ChatColor.RED + "/uhccreate" + ChatColor.GRAY + " !");
				return true;
			}
			if (Green.size() == teamSize && Red.size() == teamSize
					&& Green.size() == teamSize && Yellow.size() == teamSize
					&& Purple.size() == teamSize && Black.size() == teamSize) {
				if (args.length == 0) {
					Player[] players = Bukkit.getOnlinePlayers();

					for (Player player : players) {
						if (this.Blue.contains(player.getName())) {
							int xblue = (int) (Math.random() * bordersize);
							int zblue = (int) (Math.random() * bordersize);
							int yblue = player.getWorld().getHighestBlockYAt(
									xblue, zblue);

							player.teleport(new Location(player.getWorld(),
									xblue, yblue, zblue));
						} else if (this.Red.contains(player.getName())) {
							int xred = (int) (Math.random() * bordersize);
							int zred = (int) (Math.random() * bordersize);
							int yred = player.getWorld().getHighestBlockYAt(
									xred, zred);

							player.teleport(new Location(player.getWorld(),
									xred, yred, zred));
						} else if (this.Green.contains(player.getName())) {
							int xgreen = (int) (Math.random() * bordersize);
							int zgreen = (int) (Math.random() * bordersize);
							int ygreen = player.getWorld().getHighestBlockYAt(
									xgreen, zgreen);

							player.teleport(new Location(player.getWorld(),
									xgreen, ygreen, zgreen));
						}
						//
						else if (this.Yellow.contains(player.getName())) {
							int xyellow = (int) (Math.random() * bordersize);
							int zyellow = (int) (Math.random() * bordersize);
							int yyellow = player.getWorld().getHighestBlockYAt(
									xyellow, zyellow);

							player.teleport(new Location(player.getWorld(),
									xyellow, yyellow, zyellow));
						} else if (this.Purple.contains(player.getName())) {
							int xpurple = (int) (Math.random() * bordersize);
							int zpurple = (int) (Math.random() * bordersize);
							int ypurple = player.getWorld().getHighestBlockYAt(
									xpurple, zpurple);

							player.teleport(new Location(player.getWorld(),
									xpurple, ypurple, zpurple));
						} else if (this.Black.contains(player.getName())) {
							int xblack = (int) (Math.random() * bordersize);
							int zblack = (int) (Math.random() * bordersize);
							int yblack = player.getWorld().getHighestBlockYAt(
									xblack, zblack);

							player.teleport(new Location(player.getWorld(),
									xblack, yblack, zblack));
						}
						player.addPotionEffect(new PotionEffect(
								PotionEffectType.REGENERATION, 300, 10));
						player.addPotionEffect(new PotionEffect(
								PotionEffectType.FIRE_RESISTANCE, 300, 10));
						this.Indagame.add(player.getName());
						this.Ingame.remove(player.getName());
						Bukkit.broadcastMessage(ChatColor.GRAY + "["
								+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
								+ player.getName() + " has been teleported!");
					}
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY
							+ "] The UHC has started! glhf " + ChatColor.RED
							+ "<3");
					started = 1;
					return true;
				}
			} else {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "] Not every team is full yet!");
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("uhcforcestart")) {
			if (!sender.hasPermission("uhc.start")) {
				p.sendMessage(ChatColor.RED
						+ "You dont have the permission to start the game!");
				return true;
			}
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "] The game already started!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "] You havent created an arena yet!");
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "] Create an arena by typing "
						+ ChatColor.RED + "/uhccreate" + ChatColor.GRAY + " !");
				return true;
			}
			if (args.length == 0) {
				Player[] players = Bukkit.getOnlinePlayers();

				for (Player player : players) {
					if (this.Blue.contains(player.getName())) {
						int xblue = (int) (bordersize - 50);
						int zblue = (int) (-(bordersize) + 50);
						int yblue = player.getWorld().getHighestBlockYAt(xblue,
								zblue);
						Location bluespawn = new Location(player.getWorld(),
								xblue, yblue, zblue);
						player.teleport(bluespawn);
					} else if (this.Red.contains(player.getName())) {
						int xred = (int) (bordersize - 50);
						int zred = (int) (0);
						int yred = player.getWorld().getHighestBlockYAt(xred,
								zred);

						player.teleport(new Location(player.getWorld(), xred,
								yred, zred));
					} else if (this.Green.contains(player.getName())) {
						int xgreen = (int) (-(bordersize) + 50);
						int zgreen = (int) (bordersize - 50);
						int ygreen = player.getWorld().getHighestBlockYAt(
								xgreen, zgreen);

						player.teleport(new Location(player.getWorld(), xgreen,
								ygreen, zgreen));
					} else if (this.Yellow.contains(player.getName())) {
						int xyellow = (int) (-(bordersize) + 50);
						int zyellow = (int) (bordersize - 50);
						int yyellow = player.getWorld().getHighestBlockYAt(
								xyellow, zyellow);

						player.teleport(new Location(player.getWorld(),
								xyellow, yyellow, zyellow));
					} else if (this.Purple.contains(player.getName())) {
						int xpurple = (int) (-(bordersize) + 50);
						int zpurple = (int) (0);
						int ypurple = player.getWorld().getHighestBlockYAt(
								xpurple, zpurple);

						player.teleport(new Location(player.getWorld(),
								xpurple, ypurple, zpurple));
					} else if (this.Black.contains(player.getName())) {
						int xblack = (int) (bordersize - 50);
						int zblack = (int) (-(bordersize) + 50);
						int yblack = player.getWorld().getHighestBlockYAt(
								xblack, zblack);

						player.teleport(new Location(player.getWorld(), xblack,
								yblack, zblack));
					}
					player.addPotionEffect(new PotionEffect(
							PotionEffectType.REGENERATION, 300, 10));
					player.addPotionEffect(new PotionEffect(
							PotionEffectType.FIRE_RESISTANCE, 300, 10));
					this.Indagame.add(player.getName());
					this.Ingame.remove(player.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ player.getName() + " has been teleported!");
				}
				Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED
						+ "UHC" + ChatColor.GRAY
						+ "] The UHC has started! glhf " + ChatColor.RED + "<3");
				started = 1;
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("uhcabort")) {
			if (started == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY + "] Game hasn't started yet!");
				return true;
			}
			if (!sender.hasPermission("uhc.abort")) {
				p.sendMessage(ChatColor.RED
						+ "You dont have the permission to abort the game!");
				return true;
			}
			if (args.length == 0) {
				Player[] players = Bukkit.getOnlinePlayers();

				for (Player player : players) {
					World w = Bukkit.getServer().getWorld("world");
					this.Ingame.add(player.getName());
					player.teleport(new Location(w, 0, 202, 0));
				}
				started = 0;
				Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED
						+ "UHC" + ChatColor.GRAY
						+ "] Game has been aborted by an OP!");
				this.Indagame.clear();
				this.Inteam.clear();
				this.Red.clear();
				this.Blue.clear();
				this.Green.clear();
				this.Yellow.clear();
				this.Purple.clear();
				this.Black.clear();
				this.Spectator.clear();
				return true;
			}
		}

		// TEAMS MANAGEMENT
		if (cmd.getName().equalsIgnoreCase("joingreen")) {
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.RED
						+ "UHC"
						+ ChatColor.GRAY
						+ "] The game already started, you cant join teams anymore!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY
						+ "] No arena has been set yet, ask an OP to do this.");
				return true;
			}
			if (args.length == 0) {
				if (Green.size() == teamSize) {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] This team is already full!");
					return true;
				}
				if (!(this.Inteam.contains(p.getName()))) {
					this.Green.add(p.getName());
					this.Inteam.add(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " joined the " + ChatColor.GREEN
							+ " GREEN" + ChatColor.GRAY + " team!");
					return true;
				} else {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] You are already in a team.");
				}
			}
		}

		if (cmd.getName().equalsIgnoreCase("joinblue")) {
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.RED
						+ "UHC"
						+ ChatColor.GRAY
						+ "] The game already started, you cant join teams anymore!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY
						+ "] No arena has been set yet, ask an OP to do this.");
				return true;
			}
			if (args.length == 0) {
				if (Green.size() == teamSize) {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] This team is already full!");
					return true;
				}
				if (!(this.Inteam.contains(p.getName()))) {
					this.Blue.add(p.getName());
					this.Inteam.add(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " joined the " + ChatColor.BLUE
							+ " BLUE" + ChatColor.GRAY + " team!");
					return true;
				} else {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] You are already in a team.");
				}
			}
		}

		if (cmd.getName().equalsIgnoreCase("joinred")) {
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.RED
						+ "UHC"
						+ ChatColor.GRAY
						+ "] The game already started, you cant join teams anymore!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY
						+ "] No arena has been set yet, ask an OP to do this.");
				return true;
			}
			if (args.length == 0) {
				if (Green.size() == teamSize) {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] This team is already full!");
					return true;
				}
				if (!(this.Inteam.contains(p.getName()))) {
					this.Red.add(p.getName());
					this.Inteam.add(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " joined the " + ChatColor.RED
							+ " RED" + ChatColor.GRAY + " team!");
					return true;
				} else {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] You are already in a team.");
				}
			}
		}

		if (cmd.getName().equalsIgnoreCase("joinyellow")) {
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.RED
						+ "UHC"
						+ ChatColor.GRAY
						+ "] The game already started, you cant join teams anymore!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY
						+ "] No arena has been set yet, ask an OP to do this.");
				return true;
			}
			if (args.length == 0) {
				if (Green.size() == teamSize) {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] This team is already full!");
					return true;
				}
				if (!(this.Inteam.contains(p.getName()))) {
					this.Yellow.add(p.getName());
					this.Inteam.add(p.getName());
					p.setDisplayName(ChatColor.YELLOW + p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " joined the " + ChatColor.YELLOW
							+ " YELLOW" + ChatColor.GRAY + " team!");
					return true;
				} else {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] You are already in a team.");
				}
			}
		}

		if (cmd.getName().equalsIgnoreCase("joinpurple")) {
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.RED
						+ "UHC"
						+ ChatColor.GRAY
						+ "] The game already started, you cant join teams anymore!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY
						+ "] No arena has been set yet, ask an OP to do this.");
				return true;
			}
			if (args.length == 0) {
				if (Green.size() == teamSize) {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] This team is already full!");
					return true;
				}
				if (!(this.Inteam.contains(p.getName()))) {
					this.Purple.add(p.getName());
					this.Inteam.add(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " joined the "
							+ ChatColor.DARK_PURPLE + " PURPLE"
							+ ChatColor.GRAY + " team!");
					return true;
				} else {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] You are already in a team.");
				}
			}
		}

		if (cmd.getName().equalsIgnoreCase("joinblack")) {
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.RED
						+ "UHC"
						+ ChatColor.GRAY
						+ "] The game already started, you cant join teams anymore!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY
						+ "] No arena has been set yet, ask an OP to do this.");
				return true;
			}
			if (args.length == 0) {
				if (Green.size() == teamSize) {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] This team is already full!");
					return true;
				}
				if (!(this.Inteam.contains(p.getName()))) {
					this.Black.add(p.getName());
					this.Inteam.add(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " joined the " + ChatColor.BLACK
							+ " BLACK" + ChatColor.GRAY + " team!");
					return true;
				} else {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] You are already in a team.");
				}
			}
		}

		if (cmd.getName().equalsIgnoreCase("leaveteam")) {
			if (started == 1) {
				p.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.RED
						+ "UHC"
						+ ChatColor.GRAY
						+ "] The game already started, you cant leave teams anymore!");
				return true;
			}
			if (ArenaSet == 0) {
				p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
						+ ChatColor.GRAY
						+ "] No arena has been set yet, ask an OP to do this.");
				return true;
			}
			if (args.length == 0) {
				if (this.Blue.contains(p.getName())) {
					this.Blue.remove(p.getName());
					this.Inteam.remove(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " left the " + ChatColor.BLUE
							+ "BLUE " + ChatColor.GRAY + "team!");
					return true;
				} else if (this.Red.contains(p.getName())) {
					this.Red.remove(p.getName());
					this.Inteam.remove(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " left the " + ChatColor.RED
							+ "RED " + ChatColor.GRAY + "team!");
					return true;
				} else if (this.Green.contains(p.getName())) {
					this.Green.remove(p.getName());
					this.Inteam.remove(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " left the " + ChatColor.GREEN
							+ "GREEN " + ChatColor.GRAY + "team!");
					return true;
				} else if (this.Yellow.contains(p.getName())) {
					this.Yellow.remove(p.getName());
					this.Inteam.remove(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " left the " + ChatColor.YELLOW
							+ "YELLOW " + ChatColor.GRAY + "team!");
					return true;
				} else if (this.Purple.contains(p.getName())) {
					this.Purple.remove(p.getName());
					this.Inteam.remove(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " left the "
							+ ChatColor.DARK_PURPLE + "PURPLE "
							+ ChatColor.GRAY + "team!");
					return true;
				} else if (this.Black.contains(p.getName())) {
					this.Black.remove(p.getName());
					this.Inteam.remove(p.getName());
					Bukkit.broadcastMessage(ChatColor.GRAY + "["
							+ ChatColor.RED + "UHC" + ChatColor.GRAY + "] "
							+ p.getName() + " left the " + ChatColor.BLACK
							+ "BLACK " + ChatColor.GRAY + "team!");
					return true;
				} else if (!(this.Inteam.contains(p.getName()))) {
					p.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "UHC"
							+ ChatColor.GRAY + "] You are not in a team yet!");
					return true;
				}
			}
		}
		return true;
	}

}
