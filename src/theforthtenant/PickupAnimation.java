package theforthtenant;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.Timer;

/**
 * Pickup animation (drop-in replacement, same play(...) signature).
 *
 *   1. CHARGE  - item trembles and swells with a growing glow while motes get
 *                sucked into it, then it implodes.
 *   2. BURST   - white flash, light rays, two shockwave rings, star + dot sparks.
 *   3. FLIGHT  - a comet (spinning star core, glow, tapered trail, shed sparks)
 *                curves along a bezier path to the inventory.
 *   4. IMPACT  - ring pulses + a small burst + glow at the inventory.
 */
public final class PickupAnimation {

    // ── tuning ─────────────────────────────────────────────
    private static final int TICK_MS          = 16;

    private static final double CHARGE_MS     = 350;
    private static final double BURST_GAP_MS  = 250;   // pause between burst and launch
    private static final double FLIGHT_MS     = 750;
    private static final double IMPACT_MS     = 500;

    private static final int BURST_COUNT      = 70;
    private static final int IMPACT_COUNT     = 28;

    private static final Color GOLD       = new Color(255, 220, 100);
    private static final Color AMBER      = new Color(255, 170, 50);
    private static final Color WHITE_HOT  = new Color(255, 250, 225);
    private static final Color[] PALETTE  = { GOLD, AMBER, WHITE_HOT, GOLD };

    private PickupAnimation() {}

    /**
     * @param frame       host frame
     * @param itemIcon    the icon of the clicked item
     * @param startX/startY  item centre, in frame content-pane coords
     * @param endX/endY      target (inventory) centre, in the same coords
     */
    public static void play(JFrame frame, Icon itemIcon,
                            int startX, int startY,
                            int endX, int endY) {

        JLayeredPane layered = frame.getRootPane().getLayeredPane();

        ParticlePanel panel = new ParticlePanel(itemIcon, startX, startY, endX, endY);
        panel.setBounds(0, 0,
                frame.getContentPane().getWidth(),
                frame.getContentPane().getHeight());
        panel.setOpaque(false);
        layered.add(panel, JLayeredPane.POPUP_LAYER);
        layered.repaint();

        panel.start(layered);
    }

    // ── helpers ────────────────────────────────────────────

