<img width="1536" height="1024" alt="MainImage" src="https://github.com/user-attachments/assets/2ac96d37-24e1-4e31-bf49-a75b4c373101" />

# TreeFella_Extended
This repository is a fork of the original <a href="https://github.com/Vittorassi/TreeFella">plugin</a> by <a href="https://github.com/Vittorassi/">Vittorassi</a>.

A simple plugin for quickly chopping down trees and ore mining!

<a href="https://hangar.papermc.io/V1ttorassi/TreeFella">TreeFella</a> original Hangar page

<a href="https://hangar.papermc.io/fakemade/TreeFella_Extended">TreeFella_Extended</a> Hangar page
## Differences from the original plugin

1. Update to 1.21.11 core for Paper
2. Added copper tools
3. Rewrite felling/mining algorithm
   3\.1. Now the items are cut down one by one with a very small delay to add a bit of verisimilitude.
   3\.2. The algorithm for getting dropped blocks has been changed. Now there are several schemes:
   - If there is enough strength of the tool and space in the inventory → the blocks break, the drop is placed in the inventory.
   - If the tool has enough durability but the inventory is full → the blocks will break. The system then checks for available space. If there is none, all loot drops at the block's location. If there is some space, part of the loot is added to the inventory, and the remainder is dropped on the ground.
   - If the tool’s durability is insufficient to break all blocks → only the amount corresponding to the remaining durability will be destroyed. Regarding inventory storage, the conditions mentioned above apply. Any remaining blocks that exceeded the tool's durability stay intact.
   3\.3. Now, if the ore is nearby but in different types of blocks (deepslate and stone), they will break together, since the type of ore is the same.
4. There is a destruction effect for each broken block, previously the effect was only for the first one.
5. New commands and opportunity using emotes for Bedrock players

# How to install
- Download  ".jar" the file  and  place it in the plugins  folder
- Start/restart your server


## How to use it
Now plugin has 2 commands for enabling and disabling plugin and sneaking requirement.
- ***/tfetoggle on*** - enable plugin. If it's enabled it sends message in chat 
- ***/tfetoggle off*** - disable plugin. If it's enabled it sends message in chat 
- ***/tfesneaking on*** - enable requirement sneaking. If it's enabled it sends message in chat
- ***/tfesneaking off*** - disable requirement sneaking. If it's enabled it sends message in chat
You can define sneaking requirement in **config.yml**.
### Info for Bedrock players
For bedrock players who play in Java servers through Geyser you can use emotes for two command above.
You can define this emotes in **config.yml**. Now available 4 type of emotes(because its free ond common):
1. *"waving"*
2. *"asking everyone to follow"*
3. *"pointing overthere"*
4. *"clapping"*

If emote is used it triggers above commands.

Example of default **config.yml**:
```yaml
need_sneaking: false
emote_toggle: "waving"
emote_sneaking: "asking everyone to follow"
```