package com.mycodefu.werekitten.netty.codec;

import com.mycodefu.werekitten.network.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.CharsetUtil;

import java.util.List;

public class MessageDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) throws Exception {
        ByteBuf in = msg.content();
        MessageType type = MessageType.forCode(in.readByte());
        long timeStamp = in.readLong();
        switch (type) {
            case chat: {
                String text = getStringFromBuffer(in);
                out.add(new ChatMessage(timeStamp, text));
                break;
            }
            case join: {
                String id = getStringFromBuffer(in);
                out.add(new JoinMessage(timeStamp, id));
                break;
            }
            case pang: {
                out.add(new PangMessage(timeStamp, in.readLong()));
                break;
            }
            case idleLeft:
            case idleRight:
            case init:
            case jump:
            case moveLeft:
            case moveRight: {
                out.add(new XSyncMessage(type, timeStamp, ((double) in.readShort()) / 10));
                break;
            }
            default:
                out.add(new Message(type, timeStamp));
        }
    }

    private String getStringFromBuffer(ByteBuf in) {
        byte stringLength = in.readByte();
        byte[] bytes = new byte[stringLength];
        in.readBytes(bytes);
        return new String(bytes, CharsetUtil.UTF_8);
    }
}
