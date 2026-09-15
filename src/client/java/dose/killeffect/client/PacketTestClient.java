package dose.killeffect.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.text.Text;

/**
 * Small client-side packet replay harness for testing a server that you control.
 *
 * <p>The tool never fabricates unknown custom payloads. Instead it arms a one-shot
 * capture for an outgoing GUI/custom packet, stores that exact packet instance,
 * and can then replay it a bounded number of times. This is useful for checking
 * replay/idempotency bugs in custom GUI systems without knowing their payload
 * codec ahead of time.</p>
 */
public final class PacketTestClient {
    private static final int DEFAULT_REPLAY_COUNT = 10;
    private static final int MAX_REPLAY_COUNT = 100;

    private static CaptureKind armedKind;
    private static Packet<?> capturedPacket;
    private static String capturedDescription;
    private static boolean replaying;

    private PacketTestClient() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        ClientCommandManager.literal("packettest")
                                .executes(ctx -> {
                                    printHelp();
                                    return 1;
                                })
                                .then(ClientCommandManager.literal("help").executes(ctx -> {
                                    printHelp();
                                    return 1;
                                }))
                                .then(ClientCommandManager.literal("status").executes(ctx -> {
                                    printStatus();
                                    return 1;
                                }))
                                .then(ClientCommandManager.literal("arm")
                                        .then(ClientCommandManager.literal("any").executes(ctx -> arm(CaptureKind.ANY)))
                                        .then(ClientCommandManager.literal("click").executes(ctx -> arm(CaptureKind.CLICK)))
                                        .then(ClientCommandManager.literal("button").executes(ctx -> arm(CaptureKind.BUTTON)))
                                        .then(ClientCommandManager.literal("custom").executes(ctx -> arm(CaptureKind.CUSTOM))))
                                .then(ClientCommandManager.literal("replay")
                                        .executes(ctx -> replay(DEFAULT_REPLAY_COUNT))
                                        .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1, MAX_REPLAY_COUNT))
                                                .executes(ctx -> replay(IntegerArgumentType.getInteger(ctx, "count")))))
                                .then(ClientCommandManager.literal("clear").executes(ctx -> {
                                    clear();
                                    return 1;
                                }))
                )
        );
    }

    /**
     * Called by the client-network-handler mixin before an outgoing packet is sent.
     */
    public static void onOutgoingPacket(Packet<?> packet) {
        if (packet == null || replaying || armedKind == null) {
            return;
        }

        CaptureKind requested = armedKind;
        if (!requested.matches(packet)) {
            return;
        }

        capturedPacket = packet;
        capturedDescription = describe(packet);
        armedKind = null;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.execute(() -> send("§a[PacketTest] 캡처 완료: §f" + capturedDescription));
        }
    }

    private static int arm(CaptureKind kind) {
        armedKind = kind;
        capturedPacket = null;
        capturedDescription = null;
        send("§e[PacketTest] 다음 " + kind.displayName + " 패킷 1개를 캡처합니다. 이제 테스트할 UI 동작을 한 번 실행하세요.");
        return 1;
    }

    private static int replay(int count) {
        if (capturedPacket == null) {
            send("§c[PacketTest] 저장된 패킷이 없습니다. 먼저 /packettest arm <any|click|button|custom> 을 실행하세요.");
            return 0;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null) {
            send("§c[PacketTest] 서버에 연결되어 있지 않습니다.");
            return 0;
        }

        replaying = true;
        int sent = 0;
        try {
            for (int i = 0; i < count; i++) {
                client.getNetworkHandler().sendPacket(capturedPacket);
                sent++;
            }
        } catch (RuntimeException e) {
            send("§c[PacketTest] 재전송 중 클라이언트 예외: " + e.getClass().getSimpleName() + ": " + safeMessage(e));
            return 0;
        } finally {
            replaying = false;
        }

        send("§a[PacketTest] 동일 패킷을 " + sent + "회 재전송했습니다: §f" + capturedDescription);
        return 1;
    }

    private static void clear() {
        armedKind = null;
        capturedPacket = null;
        capturedDescription = null;
        send("§7[PacketTest] 캡처 상태를 초기화했습니다.");
    }

    private static void printStatus() {
        String armed = armedKind == null ? "없음" : armedKind.displayName;
        String captured = capturedDescription == null ? "없음" : capturedDescription;
        send("§b[PacketTest] armed=§f" + armed + "§b, captured=§f" + captured);
    }

    private static void printHelp() {
        send("§b[PacketTest] 본인이 관리하는 테스트 서버에서만 사용하세요.");
        send("§f/packettest arm click §7- 다음 인벤토리 클릭 패킷 캡처");
        send("§f/packettest arm button §7- 다음 ScreenHandler 버튼 패킷 캡처");
        send("§f/packettest arm custom §7- 다음 Fabric/커스텀 payload 패킷 캡처");
        send("§f/packettest arm any §7- 위 3종 중 다음 패킷 캡처");
        send("§f/packettest replay [1-100] §7- 캡처한 동일 패킷 그대로 재전송 (기본 10회)");
        send("§f/packettest status §7- 현재 캡처 상태 확인");
        send("§f/packettest clear §7- 캡처 초기화");
        send("§8예: /packettest arm click → 수리 재료 Shift+클릭 1회 → GUI 닫기 → /packettest replay 20");
    }

    private static String describe(Packet<?> packet) {
        if (packet instanceof ClickSlotC2SPacket click) {
            return "ClickSlotC2SPacket{syncId=" + click.getSyncId()
                    + ", revision=" + click.getRevision()
                    + ", slot=" + click.getSlot()
                    + ", button=" + click.getButton()
                    + ", action=" + click.getActionType() + "}";
        }

        if (packet instanceof ButtonClickC2SPacket button) {
            return "ButtonClickC2SPacket{syncId=" + button.syncId()
                    + ", buttonId=" + button.buttonId() + "}";
        }

        if (packet instanceof CustomPayloadC2SPacket custom) {
            return "CustomPayloadC2SPacket{id=" + custom.payload().getId().id() + "}";
        }

        return packet.getClass().getSimpleName();
    }

    private static String safeMessage(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? "(no message)" : message;
    }

    private static void send(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal(message), false);
        }
    }

    private enum CaptureKind {
        ANY("지원 패킷"),
        CLICK("ClickSlotC2SPacket"),
        BUTTON("ButtonClickC2SPacket"),
        CUSTOM("CustomPayloadC2SPacket");

        private final String displayName;

        CaptureKind(String displayName) {
            this.displayName = displayName;
        }

        private boolean matches(Packet<?> packet) {
            return switch (this) {
                case ANY -> packet instanceof ClickSlotC2SPacket
                        || packet instanceof ButtonClickC2SPacket
                        || packet instanceof CustomPayloadC2SPacket;
                case CLICK -> packet instanceof ClickSlotC2SPacket;
                case BUTTON -> packet instanceof ButtonClickC2SPacket;
                case CUSTOM -> packet instanceof CustomPayloadC2SPacket;
            };
        }
    }
}
