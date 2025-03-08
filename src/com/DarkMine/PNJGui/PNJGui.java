package com.DarkMine.PNJGui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.aufdemrand.sentry.SentryInstance;
import net.aufdemrand.sentry.SentryTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.trait.SkinLayers;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Preconditions;

public class PNJGui extends JavaPlugin implements Listener {
	
	private Map<UUID, UUID> AssociatedNPC = new HashMap<UUID, UUID>();
    public static Permission perms = null;
    public static Chat chat = null;
    public static Economy economy = null;
    public static File configf, translatef;
    public static FileConfiguration config, translate;
    public static int BuyPrice, CallPrice, SellPrice, UpgradeLevel2, UpgradeLevel3, UpgradeWeaponPriceLevel2, UpgradeWeaponPriceLevel3;
    private List<UUID> MyList = new LinkedList<UUID>();
    public static String BasicHead, BasicChestPlate, BasicBoots, BasicLeggings;
    public static String InterHead, InterChestPlate, InterBoots, InterLeggings;
    public static String AdvancedHead, AdvancedChestPlate, AdvancedBoots, AdvancedLeggings;
    public static String BasicWeapon, InterWeapon, AdvancedWeapon;
    private static String ErrorLoad, CitizensSave, UnableConsole, GuiName, Buy, Call, Sell, UpButArmorv2, UpButArmorv3, UpButWeaponv2, 
    UpButWeaponv3, SkinSet, SkinNotSet, TypeSkinName, MoneyLeft, CantBuy, NotEnoughMoney, NoBuyEconomy, SoldBody,
    BodyTeleported, BodySpawn, NoBodyguard, NoCallBody, SpawnBefore, UpArmorLvl2, UpArmorDisable, CantUpArmor, UpArmorLvl3, UpWeaponLvl2, UpWeaponLvl3, UpWeaponDisable;
    private Map<UUID,Boolean> PendingReload = new HashMap<UUID, Boolean>();

	static boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = PNJGui.getPlugin(PNJGui.class).getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
    
