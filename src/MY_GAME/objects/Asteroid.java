package MY_GAME.objects;

import lwjglutils.OGLBuffers;
import transforms.*;

public class Asteroid {
    // --- 1. SDÍLENÁ DATA PRO VŠECHNY ASTEROIDY (Zásadní pro výkon) ---
    // Vygenerujeme pouze 5 různých tvarů a všech 10 000 asteroidů si je bude sdílet.
    private static final int POCET_TYPU = 10;
    private static OGLBuffers[] sdileneTvary = null;

    private OGLBuffers buffers;

    // Vlastnosti
    private Vec3D position;
    private Vec3D velocity;
    private double scale;

    // Rotace
    private double rotX, rotY, rotZ;
    private double rotSpeedX, rotSpeedY, rotSpeedZ;

    public Asteroid(Vec3D startPozice, double startMeritko) {
        this.position = startPozice;

        // --- 2. EXPONENCIÁLNÍ VELIKOST ---
        // Math.pow(..., 3) zajistí, že padne většinou malé číslo, ale občas obrovské.
        // Tím vznikne spousta malých šutrů a pár opravdových gigantů.
        double nahodaVelikosti = Math.random();
        this.scale = startMeritko * (0.2 + 3.0 * Math.pow(nahodaVelikosti, 3));

        this.velocity = new Vec3D(
                (Math.random() * 0.1) - 0.05,
                (Math.random() * 0.1) - 0.05,
                (Math.random() * 0.2) - 0.1
        );

        this.rotX = Math.random() * Math.PI * 2;
        this.rotY = Math.random() * Math.PI * 2;
        this.rotZ = Math.random() * Math.PI * 2;

        this.rotSpeedX = (Math.random() * 0.02) - 0.01;
        this.rotSpeedY = (Math.random() * 0.02) - 0.01;
        this.rotSpeedZ = (Math.random() * 0.02) - 0.01;

        // Pokud to je první asteroid ve hře, vygenerujeme 5 sdílených modelů
        if (sdileneTvary == null) {
            inicializujTvary();
        }

        // Náhodně si "vylosujeme" jeden z 5 sdílených modelů
        this.buffers = sdileneTvary[(int)(Math.random() * POCET_TYPU)];
    }

    private static void inicializujTvary() {
        sdileneTvary = new OGLBuffers[POCET_TYPU];
        for (int i = 0; i < POCET_TYPU; i++) {
            sdileneTvary[i] = generujTvar();
        }
    }

    // --- 3. ČISTÁ PROCEDURÁLNÍ GENERACE ---
    // ... uvnitř třídy Asteroid (vylepšená verze z předchozí odpovědi) ...

    // --- 3. ČISTÁ PROCEDURÁLNÍ GENERACE (Verze "Smooth") ---
    private static OGLBuffers generujTvar() {
        // --- 1. ZVÝŠENÍ GEOMETRICKÉHO ROZLIŠENÍ pro hladší křivky ---
        // Zvýšili jsme počet řádků a sloupců ze 12 na 24.
        // Více polygonů = fyzicky hladší model.
        // (Díky statickému sdílení 5 modelů si to můžeme dovolit.)
        int radky = 15;
        int sloupce = 15;
        float zakladniPolomer = 0.5f;

        // Místo ArrayListů rovnou vytvoříme pole přesné velikosti (mnohem rychlejší)
        int pocetBodu = (radky + 1) * (sloupce + 1);
        float[] vertices = new float[pocetBodu * 6]; // 3 XYZ pro pozici + 3 XYZ pro normálu
        int[] indices = new int[radky * sloupce * 6];

        int vIndex = 0;
        for (int r = 0; r <= radky; r++) {
            double zenit = Math.PI * r / radky - Math.PI / 2.0;
            for (int s = 0; s <= sloupce; s++) {
                double azimut = 2.0 * Math.PI * s / sloupce;

                // --- 2. VÝPOČET ZÁKLADNÍ, SMOOTH NORMÁLY (na čisté kouli) ---
                // Tyto hodnoty reprezentují body na čisté, hladké kouli o poloměru 1.
                double bx = Math.cos(zenit) * Math.cos(azimut);
                double by = Math.sin(zenit);
                double bz = Math.cos(zenit) * Math.sin(azimut);

                // Vektor ukazující ven z HLADKÉ koule je naše normála, kterou normalizujeme.
                // normalized() zajistí, že délka vektoru je 1 (důležité pro shadery).
                Vec3D normalVektor = new Vec3D(bx, by, bz);

                // --- 3. APLIKACE HRBOLŮ na pozici, ale NORMÁLA zůstává hladká ---
                float hrbol = (float) (Math.random() * 0.2f); // Mírně menší hrboly pro smooth efekt
                float polomer = zakladniPolomer + hrbol;

                float x = (float) (bx * polomer);
                float y = (float) (by * polomer);
                float z = (float) (bz * polomer);

                // Zápis pozice
                vertices[vIndex++] = x;
                vertices[vIndex++] = y;
                vertices[vIndex++] = z;

                // Zápis normály (Používáme smooth normálu vypočítanou výše, NE pozici hrbolu)
                vertices[vIndex++] = (float)normalVektor.getX();
                vertices[vIndex++] = (float)normalVektor.getY();
                vertices[vIndex++] = (float)normalVektor.getZ();
            }
        }

        // Zbytek metody (generování indices a OGLBuffers) zůstává STEJNÝ jako v předchozí odpovědi.
        int iIndex = 0;
        for (int r = 0; r < radky; r++) {
            for (int s = 0; s < sloupce; s++) {
                int start = (r * (sloupce + 1)) + s;
                indices[iIndex++] = start;
                indices[iIndex++] = start + 1;
                indices[iIndex++] = start + sloupce + 1;
                indices[iIndex++] = start + 1;
                indices[iIndex++] = start + sloupce + 2;
                indices[iIndex++] = start + sloupce + 1;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3),
                new OGLBuffers.Attrib("inNormal", 3)
        };

        return new OGLBuffers(vertices, attributes, indices);
    }

    public void move() {
        position = position.add(velocity);
        rotX += rotSpeedX;
        rotY += rotSpeedY;
        rotZ += rotSpeedZ;
    }

    public Mat4 getModelMatrix() {
        return new Mat4Scale(scale)
                .mul(new Mat4RotX(rotX))
                .mul(new Mat4RotY(rotY))
                .mul(new Mat4RotZ(rotZ))
                .mul(new Mat4Transl(position));
    }

    // Obyčejné gettery a settery (zůstávají stejné)
    public OGLBuffers getBuffers() { return buffers; }
    public Vec3D getPosition() { return position; }
    public void setPosition(Vec3D newPosition) { this.position = newPosition; }
    public double getScale() { return scale; }
    public void setVelocity(Vec3D newSmerLetu) { this.velocity = newSmerLetu; }
}