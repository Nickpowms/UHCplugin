package me.nickpowns.uhc;

import org.bukkit.event.Listener;

public class events implements Listener {

	public int getStarted(){
	    main main = new main();
	    return main.started;
	}
	
	public int getArenaSet(){
	    main main = new main();
	    return main.ArenaSet;
	}
	
	
	
}
