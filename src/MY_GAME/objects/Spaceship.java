package MY_GAME.objects;

import lwjglutils.OGLBuffers;

public class Spaceship {

    // Zde si třída bude držet svá vlastní data pro grafickou kartu
    private OGLBuffers buffers;

    // Konstruktor - zavolá se automaticky, když vytvoříš novou kostku (new CubeModel())
    public void CubeModel() {
        createBuffers();
    }

    // Tady jsi vložil svůj kód z My_Game. Je teď krásně schovaný.
    // Tuto metodu si zavolá My_Game, když bude chtít kostku vykreslit

    void createBuffers() {
        // Nastavení rozměrů lodi (od středu ke kraji)
        float w = 0.5f;  // Šířka (celkově bude 1.0)
        float h = 0.1f;  // Výška - hezky placaté (celkově 0.2)
        float d = 1.0f;  // Délka - dlouhý čumák (celkově 2.0)

        float[] shipVertices = {
                // Z- strana (zadní trysky)
                w, -h, -d,    0, 0, -1,
                -w, -h, -d,    0, 0, -1,
                w,  h, -d,    0, 0, -1,
                -w,  h, -d,    0, 0, -1,
                // Z+ strana (přední čumák)
                w, -h,  d,    0, 0,  1,
                -w, -h,  d,    0, 0,  1,
                w,  h,  d,    0, 0,  1,
                -w,  h,  d,    0, 0,  1,
                // X+ strana (pravé křídlo/bok)
                w,  h, -d,    1, 0,  0,
                w, -h, -d,    1, 0,  0,
                w,  h,  d,    1, 0,  0,
                w, -h,  d,    1, 0,  0,
                // X- strana (levé křídlo/bok)
                -w,  h, -d,   -1, 0,  0,
                -w, -h, -d,   -1, 0,  0,
                -w,  h,  d,   -1, 0,  0,
                -w, -h,  d,   -1, 0,  0,
                // Y+ strana (střecha)
                w,  h, -d,    0, 1,  0,
                -w,  h, -d,    0, 1,  0,
                w,  h,  d,    0, 1,  0,
                -w,  h,  d,    0, 1,  0,
                // Y- strana (břicho)
                w, -h, -d,    0,-1,  0,
                -w, -h, -d,    0,-1,  0,
                w, -h,  d,    0,-1,  0,
                -w, -h,  d,    0,-1,  0
        };

        int[] indexBufferData = new int[36];
        for (int i = 0; i<6; i++){
            indexBufferData[i*6] = i*4;
            indexBufferData[i*6 + 1] = i*4 + 1;
            indexBufferData[i*6 + 2] = i*4 + 2;
            indexBufferData[i*6 + 3] = i*4 + 1;
            indexBufferData[i*6 + 4] = i*4 + 2;
            indexBufferData[i*6 + 5] = i*4 + 3;
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3),
                new OGLBuffers.Attrib("inNormal", 3)
        };

        // Nezapomeň tady předat to nové pole shipVertices!
        buffers = new OGLBuffers(shipVertices, attributes, indexBufferData);
    }


    public OGLBuffers getBuffers() {
        return buffers;
    }
}