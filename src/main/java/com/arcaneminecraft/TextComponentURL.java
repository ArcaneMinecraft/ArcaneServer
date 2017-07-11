package com.arcaneminecraft;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TextComponentURL {
	public static TextComponent activate(String[] ArrayWithLink) {
		return activate(ArrayWithLink, 0);
	}
	
	public static TextComponent activate(String[] ArrayWithLink, int fromIndex) {
		TextComponent ret = new TextComponent();
		for (int i = fromIndex; i < ArrayWithLink.length; i++) {
			if (i != fromIndex) ret.addExtra(" ");
			if (ArrayWithLink[i].matches(".+\\..+|http(s?):\\/\\/.+")) {
				String sent = ArrayWithLink[i].startsWith("http://") || ArrayWithLink[i].startsWith("https://") ? ArrayWithLink[i] : "http://" + ArrayWithLink[i];
				TextComponent linked = new TextComponent(ArrayWithLink[i]);
				linked.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, 
						sent));
				ret.addExtra(linked);
			} else {
				ret.addExtra(ArrayWithLink[i]);
			}
		}
		return ret;
	}
}
