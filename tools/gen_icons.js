// One-off pixel-art generator for item icons. Run with `node gen_icons.js`.
// No external deps - hand-rolled PNG encoder using Node's built-in zlib for the IDAT deflate.
const zlib = require('zlib');
const fs = require('fs');
const path = require('path');

let CRC_TABLE = null;
function crc32(buf) {
    if (!CRC_TABLE) {
        CRC_TABLE = [];
        for (let n = 0; n < 256; n++) {
            let c = n;
            for (let k = 0; k < 8; k++) {
                c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
            }
            CRC_TABLE[n] = c >>> 0;
        }
    }
    let crc = 0xFFFFFFFF;
    for (let i = 0; i < buf.length; i++) {
        crc = CRC_TABLE[(crc ^ buf[i]) & 0xFF] ^ (crc >>> 8);
    }
    return (crc ^ 0xFFFFFFFF) >>> 0;
}

function chunk(type, data) {
    const typeBuf = Buffer.from(type, 'ascii');
    const lenBuf = Buffer.alloc(4);
    lenBuf.writeUInt32BE(data.length, 0);
    const crcBuf = Buffer.alloc(4);
    crcBuf.writeUInt32BE(crc32(Buffer.concat([typeBuf, data])), 0);
    return Buffer.concat([lenBuf, typeBuf, data, crcBuf]);
}

function encodePng(width, height, getPixel) {
    const raw = Buffer.alloc((width * 4 + 1) * height);
    let offset = 0;
    for (let y = 0; y < height; y++) {
        raw[offset++] = 0;
        for (let x = 0; x < width; x++) {
            const [r, g, b, a] = getPixel(x, y);
            raw[offset++] = r;
            raw[offset++] = g;
            raw[offset++] = b;
            raw[offset++] = a;
        }
    }
    const idat = zlib.deflateSync(raw, { level: 9 });
    const ihdr = Buffer.alloc(13);
    ihdr.writeUInt32BE(width, 0);
    ihdr.writeUInt32BE(height, 4);
    ihdr[8] = 8;
    ihdr[9] = 6;
    ihdr[10] = 0;
    ihdr[11] = 0;
    ihdr[12] = 0;
    const sig = Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]);
    return Buffer.concat([sig, chunk('IHDR', ihdr), chunk('IDAT', idat), chunk('IEND', Buffer.alloc(0))]);
}

function dist(x, y, cx, cy) {
    return Math.hypot(x - cx, y - cy);
}

function inEllipse(x, y, cx, cy, rx, ry) {
    const dx = (x - cx) / rx, dy = (y - cy) / ry;
    return dx * dx + dy * dy;
}

function capsuleDist(px, py, x1, y1, x2, y2) {
    const dx = x2 - x1, dy = y2 - y1;
    const lenSq = dx * dx + dy * dy;
    let t = lenSq === 0 ? 0 : ((px - x1) * dx + (py - y1) * dy) / lenSq;
    t = Math.max(0, Math.min(1, t));
    const cx = x1 + t * dx, cy = y1 + t * dy;
    return dist(px, py, cx, cy);
}

const TRANSPARENT = [0, 0, 0, 0];

// --- Dummy spawner icon: archery target / bullseye, sampled at 4x4 subpixels per pixel for
// smooth-ish edges at 16x16.
function targetPixel(x, y) {
    const cx = 7.5, cy = 7.5;
    const RED = [178, 34, 34, 255];
    const WHITE = [237, 233, 222, 255];
    const OUTLINE = [40, 30, 20, 255];
    const SS = 4;
    let rSum = 0, gSum = 0, bSum = 0, aSum = 0;
    for (let sy = 0; sy < SS; sy++) {
        for (let sx = 0; sx < SS; sx++) {
            const px = x + (sx + 0.5) / SS;
            const py = y + (sy + 0.5) / SS;
            const d = dist(px, py, cx, cy);
            let c;
            if (d > 7.4) c = TRANSPARENT;
            else if (d > 6.6) c = OUTLINE;
            else if (d > 5.0) c = WHITE;
            else if (d > 3.4) c = RED;
            else if (d > 1.8) c = WHITE;
            else c = RED;
            rSum += c[0] * c[3]; gSum += c[1] * c[3]; bSum += c[2] * c[3]; aSum += c[3];
        }
    }
    const n = SS * SS;
    const a = Math.round(aSum / n);
    if (a === 0) return TRANSPARENT;
    return [Math.round(rSum / aSum), Math.round(gSum / aSum), Math.round(bSum / aSum), a];
}

// --- Lure bait icon: a drumstick (cream bone + brown meat) with a couple of red "irresistible" accents.
function baitPixel(x, y) {
    const SS = 4;
    const BONE = [235, 222, 190, 255];
    const BONE_OUTLINE = [120, 100, 70, 255];
    const MEAT = [134, 82, 40, 255];
    const MEAT_HI = [176, 118, 64, 255];
    const MEAT_OUTLINE = [58, 32, 14, 255];
    const ACCENT = [214, 32, 32, 255];

    let rSum = 0, gSum = 0, bSum = 0, aSum = 0;
    for (let sy = 0; sy < SS; sy++) {
        for (let sx = 0; sx < SS; sx++) {
            const px = x + (sx + 0.5) / SS;
            const py = y + (sy + 0.5) / SS;

            let c = TRANSPARENT;

            const boneD = capsuleDist(px, py, 2.5, 13.5, 7.0, 8.5);
            if (boneD <= 1.35) c = BONE;
            else if (boneD <= 1.9) c = BONE_OUTLINE;

            const meatE = inEllipse(px, py, 10.2, 6.0, 4.3, 3.6);
            if (meatE <= 1.0) {
                c = MEAT;
                const hiE = inEllipse(px, py, 9.0, 4.9, 2.1, 1.5);
                if (hiE <= 1.0) c = MEAT_HI;
            } else if (meatE <= 1.35) {
                c = MEAT_OUTLINE;
            }

            const accent1 = dist(px, py, 13.2, 3.6);
            const accent2 = dist(px, py, 12.0, 8.0);
            if (accent1 <= 0.9 || accent2 <= 0.8) c = ACCENT;

            rSum += c[0] * c[3]; gSum += c[1] * c[3]; bSum += c[2] * c[3]; aSum += c[3];
        }
    }
    const n = SS * SS;
    const a = Math.round(aSum / n);
    if (a === 0) return TRANSPARENT;
    return [Math.round(rSum / aSum), Math.round(gSum / aSum), Math.round(bSum / aSum), a];
}

const outDir = path.resolve(__dirname, '..', 'src', 'main', 'resources', 'assets', 'trainingdummy', 'textures', 'item');
fs.mkdirSync(outDir, { recursive: true });
fs.writeFileSync(path.join(outDir, 'dummy_spawner.png'), encodePng(16, 16, targetPixel));
fs.writeFileSync(path.join(outDir, 'lure_bait.png'), encodePng(16, 16, baitPixel));
console.log('Wrote icons to', outDir);
