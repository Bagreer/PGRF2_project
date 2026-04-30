package MY_GAME.objects;

import lwjglutils.OGLBuffers;

public class Arrow {
    private final OGLBuffers buffers;

    public Arrow() {
        // Jednoduchá pyramida mířící ve směru osy X
        float[] vertices = {
                // Špička
                0.5f,  0.0f,  0.0f,
                // Základna (čtverec)
                -0.5f,  0.1f,  0.1f,
                -0.5f, -0.1f,  0.1f,
                -0.5f, -0.1f, -0.1f,
                -0.5f,  0.1f, -0.1f
        };

        int[] indices = {
                0, 1, 2,   0, 2, 3,   0, 3, 4,   0, 4, 1, // Boky
                1, 2, 3,   3, 4, 1                        // Podstava
        };

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3)
        };
        this.buffers = new OGLBuffers(vertices, attributes, indices);
    }

    public OGLBuffers getBuffers() { return buffers; }
}