package com.bada774.fet.utils.dumping;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class FETCommand extends CommandBase {

    private static final Map<String, Runnable> DUMP_HANDLERS = new LinkedHashMap<>();
    static {
        DUMP_HANDLERS.put("all",        MachineDumper::dumpAll);
        DUMP_HANDLERS.put("alloy",      MachineDumper::dumpAlloySmelter);
        DUMP_HANDLERS.put("combustion", MachineDumper::dumpCombustion);
        DUMP_HANDLERS.put("enchanter",  MachineDumper::dumpEnchanter);
        DUMP_HANDLERS.put("sagmill",    MachineDumper::dumpSagMill);
        DUMP_HANDLERS.put("slice",      MachineDumper::dumpSliceNSplice);
        DUMP_HANDLERS.put("soulbinder", MachineDumper::dumpSoulBinder);
        DUMP_HANDLERS.put("tank",       MachineDumper::dumpTank);
        DUMP_HANDLERS.put("vat",        MachineDumper::dumpVat);
    }

    @Override
    @MethodsReturnNonnullByDefault
    public String getName() {
        return "fet";
    }

    @Override
    @MethodsReturnNonnullByDefault
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/fet dump <" + String.join(" | ", DUMP_HANDLERS.keySet()) + ">";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args)
            throws CommandException {

        if (args.length < 2 || !args[0].equalsIgnoreCase("dump")) {
            sender.sendMessage(usage());
            return;
        }

        String target = args[1].toLowerCase();
        Runnable handler = DUMP_HANDLERS.get(target);

        if (handler == null) {
            sender.sendMessage(error("Unknown dump target: " + args[1]));
            sender.sendMessage(usage());
            return;
        }

        handler.run();
        sender.sendMessage(ok());
    }

    @Override
    @MethodsReturnNonnullByDefault
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender,
                                          String[] args, BlockPos pos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Collections.singletonList("dump"));
        if (args.length == 2 && args[0].equalsIgnoreCase("dump"))
            return getListOfStringsMatchingLastWord(args, DUMP_HANDLERS.keySet());
        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    private static TextComponentString usage() {
        return new TextComponentString(
            TextFormatting.YELLOW + "Usage: /fet dump <"
            + String.join("|", DUMP_HANDLERS.keySet()) + ">");
    }

    private static TextComponentString error(String text) {
        return new TextComponentString(TextFormatting.RED + "[FET] " + text);
    }

    private static TextComponentString ok() {
        return new TextComponentString(TextFormatting.GREEN + "[FET] " + "Dump complete - check latest.log for [FET_DUMP_*] entries.");
    }
}
