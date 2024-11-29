package ru.fruzz.extazyy.misc.event.events.impl;

import net.minecraft.network.protocol.Packet;
import ru.fruzz.extazyy.misc.event.events.Event;

public class EventPacket extends Event {

    private Packet packet;

    private final PacketType packetType;

    public EventPacket(Packet packet, PacketType packetType) {
        this.packet = packet;
        this.packetType = packetType;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public boolean isReceivePacket() {
        return this.packetType == PacketType.RECEIVE;
    }

    public boolean isSendPacket() {
        return this.packetType == PacketType.SEND;
    }

    public enum PacketType {
        SEND, RECEIVE
    }
}
