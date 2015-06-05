package bukkitTesting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitTesting
  extends JavaPlugin
  implements Listener
{
  Logger myPluginLogger = Bukkit.getLogger();
  @SuppressWarnings({ "unchecked", "rawtypes" })
Map<String, String> map = new HashMap();
  @SuppressWarnings({ "unchecked", "rawtypes" })
Map<String, Integer> creditsMap = new HashMap();
  
  public void onEnable()
  {
    this.myPluginLogger.info("Loading BukkitTesting...");
    getServer().getPluginManager().registerEvents(this, this);
  }
  
  public void onDisable()
  {
    this.myPluginLogger.info("BukkitTesting has been disabled.");
    //Bukkit.broadcastMessage(ChatColor.GREEN + "The server is reloading.");
  }
  
  @SuppressWarnings("deprecation")
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
	Player p = (Player)sender;
	
	String yt = ChatColor.GOLD + "[From: ytorgonak] " + ChatColor.GRAY;
	String ww = ChatColor.BLUE + "//";
	String gray = ChatColor.GRAY + "";
	String white = ChatColor.WHITE + "";
	String green = ChatColor.GREEN + "";
	String it = ChatColor.ITALIC + "";
	String gold = ChatColor.GOLD + "";
	
	if(commandLabel.equalsIgnoreCase("sharpshootingace") || commandLabel.equalsIgnoreCase("sharp")){
		if(args.length == 0){
			
			p.sendMessage(gold + "---- SharpshootingAce Plugin 1.0.0 ----");
			p.sendMessage(gold + "[\\]" + gray + " This is the menu for SharpPlug. The command are as follows:");
			p.sendMessage(gold + "[\\] " + green + "/sharp enchant " + gray + "List the enchantments for an item");
			p.sendMessage(gold + "[\\] " + green + "/sharp info " + gray + "View your information");
			
		}
		if(args.length == 1){
			
			if(args[0].equalsIgnoreCase("enchant")){
				if(p.getItemInHand().getEnchantments().size() > 0){
					p.sendMessage(gold + "---- SharpPlug Enchantment Lookup ---- ");
					p.sendMessage(gold + "[\\] " + green + "Item: " + white + p.getItemInHand().getType());
					p.sendMessage(gold + "[\\] " + green + "Enchantment: " + white + p.getItemInHand().getEnchantments().toString());
				}else{
					p.sendMessage(gold + "[\\] " + "SharpPlug Error Report: " + ChatColor.RED + "1 Error(s) detected!");
					p.sendMessage(gold + "[\\] Code: " + gray + "ItemNotEnchanted=true");
					p.sendMessage(gold + "[\\] English: " + gray + "That item is not enchanted!");
				}
			}
			if(args[0].equalsIgnoreCase("info")){
				
				p.sendMessage(gold + "---- SharpPlug Player Lookup ----");
				p.sendMessage(gold + "[\\] " + green + "Username: " + gray + p.getDisplayName());
				p.sendMessage(gold + "[\\] " + green + "Health: " + gray + p.getHealth());
				p.sendMessage(gold + "[\\] " + green + "Experience: " + gray + p.getExp());
			}
		}	
	}
	if(commandLabel.equalsIgnoreCase("ytorgonak") || commandLabel.equalsIgnoreCase("ytor"))
	{
		if(args.length == 0){
			
			p.sendMessage(ChatColor.BLUE + "---- " + ChatColor.GRAY + "ytorgonakPlugin 2.0" + ChatColor.BLUE + " ----");
			p.sendMessage(ww + ChatColor.GRAY + " This is the menu for the ytorgonak plugin - Version 2.0");
			p.sendMessage(ww + ChatColor.GRAY + " /ytor id - Let ytorgonak identify your item");
			p.sendMessage(ww + ChatColor.GRAY + " /ytor loc - Let ytorgonak give you your location");
			
		}
		if(args.length == 1){
			if(args[0].equalsIgnoreCase("id")){
				
				
				
				p.sendMessage(yt + "That's a(n) " + p.getItemInHand().getType() + " - " + p.getItemInHand().getAmount() + " of 'em!");
				
			}
			
			if(args[0].equalsIgnoreCase("loc")){
				
				int x = p.getLocation().getBlockX();
				int y = p.getLocation().getBlockY();
				int z = p.getLocation().getBlockZ();
				
				p.sendMessage(yt + "Hey! You're located at " + x + "x, " + y + "y, " + z + "z!");

			}
			
		}
		
	}
	
	if (commandLabel.equalsIgnoreCase("report") || commandLabel.equalsIgnoreCase("re")){
		if(args.length == 0){
			p.sendMessage(gold + "[ArcaneReport] " + white + "Coming soon!");
		}
		
		if(args.length == 1) {
			
		}
	}
	
	
	if (commandLabel.equalsIgnoreCase("arcane") || commandLabel.equalsIgnoreCase("arcanesurvival") || commandLabel.equalsIgnoreCase("arc")){
		if (args.length == 0){ 
			p.sendMessage(ChatColor.GOLD + "[ArcaneSurvival] " + ChatColor.WHITE + "Version 1.0.2");
		}
		
		String yellow = ChatColor.YELLOW + "";
		
		if (args.length == 1) {
			
			if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
				
				p.sendMessage(gold + "------- Arcane Survival --------");
		  		p.sendMessage(gold + "> " + yellow + "/spawn " + white + "- Return to the spawn");
		  		p.sendMessage(gold + "> " + yellow + "/home " + white + "- Takes you to your home");
		  		p.sendMessage(gold + "> " + yellow + "/pvp " + white + "- Toggle PvP combat");
		  		p.sendMessage(gold + "> " + yellow + "/help lwc " + white + "- Chest protection help");
		  		p.sendMessage(gold + "> " + yellow + "/help ignore " + white + "- Ignore help");
		  		p.sendMessage(gold + "> " + yellow + "/seen " + white + "- Seen commands");	
				
			}
			
		} // if args.length == 1 
	
	// Message
	
    int i;
    if ((commandLabel.equalsIgnoreCase("m")) || (commandLabel.equals("msg")))
    {
      //Player r = Bukkit.getServer().getPlayer(args[0]);
    	
      if (args.length == 0)
      {
    	  
        p.sendMessage(ChatColor.GOLD + "[ArcaneMessage]" + ChatColor.WHITE + " Usage: /msg <player> <message>");
        return true;
     }
      
      Player r = Bukkit.getServer().getPlayer(args[0]);
      
      if (args.length == 1)
    	  
      {
        p.sendMessage(ChatColor.GOLD + "[ArcaneMessage]" + ChatColor.WHITE + " You must enter a message!");
      }
      
      else if (r != null)
    	  
      {
        String message = "";
        for (i = 1; i < args.length; i++) {
          message = message + args[i] + " ";
        }
        p.sendMessage(ChatColor.GOLD + "[To: " + r.getName() + "] " + ChatColor.GRAY + message);
        r.sendMessage(ChatColor.GOLD + "[From: " + p.getName() + "] " + ChatColor.GRAY + message);
        
        // Log to console
        this.myPluginLogger.info("[" + p.getName() + "]" + " -> " + 
                r.getName() + " " + message);
        
        //String it = ChatColor.ITALIC + "";
        
     // PM spy (permission = arcane.pm)
        Bukkit.broadcast(ChatColor.RED + "PM // " + ChatColor.GRAY + it + "[ " + ChatColor.GRAY + it + p.getName() + " -> "
        + r.getName() + ": " + message + "]", "arcane.pm");
        
        this.map.put(r.getName(), p.getName());
      }
      
      else
      {
        p.sendMessage(ChatColor.GOLD + "[ArcaneMessage]" + ChatColor.WHITE + " That user is not online!");
      }
    }
    Player target;
    if (((commandLabel.equals("r")) || (commandLabel.equals("reply"))) && 
      ((sender instanceof Player)))
    {
      Player player = (Player)sender;
      if (args.length == 0)
      {
        player.sendMessage(ChatColor.GOLD + "[ArcaneMessage]" + ChatColor.WHITE + " Usage: /reply <message>");
        return true;
      }
      String message = "";
      for (int i1 = 0; i1 < args.length; i1++) {
        message = message + args[i1] + " ";
      }
      target = getServer().getPlayer(
        (String)this.map.get(player.getName()));
      if (target == null)
      {
    	  
        player.sendMessage(ChatColor.GOLD + "[ArcaneMessage]" + ChatColor.WHITE + " No player found.");
        return true;
        
      }else if (target != null){
    	  
        target.sendMessage(ChatColor.GOLD + "[From: " + 
          player.getName() + "] " + ChatColor.GRAY + 
          message);
        
        player.sendMessage(ChatColor.GOLD + "[To: " + 
          target.getName() + "] " + ChatColor.GRAY + 
          message);
        
        // Log to console
        this.myPluginLogger.info("[" + player.getName() + "]" + " -> " + 
          target.getName() + " " + message);
        
        //String it = ChatColor.ITALIC + "";
        
        // PM spy (permission = arcane.pm)
        Bukkit.broadcast(ChatColor.RED + "PM // " + ChatColor.GRAY + it + "[ " + ChatColor.GRAY + it + player.getName() + " -> "
        + target.getName() + ": " + message + "]", "arcane.pm");
        
        this.map.put(target.getName(), player.getName());
        

    }
	return true;
	}
	return true;
	}
	return false;
  }


  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e)
  {
    Player player = e.getPlayer();
    if (!player.hasPlayedBefore())
    {
      Location playerLoc = player.getLocation();
      playerLoc.setX(290.49D);
      playerLoc.setY(65.0D);
      playerLoc.setZ(50.64D);
      playerLoc.setPitch(0.0F);
      playerLoc.setYaw(0.0F);
      
      player.teleport(playerLoc);
      
      Bukkit.broadcastMessage(
        ChatColor.YELLOW + player.getName() + 
        " has joined Arcane for the first time.");
    }
  }
  
  public void logToFile(String message, String fileName)
  {
    try
    {
      File dataFolder = getDataFolder();
      if (!dataFolder.exists()) {
        dataFolder.mkdir();
      }
      File saveTo = new File(getDataFolder(), fileName + ".txt");
      if (!saveTo.exists()) {
        saveTo.createNewFile();
      }
      FileWriter fw = new FileWriter(saveTo, true);
      
      PrintWriter pw = new PrintWriter(fw);
      
      pw.println(message);
      
      pw.flush();
      
      pw.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  
  @EventHandler
  public void onBlockBreak(BlockBreakEvent e)
  {
    if ((e.getBlock().getType() == Material.DIAMOND_ORE) && 
      (e.getBlock().getY() <= 20))
    {
      Player player = e.getPlayer();
      Date date = new Date();
      
      /*
      
      Bukkit.broadcast(ChatColor.GOLD + "[Alert] " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + 
        " mined diamond ore.", "bukkit.broadcast.admin");
      
      */
      
      logToFile("[" + date.toString() + "] " + player.getName() + 
        " mined diamond ore at " + e.getBlock().getX() + ", " + 
        e.getBlock().getY() + ", " + e.getBlock().getZ() + ".", 
        "xraylog");
      
    }

    
  }


  @EventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
  
  {
	  
    Player player = e.getPlayer();
    
    if (
    	
    	e.getMessage().startsWith("/op")|| 
    	e.getMessage().startsWith("/pl")|| 
    	e.getMessage().startsWith("/game")|| 
    	e.getMessage().startsWith("/reload")||
    	e.getMessage().startsWith("/?")||
    	e.getMessage().startsWith("/give")||
    	e.getMessage().startsWith("/stop")||
    	e.getMessage().startsWith("/hack")
    
	
    		){
    	
    	Bukkit.broadcast(ChatColor.RED + "Alert // " + ChatColor.WHITE + player.getName() +
    		" attempted command \"" + ChatColor.RED + e.getMessage() + ChatColor.WHITE + 
    		"\".", "bukkit.broadcast.admin");
    	
    	//player.sendMessage("WE DID IT");
    		
    	}
    
    if (e.getMessage().startsWith("/tell")){
    	
    	player.sendMessage(ChatColor.GOLD + "[ArcaneMessage] " + ChatColor.WHITE + "You should use /msg instead!");
    	
    }
    
    if(e.getMessage().startsWith("/") & !player.hasPermission("arcane.admin") & (!(e.getMessage() == ("/msg")) ||
    																				e.getMessage() == ("/r") ||
    																				e.getMessage() == ("/reply") ||
    																				e.getMessage() == ("/m") ||
    																				e.getMessage() == ("/a")
    																				
    																				)){
    	
    	Bukkit.broadcast(ChatColor.RED + "Alert // " + ChatColor.WHITE + player.getName() +
    		" attempted command \"" + ChatColor.RED + e.getMessage() + ChatColor.WHITE + 
    		"\".", "arcane.command");
    }
    
    
    }
  
	  
  }
  
  //if msg starts with /, and the player does not have perm arcane.admin, and msg is not /msg or /r etc
  
  
  

  
  
  
