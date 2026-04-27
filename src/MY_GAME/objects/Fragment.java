package MY_GAME.objects;

import lwjglutils.OGLBuffers;
import transforms.*;

public class Fragment {
    private Vec3D pozice;
    private Vec3D smerLetu;
    private double meritko;

    private double rotX, rotY, rotZ;
    private double rychlostRotX, rychlostRotY, rychlostRotZ;

    private int zivotnost; // Za jak dlouho úlomek zmizí (ve snímcích)
    private OGLBuffers buffers; // Sdílený model

    public Fragment(Vec3D startPozice, double puvodniMeritko, OGLBuffers puvodniBuffer) {
        this.pozice = startPozice;
        // Úlomky budou zhruba třetinové oproti původnímu šutru
        this.meritko = puvodniMeritko * (0.2 + Math.random() * 0.3);
        this.buffers = puvodniBuffer;

        // Životnost cca 30 až 60 snímků (půl až jedna vteřina při 60 FPS)
        this.zivotnost = 30 + (int)(Math.random() * 30);

        // Exploze: Náhodný směr do všech stran, docela velká rychlost
        this.smerLetu = new Vec3D(
                (Math.random() - 0.5),
                (Math.random() - 0.5),
                (Math.random() - 0.5)
        ).mul(0.3 + Math.random() * 0.5);

        // Divoká rotace
        this.rotX = Math.random() * Math.PI;
        this.rotY = Math.random() * Math.PI;
        this.rotZ = Math.random() * Math.PI;
        this.rychlostRotX = (Math.random() - 0.5) * 0.3;
        this.rychlostRotY = (Math.random() - 0.5) * 0.3;
        this.rychlostRotZ = (Math.random() - 0.5) * 0.3;
    }

    /**
     * moves and returns true if fragments live exceeded limit
     * @return boolean
     */
    public boolean moveAndCheck() {
        pozice = pozice.add(smerLetu);
        rotX += rychlostRotX;
        rotY += rychlostRotY;
        rotZ += rychlostRotZ;

        // Zpomalování (tření o vesmírný prach)
        smerLetu = smerLetu.mul(0.95);

        zivotnost--;
        return zivotnost <= 0;
    }

    public Mat4 getModelMatrix() {
        return new Mat4Scale(meritko)
                .mul(new Mat4RotX(rotX))
                .mul(new Mat4RotY(rotY))
                .mul(new Mat4RotZ(rotZ))
                .mul(new Mat4Transl(pozice));
    }

    public OGLBuffers getBuffers() {
        return buffers;
    }
}