    private boolean createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
    		configf = new File(getDataFolder(), "config.yml");
    	    if(!configf.exists()){
    	    	configf.getParentFile().mkdirs();
    	    	copy(getResource("config.yml"), configf);
    	    }
    	    config = new YamlConfiguration();
	        try {
	            config.load(configf);
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
        } catch (Exception e) {
            e.printStackTrace();
    		return false;
    	}
    }
    
    private void PutInlist(Player player)
    {
    	if (!MyList.contains(player.getUniqueId())) {
    		MyList.add(player.getUniqueId());
    	}
    }
    
    @Override
	public void onEnable() {
		if ( ( getServer().getPluginManager().getPlugin("Citizens") == null ) || ( !getServer().getPluginManager().getPlugin("Citizens").isEnabled() ) )
	    {
	      getLogger().log(Level.SEVERE, "Citizens 2.0 not found or not enabled");
	      getServer().getPluginManager().disablePlugin(this);
	      return;
	    }
		
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
	    translatef = new File(getDataFolder(), "translate.yml");
	    if(!translatef.exists()){
	    	translatef.getParentFile().mkdirs();
	    	copy(getResource("translate.yml"), translatef);
	    }
	    translate = new YamlConfiguration();
	    try{
	    	translate.load(translatef);
	    	load();
	    } catch (Exception e){
	    	e.printStackTrace();
	    }
		  
	    if(!createConfig()){
            getLogger().severe(String.format(ErrorLoad));
            getServer().getPluginManager().disablePlugin(this);
	    }else{
	    	BuyPrice = config.getInt("Pnj.BuyPrice");
	    	CallPrice = config.getInt("Pnj.CallPrice");
	    	SellPrice = config.getInt("Pnj.SellPrice");
	    	UpgradeLevel2 = config.getInt("Pnj.UpgradeArmorPriceLevel2");
	    	UpgradeLevel3 = config.getInt("Pnj.UpgradeArmorPriceLevel3");
	    	
	    	BasicHead = config.getString("Stuff.Armor.Basic.Head");
	    	BasicChestPlate = config.getString("Stuff.Armor.Basic.ChestPlate");
	    	BasicLeggings = config.getString("Stuff.Armor.Basic.Leggings");
	    	BasicBoots = config.getString("Stuff.Armor.Basic.Boots");
	    	
	    	InterHead = config.getString("Stuff.Armor.Inter.Head");
	    	InterChestPlate = config.getString("Stuff.Armor.Inter.ChestPlate");
	    	InterLeggings = config.getString("Stuff.Armor.Inter.Leggings");
	    	InterBoots = config.getString("Stuff.Armor.Inter.Boots");
	    	
	    	AdvancedHead = config.getString("Stuff.Armor.Advanced.Head");
	    	AdvancedChestPlate = config.getString("Stuff.Armor.Advanced.ChestPlate");
	    	AdvancedLeggings = config.getString("Stuff.Armor.Advanced.Leggings");
	    	AdvancedBoots = config.getString("Stuff.Armor.Advanced.Boots");
	    	UpgradeWeaponPriceLevel2 = config.getInt("Pnj.UpgradeWeaponPriceLevel2");
	    	UpgradeWeaponPriceLevel3 = config.getInt("Pnj.UpgradeWeaponPriceLevel3");
	    	
	    	BasicWeapon = config.getString("Stuff.Weapon.Basic");
	    	InterWeapon = config.getString("Stuff.Weapon.Inter");
	    	AdvancedWeapon = config.getString("Stuff.Weapon.Advanced");
	    }
	    
    
        PluginManager pm = getServer().getPluginManager();
	    pm.registerEvents(this, this);
	    
	    File dataFolder = getDataFolder();
	    if(!dataFolder.exists())
	    {
	    	dataFolder.mkdir();
	    }
	    
	    File getData = new File("./plugins/Citizens/saves.yml");
	    if (!getData.exists())
        {
	    	try {
				getData.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	    FileConfiguration Data = YamlConfiguration.loadConfiguration(new File("./plugins/Citizens/saves.yml"));
	    int lastid = Data.getInt("last-created-npc-id");
	    for(int i = 0; i < lastid+1; i++)
	    {
	    	String path = "npc."+i;
	    	String path2 = "npc."+i+".traits.sentry.GuardTarget";
	    	if(Data.isSet(path) && Data.isSet(path2)){
	        	try {
			    	UUID myuuid = UUID.fromString(Data.getString(path+".traits.owner.uuid"));
			    	UUID npcuuid = UUID.fromString(Data.getString(path+".uuid"));
			    	AssociatedNPC.put(myuuid, npcuuid);
	            } catch (Exception e) {
	                e.printStackTrace();
	        	}
	    	}
	    }
	    if(net.citizensnpcs.api.CitizensAPI.getTraitFactory().getTrait(SentryTrait.class) == null)
		net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(SentryTrait.class));
		
	    getLogger().info("Plugin démarré !");
    }
 
    private void load() {
    	ErrorLoad = translate.getString("Loading_error");
    	CitizensSave = translate.getString("Citizens_save_message");
    	UnableConsole = translate.getString("Unable_from_console");
    	GuiName = translate.getString("Gui_name");
    	Buy = translate.getString("Buy_button");
    	Call = translate.getString("Call_button");
    	Sell = translate.getString("Sell_button");
    	UpButArmorv2 = translate.getString("Upgrade_button_armor_v2");
    	UpButArmorv3 = translate.getString("Upgrade_button_armor_v3");
    	UpButWeaponv2 = translate.getString("Upgrade_button_weapon_v2");
        UpButWeaponv3 = translate.getString("Upgrade_button_weapon_v3");
        SkinSet = translate.getString("Skin_set");
        SkinNotSet = translate.getString("Skin_not_set");
        TypeSkinName = translate.getString("Type_skin_name");
        MoneyLeft = translate.getString("Money_left");
        CantBuy = translate.getString("Cant_buy");
        NotEnoughMoney = translate.getString("Not_enough_money");
        NoBuyEconomy = translate.getString("No_buy_economy");
        BodyTeleported = translate.getString("Bodyguard_teleported");
        BodySpawn = translate.getString("Bodyguard_spawn");
        NoBodyguard = translate.getString("Not_bodyguard");
        NoCallBody = translate.getString("No_call_bodyguard");
        SoldBody = translate.getString("Sold_body");
        SpawnBefore = translate.getString("Spawn_before");
        UpArmorLvl2 = translate.getString("Upgraded_armor_lvl2");
        UpArmorDisable = translate.getString("Upgrade_armor_disable");
        CantUpArmor = translate.getString("Cant_upgrade_armor");
        UpArmorLvl3 = translate.getString("Upgraded_armor_lvl3");
        UpWeaponLvl2 = translate.getString("Upgraded_weapon_lvl2");
        UpWeaponLvl3 = translate.getString("Upgraded_weapon_lvl3");
        UpWeaponDisable = translate.getString("Upgrade_weapon_disable");
    }
    
    
    public void saveYamls() {
        try {
            translate.save(translatef);
            config.save(configf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }	
	
	public void onDisable() {
		for(Map.Entry<UUID, UUID> entries : AssociatedNPC.entrySet())
		{
			NPC npc = CitizensAPI.getNPCRegistry().getByUniqueId(AssociatedNPC.get(entries.getValue()));
			new destroyNPC(npc);
		}
		getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
		saveYamls();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (("gui".equalsIgnoreCase(cmd.getName()) || "g".equalsIgnoreCase(cmd.getName())) && args.length == 0) {
			if(sender instanceof Player)
			{
				Player p = (Player)sender;
				if(p.hasPermission("*") || p.hasPermission("gui"))
				{
					openGui(p.getPlayer());
					return true;
				}
			}
		}else if(("gui".equalsIgnoreCase(cmd.getName()) || "g".equalsIgnoreCase(cmd.getName())) && args.length == 1 && args[0].equals("reload")) {
			if(sender instanceof Player)
			{
				Player p = (Player)sender;
				if(p.hasPermission("*") || p.hasPermission("gui.reload"))
				{
					p.sendMessage(ChatColor.RED + CitizensSave);
					p.sendMessage(ChatColor.RED + "Type 'Yes' if you allready did it, or No if you don't.");
					PendingReload.put(p.getUniqueId(), true);
					return true;
				}
			}else if(sender instanceof ConsoleCommandSender){
				getLogger().info(String.format(ChatColor.RED + UnableConsole));
				return true;
			}
		}
        return false;
	}
	
	private void openGui(Player player) {
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + GuiName); //create inventory
		
		ItemStack buyguard = new ItemStack(Material.DOUBLE_PLANT); //create buy button
		ItemMeta buyguardMeta = buyguard.getItemMeta();
		buyguardMeta.setDisplayName(ChatColor.GREEN + Buy);
		buyguard.setItemMeta(buyguardMeta);
		
		ItemStack tpguard = new ItemStack(Material.LEASH); //create tp button
		ItemMeta tpguardMeta = tpguard.getItemMeta();
		tpguardMeta.setDisplayName(ChatColor.YELLOW + Call);
		tpguard.setItemMeta(tpguardMeta);
		
		ItemStack sellguard = new ItemStack(Material.BLAZE_POWDER); //create sell button
		ItemMeta sellguardMeta = sellguard.getItemMeta();
		sellguardMeta.setDisplayName(ChatColor.DARK_RED + Sell);
		sellguard.setItemMeta(sellguardMeta);
		
		ItemStack UpgradeArmor1 = new ItemStack(Material.ANVIL);
		ItemMeta UpgradeA1 = UpgradeArmor1.getItemMeta();
		UpgradeA1.setDisplayName(ChatColor.GOLD + UpButArmorv2);
		UpgradeArmor1.setItemMeta(UpgradeA1);
		
		ItemStack UpgradeArmor2 = new ItemStack(Material.BEACON);
		ItemMeta UpgradeA2 = UpgradeArmor2.getItemMeta();
		UpgradeA2.setDisplayName(ChatColor.GOLD + UpButArmorv3);
		UpgradeArmor2.setItemMeta(UpgradeA2);
		
		ItemStack UpgradeWeapon1 = new ItemStack(Material.IRON_SWORD);
		ItemMeta UpgradeW1 = UpgradeWeapon1.getItemMeta();
		UpgradeW1.setDisplayName(ChatColor.GOLD + UpButWeaponv2);
		UpgradeWeapon1.setItemMeta(UpgradeW1);
		
		ItemStack UpgradeWeapon2 = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta UpgradeW2 = UpgradeWeapon2.getItemMeta();
		UpgradeW2.setDisplayName(ChatColor.GOLD + UpButWeaponv3);
		UpgradeWeapon2.setItemMeta(UpgradeW2);

		//place every buttons
		inv.setItem(0, buyguard);
		inv.setItem(4, tpguard);
		inv.setItem(8, sellguard);
		
		NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
     	try {
     		NPC npc = npcRegistry.getByUniqueId(AssociatedNPC.get(player.getUniqueId()));   
     		if(npc != null){
		        Equipment equipTrait = npc.getTrait(Equipment.class);
	            
	            if(!equipTrait.get(1).getType().name().contentEquals(InterHead) && !equipTrait.get(1).getType().name().contentEquals(AdvancedHead)){
	        		inv.setItem(9, UpgradeArmor1);
	        	}
	            if(equipTrait.get(1).getType().name().contentEquals(InterHead) && !equipTrait.get(1).getType().name().contentEquals(AdvancedHead)){
	            	inv.setItem(13, UpgradeArmor2);	            	
	            }
	            
	            if(!equipTrait.get(0).getType().name().contentEquals(InterWeapon) && !equipTrait.get(0).getType().name().contentEquals(AdvancedWeapon)){
					inv.setItem(15, UpgradeWeapon1);
	            }
	            if(equipTrait.get(0).getType().name().contentEquals(InterWeapon) && !equipTrait.get(0).getType().name().contentEquals(AdvancedWeapon)){
					inv.setItem(17, UpgradeWeapon2);
	            }
	     	}
        } catch (Exception e) {
            e.printStackTrace();
    	}
		
		player.openInventory(inv);
	}
	
    public void skin(final NPC npc, String name, Player player) {
        String skinName = name;
        Preconditions.checkNotNull(skinName);
        try{
            npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, skinName.toLowerCase());
	       	player.sendMessage(String.format(SkinSet, name));
        } catch (Exception e) {
            npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, player.getName().toLowerCase());
        	player.sendMessage(String.format(SkinNotSet, player.getName()));
        }
        npc.data().set(NPC.PLAYER_SKIN_USE_LATEST, false);
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent evt){
    	Player player = evt.getPlayer();
	   	if (MyList.contains(player.getUniqueId())) {
			NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
	     	try {
	     		NPC npc = npcRegistry.getByUniqueId(AssociatedNPC.get(player.getUniqueId()));   
	     		if(npc != null){
	                skin(npc, evt.getMessage(), player);
					evt.setCancelled(true);
			        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new SpawnNPC(player));
		     	}
	        } catch (Exception e) {
	            e.printStackTrace();
	    	}
    		MyList.remove(player.getUniqueId());
    	}
	   	
	   	if(PendingReload.containsKey(player.getUniqueId()) && PendingReload.containsValue(true) && evt.getMessage().equalsIgnoreCase("yes")){
			PluginManager plg = Bukkit.getPluginManager();
			Plugin plgname = plg.getPlugin("PNJGui");
			plg.disablePlugin(plgname);
			plg.enablePlugin(plgname);
			player.sendMessage(ChatColor.GREEN + "[PNJGui] Plugin reloaded.");
			PendingReload.remove(player.getUniqueId());
			evt.setCancelled(true);
	   	}else if(PendingReload.containsKey(player.getUniqueId()) && PendingReload.containsValue(true) && evt.getMessage().equalsIgnoreCase("no")){
			PendingReload.remove(player.getUniqueId());	
			player.sendMessage(ChatColor.GREEN + "Reload cancelled.");
			evt.setCancelled(true);
	   	}
	}
    
    class SpawnNPC implements Runnable
    {
        private Player player;
     
        public SpawnNPC(Player p)
        {
            player = p;
        }
     
        public void run()
        {
			NPC spawnNPC = CitizensAPI.getNPCRegistry().getByUniqueId(AssociatedNPC.get(player.getUniqueId()));
			spawnNPC.spawn(player.getLocation());
       }
    }
    
    class CreateSpawnNPC implements Runnable
    {
        private Player player;
     
        public CreateSpawnNPC(Player p)
        {
            player = p;
        }
     
        public void run()
        {
			NPC spawnNPC = CitizensAPI.getNPCRegistry().createNPC(player.getType(), player.getName() + "'s bodyguard");
			spawnNPC.spawn(player.getLocation());
			spawnNPC.getTrait(Owner.class).setOwner(player.getName(), player.getUniqueId());
			spawnNPC.getTrait(SkinLayers.class);
			spawnNPC.getTrait(SentryTrait.class);
			UUID myuuid = player.getUniqueId();
	    	UUID npcuuid = spawnNPC.getUniqueId();
			player.getLocation().getBlockX();
			player.getLocation().getBlockY();
			player.getLocation().getBlockZ();

	    	AssociatedNPC.put(myuuid, npcuuid);
			economy.withdrawPlayer(player, BuyPrice);
			SentryInstance inst = spawnNPC.getTrait(SentryTrait.class).getInstance();
			inst.setGuardTarget(player.getName(), true);
			
			Equipment equipTrait = spawnNPC.getTrait(Equipment.class);
			ItemStack Basicsword = new ItemStack(Material.matchMaterial(BasicWeapon), 1);
			ItemStack BasicHelmet = new ItemStack(Material.matchMaterial(BasicHead), 1);
			ItemStack BasicChest = new ItemStack(Material.matchMaterial(BasicChestPlate), 1);
			ItemStack BasicPants = new ItemStack(Material.matchMaterial(BasicLeggings), 1);
			ItemStack BasicBoot = new ItemStack(Material.matchMaterial(BasicBoots), 1);
			
			equipTrait.set(0, Basicsword);
			equipTrait.set(1, BasicHelmet);
			equipTrait.set(2, BasicChest);
			equipTrait.set(3, BasicPants);
			equipTrait.set(4, BasicBoot);
				
			PutInlist(player);
			
			player.sendMessage(String.format("%s"+TypeSkinName, ChatColor.GREEN));
			player.sendMessage(String.format("%s"+MoneyLeft + " " + economy.getBalance(player)+" !", ChatColor.GREEN));
			spawnNPC.despawn();
       }
    }
    
    class UpgradePlayerNPC implements Runnable
    {
    	private Player player;
    	private String Level;
    	public UpgradePlayerNPC(Player p, String Rank)
    	{
    		player = p;
    		Level = Rank;
    	}
    	
    	public void run()
    	{
    		NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
    		NPC npc = null;
            try {
       			npc = npcRegistry.getByUniqueId(AssociatedNPC.get(player.getUniqueId()));    
            } catch (Exception e) {
                e.printStackTrace();
        	}
            if(npc != null){
		        Equipment equipTrait = npc.getTrait(Equipment.class);
	            if(Level == "Inter"){
					ItemStack Helmet = new ItemStack(Material.matchMaterial(InterHead), 1);
					ItemStack Chest = new ItemStack(Material.matchMaterial(InterChestPlate), 1);
					ItemStack Pants = new ItemStack(Material.matchMaterial(InterLeggings), 1);
					ItemStack Boot = new ItemStack(Material.matchMaterial(InterBoots), 1);
					
					equipTrait.set(1, Helmet);
					equipTrait.set(2, Chest);
					equipTrait.set(3, Pants);
					equipTrait.set(4, Boot);
	            }
	            else if(Level == "Advanced")
	            {
					ItemStack Helmet = new ItemStack(Material.matchMaterial(AdvancedHead), 1);
					ItemStack Chest = new ItemStack(Material.matchMaterial(AdvancedChestPlate), 1);
					ItemStack Pants = new ItemStack(Material.matchMaterial(AdvancedLeggings), 1);
					ItemStack Boot = new ItemStack(Material.matchMaterial(AdvancedBoots), 1);
					
					equipTrait.set(1, Helmet);
					equipTrait.set(2, Chest);
					equipTrait.set(3, Pants);
					equipTrait.set(4, Boot);            	
	            }
            }
    	}
    }
    
    class UpgradeNPCWeapon implements Runnable
    {
    	private Player player;
    	private String Level;
    	public UpgradeNPCWeapon(Player p, String Rank)
    	{
    		player = p;
    		Level = Rank;
    	}
    	
    	public void run()
    	{
    		NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
    		NPC npc = null;
            try {
       			npc = npcRegistry.getByUniqueId(AssociatedNPC.get(player.getUniqueId()));    
            } catch (Exception e) {
                e.printStackTrace();
        	}
            if(npc != null)
            {
	            Equipment equipTrait = npc.getTrait(Equipment.class);
	            if(Level == "Inter"){
					ItemStack sword = new ItemStack(Material.matchMaterial(InterWeapon), 1);
					equipTrait.set(0, sword);
	            }
	            else if(Level == "Advanced")
	            {
					ItemStack sword = new ItemStack(Material.matchMaterial(AdvancedWeapon), 1);
					equipTrait.set(0, sword);
	            }
            }
    	}
    }
    
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase(GuiName))
			return;
		Player player = (Player) event.getWhoClicked();
		event.setCancelled(true);

		NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
		NPC npc = null;
        try {
   			npc = npcRegistry.getByUniqueId(AssociatedNPC.get(player.getUniqueId()));    
        } catch (Exception e) {
            e.printStackTrace();
    	}
		if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !event.getCurrentItem().hasItemMeta()){
			player.closeInventory();
			return;
		}
		int x, y, z;
		x = player.getLocation().getBlockX();
		y = player.getLocation().getBlockY();
		z = player.getLocation().getBlockZ();
		switch(event.getCurrentItem().getType()){
		case DOUBLE_PLANT:
			if(economy != null)
			{
				if(economy.getBalance(player) >= BuyPrice){
					if(npc == null){
				        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new CreateSpawnNPC(player));
					}else
					{
						player.sendMessage(String.format("%s"+CantBuy, ChatColor.DARK_RED));
					}
				}else{
					player.sendMessage(String.format("%s"+NotEnoughMoney, ChatColor.DARK_RED));					
				}
			}
			else{
				player.sendMessage(String.format("%s"+NoBuyEconomy, ChatColor.DARK_RED));					
			}
			player.closeInventory();
			break;
		case LEASH:
			if(economy != null)
			{
				if(economy.getBalance(player) >= CallPrice){
					if(npc != null){
						if(npc.isSpawned()){
							economy.withdrawPlayer(player, CallPrice);				
							Location location = new Location(player.getWorld(), x, y, z);
							npc.teleport(location, TeleportCause.COMMAND);
							player.sendMessage(String.format("%s"+BodyTeleported, ChatColor.GREEN));					
							player.sendMessage(String.format("%s"+MoneyLeft + " " + economy.getBalance(player)+" !", ChatColor.GREEN));							
						}else{
							economy.withdrawPlayer(player, CallPrice);
							Location location = new Location(player.getWorld(), x, y, z);
							npc.spawn(location);
							player.sendMessage(String.format("%s"+BodySpawn, ChatColor.GREEN));
						}
					}else
					{
						player.sendMessage(String.format("%s"+NoBodyguard, ChatColor.DARK_RED));					
					}
				}else{
					player.sendMessage(String.format("%s"+NotEnoughMoney, ChatColor.DARK_RED));					
				}
			}
			else{
				player.sendMessage(String.format("%s"+NoCallBody, ChatColor.DARK_RED));					
			}
			player.closeInventory();
			break;
		case BLAZE_POWDER:
			if(npc != null){
				if(npc.isSpawned()){
			        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new delNPC(npc));
			        economy.depositPlayer(player, SellPrice);
					player.sendMessage(String.format("%s"+SoldBody, ChatColor.DARK_RED ));
					player.sendMessage(String.format("%s"+MoneyLeft + " " + economy.getBalance(player)+" !", ChatColor.GREEN));							
				}else{
					player.sendMessage(String.format("%s"+SpawnBefore, ChatColor.DARK_RED));
				}
			}else
			{
				player.sendMessage(String.format("%s"+NoBodyguard, ChatColor.DARK_RED));
			}
			player.closeInventory();
			break;
		case ANVIL:
			if(economy != null)
			{
				if(economy.getBalance(player) >= UpgradeLevel2){
					if(npc != null){
						if(npc.isSpawned()){
							if(UpgradeLevel2 > 0){
								Bukkit.getScheduler().scheduleSyncDelayedTask(this, new UpgradePlayerNPC(player, "Inter"));
								economy.withdrawPlayer(player, UpgradeLevel2);
								player.sendMessage(String.format("%s"+UpArmorLvl2, ChatColor.GREEN));
							}else{
								player.sendMessage(String.format("%s"+UpArmorDisable, ChatColor.DARK_RED));
							}
						}else{
							player.sendMessage(String.format("%s"+SpawnBefore, ChatColor.DARK_RED));
						}
					}else{
						player.sendMessage(String.format("%s"+NoBodyguard, ChatColor.DARK_RED));					
					}
				}else{
					player.sendMessage(String.format("%s"+NotEnoughMoney, ChatColor.DARK_RED));					
				}
			}
			else{
				player.sendMessage(String.format("%s"+CantUpArmor, ChatColor.DARK_RED));					
			}
			player.closeInventory();
			break;
		case BEACON:
			if(economy != null)
			{
				if(economy.getBalance(player) >= UpgradeLevel3){
					if(npc != null){
						if(npc.isSpawned()){
							if(UpgradeLevel3 > 0 && UpgradeLevel2 > 0){
								Bukkit.getScheduler().scheduleSyncDelayedTask(this, new UpgradePlayerNPC(player, "Advanced"));
								economy.withdrawPlayer(player, UpgradeLevel3);
								player.sendMessage(String.format("%s"+UpArmorLvl3, ChatColor.GREEN));
							}else{
								player.sendMessage(String.format("%s"+UpArmorDisable, ChatColor.DARK_RED));
							}
						}else{
							player.sendMessage(String.format("%s"+NoBodyguard, ChatColor.DARK_RED));
						}
					}else{
						player.sendMessage(String.format("%s"+NotEnoughMoney, ChatColor.DARK_RED));					
					}
				}else{
					player.sendMessage(String.format("%sYou don't get enough money !", ChatColor.DARK_RED));					
				}
			}
			else{
				player.sendMessage(String.format("%s"+CantUpArmor, ChatColor.DARK_RED));					
			}
			player.closeInventory();
			break;
		case IRON_SWORD:
			if(economy != null)
			{
				if(economy.getBalance(player) >= UpgradeWeaponPriceLevel2){
					if(npc != null){
						if(npc.isSpawned()){
							if(UpgradeWeaponPriceLevel2 > 0){
								Bukkit.getScheduler().scheduleSyncDelayedTask(this, new UpgradeNPCWeapon(player, "Inter"));
								economy.withdrawPlayer(player, UpgradeWeaponPriceLevel2);
								player.sendMessage(String.format("%s"+UpWeaponLvl2, ChatColor.GREEN));
							}else{
								player.sendMessage(String.format("%s"+UpWeaponDisable, ChatColor.DARK_RED));
							}
						}else{
							player.sendMessage(String.format("%s"+SpawnBefore, ChatColor.DARK_RED));
						}
					}else{
						player.sendMessage(String.format("%s"+NoBodyguard, ChatColor.DARK_RED));					
					}
				}else{
					player.sendMessage(String.format("%s"+NotEnoughMoney, ChatColor.DARK_RED));					
				}
			}
			else{
				player.sendMessage(String.format("%s"+UpWeaponDisable, ChatColor.DARK_RED));					
			}
			player.closeInventory();
			break;
		case DIAMOND_SWORD:
			if(economy != null)
			{
				if(economy.getBalance(player) >= UpgradeWeaponPriceLevel3){
					if(npc != null){
						if(npc.isSpawned()){
							if(UpgradeWeaponPriceLevel3 > 0){
								Bukkit.getScheduler().scheduleSyncDelayedTask(this, new UpgradeNPCWeapon(player, "Advanced"));
								economy.withdrawPlayer(player, UpgradeWeaponPriceLevel3);
								player.sendMessage(String.format("%s"+UpWeaponLvl3, ChatColor.GREEN));
							}else{
								player.sendMessage(String.format("%s"+UpWeaponDisable, ChatColor.DARK_RED));
							}
						}else{
							player.sendMessage(String.format("%s"+SpawnBefore, ChatColor.DARK_RED));
						}
					}else{
						player.sendMessage(String.format("%s"+NoBodyguard, ChatColor.DARK_RED));					
					}
				}else{
					player.sendMessage(String.format("%s"+NotEnoughMoney, ChatColor.DARK_RED));					
				}
			}
			else{
				player.sendMessage(String.format("%s"+UpWeaponDisable, ChatColor.DARK_RED));					
			}
			player.closeInventory();
			break;
		default:
			player.closeInventory();
			break;
		}
	}
	
}
class delNPC implements Runnable
{
    private NPC targetLoc;
	NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();

    public delNPC(NPC npc)
    {
        targetLoc = npc;
    }
 
    public void run()
    {
    	targetLoc.data().remove(NPC.PLAYER_SKIN_UUID_METADATA);
    	targetLoc.removeTrait(SentryTrait.class);
    	targetLoc.removeTrait(SkinLayers.class);
    	targetLoc.removeTrait(Owner.class);
		npcRegistry.deregister(targetLoc);
		targetLoc.destroy();
    }
}

class destroyNPC implements Runnable
{
	private NPC npcs;
	NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
	
	public destroyNPC(NPC npc)
	{
		npcs = npc;
	}
	
	public void run()
	{
		npcs.destroy();
	}
}
