package dev.cannonlines;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Cannon Lines (client only): a glowing beam out of the BACK of every chest near you,
 * because the cannon fires out of the back of the chest.
 *
 * Command (client side, never sent to the server): /cannonlines  -> toggle on/off
 */
public class CannonLinesClient implements ClientModInitializer {
	private static final double LENGTH = 64.0;   // beam length in blocks
	private static final double SPACING = 1.0;   // distance between glowing dots (smaller = more solid, more lag)
	private static final int SCAN_RADIUS = 32;   // horizontal search radius around you
	private static final int SCAN_HEIGHT = 16;   // vertical search range around you
	private static final int SCAN_EVERY = 20;    // ticks between chest scans
	private static final int DRAW_EVERY = 20;    // ticks between beam refreshes
	private static final int MAX_CHESTS = 20;    // safety cap

	private record Beam(Vec3 origin, Vec3 dir) {}

	private boolean enabled = true;
	private int tick = 0;
	private final List<Beam> beams = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(ClientCommandManager.literal("cannonlines").executes(ctx -> {
				enabled = !enabled;
				ctx.getSource().sendFeedback(Component.literal("Cannon Lines: " + (enabled ? "ON" : "OFF")));
				return 1;
			})));
	}

	private void onTick(Minecraft mc) {
		ClientLevel level = mc.level;
		if (level == null || mc.player == null || !enabled) {
			beams.clear();
			return;
		}
		tick++;

		if (tick % SCAN_EVERY == 1) {
			scanChests(level, mc.player.blockPosition());
		}
		if (tick % DRAW_EVERY == 0) {
			for (Beam beam : beams) {
				drawBeam(level, beam);
			}
		}
	}

	/** Finds chests around the player and remembers which way their back points. */
	private void scanChests(ClientLevel level, BlockPos center) {
		beams.clear();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
			for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
				for (int dy = -SCAN_HEIGHT; dy <= SCAN_HEIGHT; dy++) {
					pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					BlockState state = level.getBlockState(pos);
					if (state.getBlock() instanceof ChestBlock) {
						Direction back = state.getValue(ChestBlock.FACING).getOpposite();
						Vec3 dir = new Vec3(back.getStepX(), 0, back.getStepZ());
						Vec3 origin = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).add(dir.scale(0.5));
						beams.add(new Beam(origin, dir));
						if (beams.size() >= MAX_CHESTS) {
							return;
						}
					}
				}
			}
		}
	}

	private static void drawBeam(ClientLevel level, Beam beam) {
		for (double d = 0; d <= LENGTH; d += SPACING) {
			// "Always visible" particles are shown even when far away (normal ones vanish past ~32 blocks).
			level.addAlwaysVisibleParticle(ParticleTypes.END_ROD,
				beam.origin().x + beam.dir().x * d,
				beam.origin().y + beam.dir().y * d,
				beam.origin().z + beam.dir().z * d,
				0.0, 0.0, 0.0);
		}
	}
}
