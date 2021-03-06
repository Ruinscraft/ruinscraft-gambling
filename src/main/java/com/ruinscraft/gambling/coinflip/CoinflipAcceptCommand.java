package com.ruinscraft.gambling.coinflip;

import com.ruinscraft.gambling.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinflipAcceptCommand implements CommandExecutor {

    private CoinflipManager coinflipManager;

    public CoinflipAcceptCommand(CoinflipManager coinflipManager) {
        this.coinflipManager = coinflipManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/" + label + " <amount>");
            return false;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount.");
            return false;
        }

        if (!VaultUtil.has(player, amount)) {
            player.sendMessage(ChatColor.RED + "You do not have enough to make this bet.");
            return false;
        }

        CoinflipManager.Coinflip coinflip = coinflipManager.acceptCoinflip(player, amount);

        if (coinflip == null) {
            player.sendMessage(ChatColor.RED + "No coinflip to accept.");
            return false;
        }

        Player winner = coinflip.decideWinner();
        final Player loser;

        if (winner == coinflip.getInitiator()) {
            loser = coinflip.getParticipant();
        } else {
            loser = coinflip.getInitiator();
        }

        if (!VaultUtil.has(loser, amount)) {
            winner.sendMessage(ChatColor.GOLD + loser.getName() + "'s balance changed and they did not have " + amount);
            winner.sendMessage(ChatColor.GOLD + "The coinflip has been canceled.");
            loser.sendMessage(ChatColor.GOLD + "Your balance was too low. The coinflip was canceled.");
            return false;
        }

        if (!VaultUtil.has(winner, amount)) {
            loser.sendMessage(ChatColor.GOLD + winner.getName() + "'s balance changed and they did not have " + amount);
            loser.sendMessage(ChatColor.GOLD + "The coinflip has been canceled.");
            winner.sendMessage(ChatColor.GOLD + "Your balance was too low. The coinflip was canceled.");
            return false;
        }

        if (VaultUtil.withdraw(loser, amount)) {
            VaultUtil.deposit(winner, amount);
            Bukkit.broadcastMessage(ChatColor.AQUA + winner.getName() + " won a coinflip against " + loser.getName() + " worth " + ChatColor.GREEN + coinflip.getCoinWorth());
        }

        return true;
    }

}