    private static double clamp01(double v) { return Math.max(0, Math.min(1, v)); }
    private static double easeOutCubic(double t) { double u = 1 - t; return 1 - u * u * u; }
    private static double easeInCubic(double t)  { return t * t * t; }
    private static double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    private static Color alpha(Color c, double a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(),
                (int) Math.round(255 * clamp01(a)));
    }

    // ── data ───────────────────────────────────────────────

    private static class Particle {
        double x, y, vx, vy;        // position (px), velocity (px/sec)
        double life, maxLife;       // seconds
        double size;
        double gravity;             // px/sec^2
        double drag;                // per-frame multiplier at 60fps
        double rot, spin;           // radians, radians/sec
        boolean star;
        Color color;
    }

    private static class Ring {
        double x, y, startMs, durMs, maxR, width;
        Color color;
    }

    private static class TrailPoint {
        double x, y, age;
    }

    // ── the animated layer ─────────────────────────────────

    private static class ParticlePanel extends JComponent {
        private final Icon itemIcon;
        private final double startX, startY, endX, endY;
        private final double ctrlX, ctrlY;     // bezier control point

        private final List<Particle>   particles = new ArrayList<>();
        private final List<Ring>       rings     = new ArrayList<>();
        private final List<TrailPoint> trail     = new ArrayList<>();
        private final Random rng = new Random();

        private final double burstAt  = CHARGE_MS;
        private final double flightAt = CHARGE_MS + BURST_GAP_MS;
        private final double impactAt = flightAt + FLIGHT_MS;
        private final double totalMs  = impactAt + IMPACT_MS;

        private long   startNanos, lastNanos;
        private double elapsed;               // ms
        private boolean burstDone, impactDone;

        private double cometX, cometY, cometRot;
        private boolean cometVisible;

        ParticlePanel(Icon itemIcon, int sx, int sy, int ex, int ey) {
            this.itemIcon = itemIcon;
            this.startX = sx; this.startY = sy;
            this.endX = ex;   this.endY = ey;

            // Control point: midpoint pushed perpendicular so the path arcs.
            double mx = (sx + ex) / 2.0, my = (sy + ey) / 2.0;
            double dx = ex - sx, dy = ey - sy;
            double len = Math.max(1, Math.hypot(dx, dy));
            double nx = -dy / len, ny = dx / len;
            if (ny > 0) { nx = -nx; ny = -ny; }          // bias the bow upward
            double bow = Math.min(180, len * 0.35) + 30;
            this.ctrlX = mx + nx * bow;
            this.ctrlY = my + ny * bow;
        }

        @Override public boolean contains(int x, int y) { return false; } // never block clicks

        void start(JLayeredPane layered) {
            startNanos = lastNanos = System.nanoTime();
            Timer timer = new Timer(TICK_MS, null);
            timer.addActionListener(e -> {
                long now = System.nanoTime();
                double dt = Math.min(0.05, (now - lastNanos) / 1e9);
                lastNanos = now;
                elapsed = (now - startNanos) / 1e6;

                if (elapsed >= totalMs && particles.isEmpty()) {
                    timer.stop();
                    layered.remove(this);
                    layered.repaint();
                    return;
                }
                update(dt);
                repaint();
            });
            timer.start();
        }

        // ── update ─────────────────────────────────────────

        private void update(double dt) {

            // CHARGE: motes get sucked into the item
            if (elapsed < burstAt) {
                double t = elapsed / CHARGE_MS;
                int n = 1 + (int) (t * 3);
                for (int i = 0; i < n; i++) {
                    double ang = rng.nextDouble() * Math.PI * 2;
                    double r = 45 + rng.nextDouble() * 45;
                    Particle p = new Particle();
                    p.x = startX + Math.cos(ang) * r;
                    p.y = startY + Math.sin(ang) * r;
                    p.life = p.maxLife = 0.22;
                    p.vx = -Math.cos(ang) * r / p.life;
                    p.vy = -Math.sin(ang) * r / p.life;
                    p.size = 2 + rng.nextDouble() * 3;
                    p.drag = 1;
                    p.color = rng.nextBoolean() ? GOLD : WHITE_HOT;
                    particles.add(p);
                }
            }

            // BURST (once)
            if (!burstDone && elapsed >= burstAt) {
                burstDone = true;
                spawnBurst();
            }

            // FLIGHT
            cometVisible = false;
            if (elapsed >= flightAt && elapsed < impactAt) {
                double t = (elapsed - flightAt) / FLIGHT_MS;
                double e = easeInOutCubic(clamp01(t));
                double u = 1 - e;
                cometX = u * u * startX + 2 * u * e * ctrlX + e * e * endX;
                cometY = u * u * startY + 2 * u * e * ctrlY + e * e * endY;
                cometRot += dt * 9;
                cometVisible = true;

                TrailPoint tp = new TrailPoint();
                tp.x = cometX; tp.y = cometY;
                trail.add(tp);

                // shed sparks behind the comet
                for (int i = 0; i < 2; i++) {
                    Particle p = new Particle();
                    p.x = cometX + (rng.nextDouble() - 0.5) * 6;
                    p.y = cometY + (rng.nextDouble() - 0.5) * 6;
                    p.vx = (rng.nextDouble() - 0.5) * 70;
                    p.vy = (rng.nextDouble() - 0.5) * 70 + 20;
                    p.gravity = 120;
                    p.drag = 0.96;
                    p.life = p.maxLife = 0.35 + rng.nextDouble() * 0.3;
                    p.size = 2 + rng.nextDouble() * 3;
                    p.star = rng.nextInt(4) == 0;
                    p.spin = (rng.nextDouble() - 0.5) * 10;
                    p.color = PALETTE[rng.nextInt(PALETTE.length)];
                    particles.add(p);
                }
            }

            // IMPACT (once)
            if (!impactDone && elapsed >= impactAt) {
                impactDone = true;
                spawnImpact();
            }

            // trail ageing
            for (Iterator<TrailPoint> it = trail.iterator(); it.hasNext();) {
                TrailPoint tp = it.next();
                tp.age += dt;
                if (tp.age > 0.4) it.remove();
            }

            // particle physics (time based)
            double frames = dt * 60.0;
            for (Iterator<Particle> it = particles.iterator(); it.hasNext();) {
                Particle p = it.next();
                p.life -= dt;
                if (p.life <= 0) { it.remove(); continue; }
                p.vy += p.gravity * dt;
                double d = Math.pow(p.drag == 0 ? 1 : p.drag, frames);
                p.vx *= d; p.vy *= d;
                p.x += p.vx * dt;
                p.y += p.vy * dt;
                p.rot += p.spin * dt;
            }

            rings.removeIf(r -> elapsed - r.startMs > r.durMs);
        }

        private void spawnBurst() {
            addRing(startX, startY, burstAt, 450, 85, 4, WHITE_HOT);
            addRing(startX, startY, burstAt + 60, 600, 140, 2.5, GOLD);

            for (int i = 0; i < BURST_COUNT; i++) {
                Particle p = new Particle();
                p.x = startX; p.y = startY;
                double ang = rng.nextDouble() * Math.PI * 2;
                double speed = 80 + rng.nextDouble() * 340;
                p.vx = Math.cos(ang) * speed;
                p.vy = Math.sin(ang) * speed - 60;
                p.gravity = 260;
                p.drag = 0.95;
                p.life = p.maxLife = 0.5 + rng.nextDouble() * 0.6;
                p.size = 3 + rng.nextDouble() * 7;
                p.star = rng.nextInt(3) == 0;
                p.spin = (rng.nextDouble() - 0.5) * 14;
                p.rot = rng.nextDouble() * Math.PI;
                p.color = PALETTE[rng.nextInt(PALETTE.length)];
                particles.add(p);
            }
        }

        private void spawnImpact() {
            addRing(endX, endY, impactAt, 400, 55, 3.5, WHITE_HOT);
            addRing(endX, endY, impactAt + 80, 480, 80, 2, GOLD);

            for (int i = 0; i < IMPACT_COUNT; i++) {
                Particle p = new Particle();
                p.x = endX; p.y = endY;
                double ang = rng.nextDouble() * Math.PI * 2;
                double speed = 60 + rng.nextDouble() * 200;
                p.vx = Math.cos(ang) * speed;
                p.vy = Math.sin(ang) * speed;
                p.gravity = 100;
                p.drag = 0.94;
                p.life = p.maxLife = 0.35 + rng.nextDouble() * 0.4;
                p.size = 2 + rng.nextDouble() * 5;
                p.star = rng.nextInt(3) == 0;
                p.spin = (rng.nextDouble() - 0.5) * 12;
                p.color = PALETTE[rng.nextInt(PALETTE.length)];
                particles.add(p);
            }
        }

        private void addRing(double x, double y, double startMs, double dur,
                             double maxR, double width, Color c) {
            Ring r = new Ring();
            r.x = x; r.y = y; r.startMs = startMs; r.durMs = dur;
            r.maxR = maxR; r.width = width; r.color = c;
            rings.add(r);
        }

        // ── painting ───────────────────────────────────────

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            paintCharge(g2);
            paintBurstFx(g2);
            paintRings(g2);
            paintTrail(g2);
            paintParticles(g2);
            paintComet(g2);
            paintImpactGlow(g2);

            g2.dispose();
        }

        /** Phase 1: shaking, swelling icon with a charging glow, then implode. */
        private void paintCharge(Graphics2D g2) {
            if (elapsed >= burstAt) return;
            double t = clamp01(elapsed / CHARGE_MS);

            glow(g2, startX, startY, 20 + 55 * t, GOLD, 0.15 + 0.7 * t);
            glow(g2, startX, startY, 10 + 25 * t, WHITE_HOT, 0.6 * t);

            if (itemIcon == null) return;

            double scale;
            if (t < 0.6) scale = 1 + 0.25 * easeOutCubic(t / 0.6);
            else         scale = 1.25 * (1 - easeInCubic((t - 0.6) / 0.4));

            double shake = 2.5 * t * (t < 0.6 ? 1 : 0.3);
            double jx = (rng.nextDouble() - 0.5) * 2 * shake;
            double jy = (rng.nextDouble() - 0.5) * 2 * shake;

            int iw = itemIcon.getIconWidth();
            int ih = itemIcon.getIconHeight();

            AffineTransform old = g2.getTransform();
            g2.translate(startX + jx, startY + jy);
            g2.scale(scale, scale);
            g2.translate(-iw / 2.0, -ih / 2.0);
            itemIcon.paintIcon(this, g2, 0, 0);
            g2.setTransform(old);
        }

        /** Phase 2: flash + radiating light rays. */
        private void paintBurstFx(Graphics2D g2) {
            double since = elapsed - burstAt;
            if (since < 0) return;

            // flash
            if (since < 220) {
                double p = since / 220.0;
                glow(g2, startX, startY, 70 + 50 * p, WHITE_HOT, 1 - p);
            }

            // light rays
            if (since < 400) {
                double p = since / 400.0;
                double e = easeOutCubic(p);
                int rays = 14;
                g2.setStroke(new BasicStroke((float) (3.5 * (1 - p) + 0.5f),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < rays; i++) {
                    double ang = i * (Math.PI * 2 / rays) + 0.2;
                    double r1 = 14 + 40 * e;
                    double r0 = 8 + 30 * e * e;
                    double len = (i % 2 == 0) ? 1.0 : 0.65;
                    g2.setColor(alpha(i % 2 == 0 ? WHITE_HOT : GOLD, (1 - p) * 0.9));
                    g2.draw(new Line2D.Double(
                            startX + Math.cos(ang) * r0,
                            startY + Math.sin(ang) * r0,
                            startX + Math.cos(ang) * (r0 + (r1 - r0 + 40 * e) * len),
                            startY + Math.sin(ang) * (r0 + (r1 - r0 + 40 * e) * len)));
                }
            }
        }

        private void paintRings(Graphics2D g2) {
            for (Ring r : rings) {
                double p = (elapsed - r.startMs) / r.durMs;
                if (p < 0 || p > 1) continue;
                double e = easeOutCubic(p);
                double radius = r.maxR * e;
                g2.setStroke(new BasicStroke((float) Math.max(0.5, r.width * (1 - p))));
                g2.setColor(alpha(r.color, (1 - p) * 0.9));
                g2.draw(new Ellipse2D.Double(r.x - radius, r.y - radius, radius * 2, radius * 2));
            }
        }

        /** Tapered glowing comet tail. */
        private void paintTrail(Graphics2D g2) {
            if (trail.size() < 2) return;
            for (int i = 1; i < trail.size(); i++) {
                TrailPoint a = trail.get(i - 1);
                TrailPoint b = trail.get(i);
                double f = 1 - clamp01(b.age / 0.4);      // 1 = fresh, 0 = old

                g2.setStroke(new BasicStroke((float) (2 + 12 * f),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(alpha(AMBER, f * 0.35));
                g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));

                g2.setStroke(new BasicStroke((float) (1 + 5 * f),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(alpha(WHITE_HOT, f * 0.85));
                g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));
            }
        }

        private void paintParticles(Graphics2D g2) {
            for (Particle p : particles) {
                double f = clamp01(p.life / p.maxLife);   // 1 -> 0
                double size = p.size * (0.3 + 0.7 * f);
                double a = Math.min(1, f * 1.6);

                glow(g2, p.x, p.y, (float) (size * 2.2), p.color, 0.35 * a);

                g2.setColor(alpha(p.color, a));
                if (p.star) {
                    g2.fill(star(p.x, p.y, size * 1.4, size * 0.35, p.rot));
                } else {
                    g2.fill(new Ellipse2D.Double(p.x - size / 2, p.y - size / 2, size, size));
                }
            }
        }

        private void paintComet(Graphics2D g2) {
            if (!cometVisible) return;
            // little pulse
            double pulse = 1 + 0.12 * Math.sin(elapsed / 40.0);

            glow(g2, cometX, cometY, (float) (30 * pulse), AMBER, 0.55);
            glow(g2, cometX, cometY, (float) (16 * pulse), GOLD, 0.9);

            g2.setColor(alpha(WHITE_HOT, 0.95));
            g2.fill(star(cometX, cometY, 16 * pulse, 3.5, cometRot));
            g2.fill(star(cometX, cometY, 10 * pulse, 2.5, -cometRot * 1.4 + 0.8));

            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(cometX - 4, cometY - 4, 8, 8));
        }

        /** Soft glow lingering at the inventory after impact. */
        private void paintImpactGlow(Graphics2D g2) {
            double since = elapsed - impactAt;
            if (since < 0 || since > IMPACT_MS) return;
            double p = since / IMPACT_MS;

            if (since < 200) {
                glow(g2, endX, endY, 45 * (float) (1 - 0.3 * (since / 200.0)),
                        WHITE_HOT, 1 - since / 200.0);
            }
            glow(g2, endX, endY, (float) (28 + 22 * p), GOLD, 0.7 * (1 - p));
        }

        // ── drawing utilities ──────────────────────────────

        private void glow(Graphics2D g2, double x, double y, double radius,
                          Color c, double alphaMul) {
            if (alphaMul <= 0.01) return;
            float r = (float) Math.max(1.0, radius);
            Color inner = alpha(c, (c.getAlpha() / 255.0) * alphaMul);
            Color outer = alpha(c, 0);
            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Double(x, y), r,
                    new float[] { 0f, 1f },
                    new Color[] { inner, outer }));
            g2.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }

        /** 4-point sparkle star centred on (cx, cy). */
        private Path2D star(double cx, double cy, double outer, double inner, double rot) {
            Path2D.Double path = new Path2D.Double();
            for (int i = 0; i < 8; i++) {
                double r = (i % 2 == 0) ? outer : inner;
                double a = rot + i * Math.PI / 4;
                double px = cx + Math.cos(a) * r;
                double py = cy + Math.sin(a) * r;
                if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
            }
            path.closePath();
            return path;
        }
    }
}