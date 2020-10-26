package org.eclipse.mosaic.app.tutorial.communication;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.annotation.Nonnull;

public class RoundTripMessage extends V2xMessage {

    /**
     * The encoded message.
     */
    private final EncodedPayload encodedPayload;

    /**
     * Simple message taking a byte array as input for the payload.
     *
     * @param routing the {@link MessageRouting} for the message
     * @param payload the byte array
     */
    public RoundTripMessage(MessageRouting routing, final byte[] payload) {
        super(routing);
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            dataOutputStream.write(payload);
            this.encodedPayload = new EncodedPayload(byteArrayOutputStream.toByteArray(), byteArrayOutputStream.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Nonnull
    @Override
    public EncodedPayload getPayLoad() {
        return encodedPayload;
    }
}
