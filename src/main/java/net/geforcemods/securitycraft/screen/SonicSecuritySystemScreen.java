package net.geforcemods.securitycraft.screen;

import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blockentities.SonicSecuritySystemBlockEntity;
import net.geforcemods.securitycraft.blockentities.SonicSecuritySystemBlockEntity.NoteWrapper;
import net.geforcemods.securitycraft.network.server.SyncSSSSettingsOnServer;
import net.geforcemods.securitycraft.network.server.SyncSSSSettingsOnServer.DataType;
import net.geforcemods.securitycraft.screen.components.SSSConnectionList;
import net.geforcemods.securitycraft.screen.components.SSSConnectionList.ConnectionAccessor;
import net.geforcemods.securitycraft.screen.components.TogglePictureButton;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class SonicSecuritySystemScreen extends Screen implements ConnectionAccessor {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/sonic_security_system.png");
	private static final ResourceLocation STREAMER_ICONS = new ResourceLocation("textures/gui/stream_indicator.png");
	private static final Component SOUND_TEXT = Utils.localize("gui.securitycraft:sonic_security_system.sound");
	/** The number of ticks between each note when playing back a recording **/
	private static final int PLAYBACK_DELAY = 10;
	private final SonicSecuritySystemBlockEntity be;
	private int xSize = 300, ySize = 166;
	private Button recordingButton, clearButton, powerButton, playButton;
	private TogglePictureButton soundButton;
	private SSSConnectionList<SonicSecuritySystemScreen> connectionList;
	/** If a recording is currently being played back **/
	private boolean playback = false;
	/** The number of ticks that has elapsed since the last note played **/
	private int tickCount = PLAYBACK_DELAY;
	private int currentNote = 0;
	private boolean isOwner;

	public SonicSecuritySystemScreen(SonicSecuritySystemBlockEntity be) {
		super(be.getName());
		this.be = be;
		isOwner = be.getOwner().isOwner(Minecraft.getInstance().player);
	}

	@Override
	public void tick() {
		// Play the note combination of this SSS when the player clicks on the play button
		if (playback) {
			tickCount++;

			// Only emit the note sound after a certain delay and if there are still notes to play
			if (tickCount >= PLAYBACK_DELAY) {
				if (currentNote < be.getNumberOfNotes()) {
					NoteWrapper note = be.getRecordedNotes().get(currentNote++);
					SoundEvent sound = NoteBlockInstrument.valueOf(note.instrumentName().toUpperCase()).getSoundEvent();
					float pitch = (float) Math.pow(2.0D, (note.noteID() - 12) / 12.0D);

					tickCount = 0;
					minecraft.level.playSound(minecraft.player, be.getBlockPos(), sound, SoundSource.RECORDS, 3.0F, pitch);
				}
				// Reset the counters when we are finished playing the final note
				else if (currentNote >= be.getNumberOfNotes()) {
					currentNote = 0;
					playback = false;
				}
			}
		}
	}

	@Override
	public void init() {
		super.init();

		boolean isActive = be.isActive();
		boolean hasNotes = be.getNumberOfNotes() > 0;
		int leftPos = (width - xSize) / 2;
		int buttonX = leftPos + xSize - 155;

		powerButton = addRenderableWidget(new ExtendedButton(buttonX, height / 2 - 59, 150, 20, getPowerString(be.isActive()), button -> {
			boolean toggledState = !be.isActive();
			boolean containsNotes = be.getNumberOfNotes() > 0;

			be.setActive(toggledState);
			SecurityCraft.channel.sendToServer(new SyncSSSSettingsOnServer(be.getBlockPos(), toggledState ? SyncSSSSettingsOnServer.DataType.POWER_ON : SyncSSSSettingsOnServer.DataType.POWER_OFF));
			powerButton.setMessage(getPowerString(toggledState));

			if (!toggledState)
				recordingButton.setMessage(getRecordingString(false));

			// Disable the recording-related buttons when the SSS is powered off
			recordingButton.active = toggledState;
			soundButton.active = toggledState;
			playButton.active = toggledState && containsNotes;
			clearButton.active = toggledState && containsNotes;
		}));

		recordingButton = addRenderableWidget(new ExtendedButton(buttonX, height / 2 - 32, 150, 20, getRecordingString(be.isRecording()), button -> {
			boolean recording = !be.isRecording();
			be.setRecording(recording);
			SecurityCraft.channel.sendToServer(new SyncSSSSettingsOnServer(be.getBlockPos(), recording ? SyncSSSSettingsOnServer.DataType.RECORDING_ON : SyncSSSSettingsOnServer.DataType.RECORDING_OFF));
			recordingButton.setMessage(getRecordingString(be.isRecording()));
		}));

		playButton = addRenderableWidget(new ExtendedButton(buttonX, height / 2 - 10, 150, 20, Utils.localize("gui.securitycraft:sonic_security_system.recording.play"), button -> {
			// Start playing back any notes that have been recorded
			if (be.getNumberOfNotes() > 0)
				playback = true;
		}));

		clearButton = addRenderableWidget(new ExtendedButton(buttonX, height / 2 + 12, 150, 20, Utils.localize("gui.securitycraft:sonic_security_system.recording.clear"), button -> {
			be.clearNotes();
			SecurityCraft.channel.sendToServer(new SyncSSSSettingsOnServer(be.getBlockPos(), SyncSSSSettingsOnServer.DataType.CLEAR_NOTES));
			playButton.active = false;
			clearButton.active = false;
		}));
		//@formatter:off
		soundButton = addRenderableWidget(new TogglePictureButton(buttonX + 130, height / 2 + 52, 20, 20, STREAMER_ICONS, new int[]{0, 0}, new int[]{32, 48}, 2, 16, 16, 16, 16, 16, 64, 2, button -> {
			//@formatter:on
			boolean toggledPing = !be.pings();

			be.setPings(toggledPing);
			SecurityCraft.channel.sendToServer(new SyncSSSSettingsOnServer(be.getBlockPos(), toggledPing ? SyncSSSSettingsOnServer.DataType.SOUND_ON : SyncSSSSettingsOnServer.DataType.SOUND_OFF));
		}));
		soundButton.setCurrentIndex(!be.pings() ? 1 : 0); // Use the disabled mic icon if the SSS is not emitting sounds

		connectionList = addRenderableWidget(new SSSConnectionList<>(this, minecraft, 130, 120, powerButton.y, leftPos + 10));

		powerButton.active = !be.isShutDown() && isOwner;
		recordingButton.active = isActive && isOwner;
		soundButton.active = isActive && isOwner;
		playButton.active = isActive && hasNotes;
		clearButton.active = isActive && hasNotes && isOwner;
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		int textWidth = font.width(title);
		int soundTextLength = font.width(SOUND_TEXT);

		renderBackground(pose);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem._setShaderTexture(0, TEXTURE);
		blit(pose, startX, startY, 0, 0, xSize, ySize, 512, 512);
		super.render(pose, mouseX, mouseY, partialTicks);
		font.draw(pose, title, startX + xSize / 2 - textWidth / 2, startY + 6, 4210752);
		font.draw(pose, SOUND_TEXT, soundButton.x - soundTextLength - 5, startY + 141, 4210752);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (connectionList != null)
			connectionList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public Set<BlockPos> getPositions() {
		if (isOwner)
			return be.getLinkedBlocks();
		else
			return Set.of();
	}

	@Override
	public void removePosition(BlockPos pos) {
		be.delink(pos, true);
		connectionList.refreshPositions();
		SecurityCraft.channel.sendToServer(new SyncSSSSettingsOnServer(be.getBlockPos(), DataType.REMOVE_POS, pos));
	}

	private Component getRecordingString(boolean recording) {
		return recording ? Utils.localize("gui.securitycraft:sonic_security_system.stop_recording") : Utils.localize("gui.securitycraft:sonic_security_system.start_recording");
	}

	private Component getPowerString(boolean on) {
		return on ? Utils.localize("gui.securitycraft:sonic_security_system.power.on") : Utils.localize("gui.securitycraft:sonic_security_system.power.off");
	}
}
