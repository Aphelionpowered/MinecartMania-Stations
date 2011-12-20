package com.afforess.minecartmaniastation;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.minecart.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.utils.ItemUtils;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;

public enum StationCondition implements Condition {
    Default {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return str.equals("D") || str.toLowerCase().contains("default");
        }
    },
    Empty {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return minecart.isStandardMinecart() && (minecart.minecart.getPassenger() == null) && str.toLowerCase().contains("empty");
        }
        
    },
    Player {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return minecart.hasPlayerPassenger() && str.toLowerCase().contains("player");
        }
        
    },
    Mob {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (minecart.minecart.getPassenger() != null) && !minecart.hasPlayerPassenger() && str.toLowerCase().contains("mob");
        }
    },
    Pig {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (minecart.minecart.getPassenger() instanceof org.bukkit.entity.Pig) && str.toLowerCase().contains("pig");
        }
    },
    Chicken {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (minecart.minecart.getPassenger() instanceof org.bukkit.entity.Chicken) && str.toLowerCase().contains("chicken");
        }
    },
    Cow {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (minecart.minecart.getPassenger() instanceof org.bukkit.entity.Cow) && str.toLowerCase().contains("cow");
        }
    },
    Sheep {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (minecart.minecart.getPassenger() instanceof org.bukkit.entity.Sheep) && str.toLowerCase().contains("sheep");
        }
    },
    Creeper {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (minecart.minecart.getPassenger() instanceof org.bukkit.entity.Creeper) && str.toLowerCase().contains("creeper");
        }
    },
    Skeleton {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (minecart.minecart.getPassenger() instanceof org.bukkit.entity.Skeleton) && str.toLowerCase().contains("skeleton");
        }
    },
    Zombie {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (minecart.minecart.getPassenger() instanceof org.bukkit.entity.Zombie) && str.toLowerCase().contains("zombie");
        }
    },
    StationCommand {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return minecart.hasPlayerPassenger() && SignCommands.processStationCommand(minecart, str);
        }
    },
    PlayerName {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return minecart.hasPlayerPassenger() && str.equalsIgnoreCase(minecart.getPlayerPassenger().getName());
        }
    },
    ContainsItem {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            if (minecart.hasPlayerPassenger() && (minecart.getPlayerPassenger().getItemInHand() != null)) {
                final ItemStack itemInHand = minecart.getPlayerPassenger().getItemInHand();
                final ItemMatcher[] signData = ItemUtils.getItemStringToMatchers(str, CompassDirection.NO_DIRECTION);
                for (final ItemMatcher matcher : signData) {
                    if ((matcher != null) && matcher.match(itemInHand))
                        return true;
                }
            } else if (minecart.isStorageMinecart()) {
                final MinecartManiaStorageCart cart = ((MinecartManiaStorageCart) minecart);
                final ItemMatcher[] signData = ItemUtils.getItemStringToMatchers(str, CompassDirection.NO_DIRECTION);
                for (final ItemMatcher matcher : signData) {
                    for (int i = 0; i < cart.size(); i++) {
                        final ItemStack item = cart.getItem(i);
                        if ((item != null) && matcher.match(item)) {
                            if (cart.amount(item.getTypeId(), item.getDurability()) > matcher.getAmount(-1))
                                return true;
                        }
                    }
                }
            }
            return false;
        }
    },
    Cargo {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return minecart.isStorageMinecart() && str.toLowerCase().contains("cargo") && ((MinecartManiaStorageCart) minecart).isEmpty();
        }
    },
    Storage {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return minecart.isStorageMinecart() && str.toLowerCase().contains("storage");
        }
    },
    Powered {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return minecart.isPoweredMinecart() && str.toLowerCase().contains("powered");
        }
    },
    West {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (str.equals("W") || str.toLowerCase().contains("west")) && !str.contains("-") && (minecart.getDirection() == CompassDirection.WEST);
        }
    },
    East {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (str.equals("E") || str.toLowerCase().contains("east")) && !str.contains("-") && (minecart.getDirection() == CompassDirection.EAST);
        }
    },
    North {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (str.equals("N") || str.toLowerCase().contains("north")) && !str.contains("-") && (minecart.getDirection() == CompassDirection.NORTH);
        }
    },
    South {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return (str.equals("S") || str.toLowerCase().contains("south")) && !str.contains("-") && (minecart.getDirection() == CompassDirection.SOUTH);
        }
    },
    Redstone {
        @Override
        public boolean result(final MinecartManiaMinecart minecart, final String str) {
            return str.toLowerCase().contains("redstone") && (minecart.isPoweredBeneath() || MinecartManiaWorld.isBlockIndirectlyPowered(minecart.minecart.getWorld(), minecart.getX(), minecart.getY() - 2, minecart.getZ()));
        }
    };
    
    public boolean result(final MinecartManiaMinecart minecart, final String str) {
        return false;
    }
}
