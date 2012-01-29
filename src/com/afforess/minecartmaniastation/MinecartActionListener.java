package com.afforess.minecartmaniastation;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import com.afforess.minecartmaniacore.config.ControlBlockList;
import com.afforess.minecartmaniacore.config.LocaleParser;
import com.afforess.minecartmaniacore.entity.MinecartManiaPlayer;
import com.afforess.minecartmaniacore.event.MinecartActionEvent;
import com.afforess.minecartmaniacore.event.MinecartClickedEvent;
import com.afforess.minecartmaniacore.event.MinecartIntersectionEvent;
import com.afforess.minecartmaniacore.event.MinecartLaunchedEvent;
import com.afforess.minecartmaniacore.event.MinecartManiaListener;
import com.afforess.minecartmaniacore.event.MinecartManiaMinecartDestroyedEvent;
import com.afforess.minecartmaniacore.event.MinecartMeetsConditionEvent;
import com.afforess.minecartmaniacore.event.MinecartMotionStartEvent;
import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.utils.DirectionUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.StringUtils;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;

public class MinecartActionListener extends MinecartManiaListener {
    
    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMinecartActionEvent(final MinecartActionEvent event) {
        final MinecartManiaMinecart minecart = event.getMinecart();
        
        if (!minecart.isAtIntersection()) {
            if (minecart.getDataValue("old rail data") != null) {
                @SuppressWarnings("unchecked")
                final ArrayList<Integer> blockData = (ArrayList<Integer>) minecart.getDataValue("old rail data");
                MinecartManiaWorld.setBlockData(minecart.minecart.getWorld(), blockData.get(0), blockData.get(1), blockData.get(2), blockData.get(3));
                minecart.setDataValue("old rail data", null);
            }
        }
        
        //stop moving, there is a queue ahead of us
        //TODO Fix this
        /*
         * MinecartManiaMinecart minecartAhead = minecart.getMinecartAhead(); while (true) { if (minecartAhead == null) { break; } if (minecartAhead.minecart.getEntityId() == minecart.minecart.getEntityId()) { break; } if (minecartAhead.getMinecartAhead() == null) { break; } if (minecartAhead.isMoving()) { break; } minecartAhead = minecartAhead.getMinecartAhead(); } if (minecartAhead != null) { if (minecartAhead.isAtIntersection()) { if (!minecartAhead.isMoving()) { minecart.setDataValue("queued velocity", minecart.minecart.getVelocity().clone()); minecart.stopCart(); if (minecart.hasPlayerPassenger()) ChatUtils.sendMultilineMessage(minecart.getPlayerPassenger(), "You've entered a queue. Please be patient.", ChatColor.YELLOW.toString()); } } }
         */
    }
    
    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMinecartIntersectionEvent(final MinecartIntersectionEvent event) {
        final MinecartManiaMinecart minecart = event.getMinecart();
        
        if (event.isActionTaken())
            return;
        
        if (ControlBlockList.isValidStationBlock(minecart)) {
            SignCommands.processStation(event);
        }
        
        if (StationUtil.shouldPromptUser(minecart)) {
            
            minecart.setDataValue("preintersection velocity", minecart.minecart.getVelocity().clone());
            minecart.stopCart();
            final Player passenger = minecart.getPlayerPassenger();
            //set the track straight
            final int data = DirectionUtils.getMinetrackRailDataForDirection(minecart.getDirection(), minecart.getDirection());
            final Block oldBlock = MinecartManiaWorld.getBlockAt(minecart.minecart.getWorld(), minecart.getX(), minecart.getY(), minecart.getZ());
            final ArrayList<Integer> blockData = new ArrayList<Integer>();
            blockData.add(new Integer(oldBlock.getX()));
            blockData.add(new Integer(oldBlock.getY()));
            blockData.add(new Integer(oldBlock.getZ()));
            blockData.add(new Integer(oldBlock.getData()));
            minecart.setDataValue("old rail data", blockData);
            if (data != -1) {
                MinecartManiaWorld.setBlockData(minecart.minecart.getWorld(), minecart.getX(), minecart.getY(), minecart.getZ(), data);
            }
            passenger.sendMessage(LocaleParser.getTextKey("StationsTapInDirection"));
        }
        
    }
    
    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMinecartLaunchedEvent(final MinecartLaunchedEvent event) {
        if (event.isActionTaken())
            return;
        SignCommands.processStation(event);
    }
    
    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMinecartMotionStartEvent(final MinecartMotionStartEvent event) {
        final MinecartManiaMinecart minecart = event.getMinecart();
        if (minecart.isAtIntersection()) {
            StationUtil.updateQueue(minecart);
        }
    }
    
    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMinecartManiaMinecartDestroyedEvent(final MinecartManiaMinecartDestroyedEvent event) {
        final MinecartManiaMinecart minecart = event.getMinecart();
        StationUtil.updateQueue(minecart);
    }
    
    @Override
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMinecartClickedEvent(final MinecartClickedEvent event) {
        if (event.isActionTaken())
            return;
        final MinecartManiaMinecart minecart = event.getMinecart();
        if (StationUtil.isInQueue(minecart)) {
            event.setActionTaken(true);
            return;
        }
        final CompassDirection facingDir = DirectionUtils.getDirectionFromMinecartRotation((minecart.minecart.getPassenger().getLocation().getYaw() - 90.0F) % 360.0F);
        
        Vector velocity = (Vector) minecart.getDataValue("preintersection velocity");
        if (velocity == null)
            return;
        
        velocity = StationUtil.alterMotionFromDirection(facingDir, velocity);
        
        //responding to chat direction prompt
        if (minecart.isAtIntersection() && minecart.hasPlayerPassenger()) {
            if (StationUtil.isValidDirection(facingDir, minecart)) {
                final int data = DirectionUtils.getMinetrackRailDataForDirection(facingDir, minecart.getDirection());
                if (data != -1) {
                    MinecartManiaWorld.setBlockData(minecart.minecart.getWorld(), minecart.getX(), minecart.getY(), minecart.getZ(), data);
                }
                minecart.minecart.setVelocity(velocity);
                minecart.setDataValue("preintersection velocity", null);
            }
            event.setActionTaken(true);
        }
    }
    
    @Override
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMinecartMeetConditionEvent(final MinecartMeetsConditionEvent event) {
        if (event.isMeetCondition())
            return;
        final Sign sign = event.getSign();
        final MinecartManiaMinecart minecart = event.getMinecart();
        MinecartManiaPlayer player = null;
        Object old = null;
        if (minecart.hasPlayerPassenger()) {
            player = MinecartManiaWorld.getMinecartManiaPlayer(minecart.getPlayerPassenger());
            old = player.getDataValue("Reset Station Data");
            player.setDataValue("Reset Station Data", true);
        }
        loop: for (int i = 0; i < sign.getNumLines(); i++) {
            final String line = StringUtils.removeBrackets(sign.getLine(i).trim());
            for (final StationCondition e : StationCondition.values()) {
                if (e.result(minecart, line)) {
                    event.setMeetCondition(true);
                    break loop;
                }
            }
        }
        if (player != null) {
            player.setDataValue("Reset Station Data", old);
        }
    }
}
