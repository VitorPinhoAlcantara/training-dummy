// Generates a 512x512 project logo for CurseForge (min 400x400, 1:1, PNG).
// Same theme as the in-game item icon (archery target / bullseye) but with a dark backing
// card and heavier supersampling since this is displayed much larger than a 16px item icon.
const zlib = require('zlib');
const fs = require('fs');
const path = require('path');

let CRC_TABLE = null;
function crc32(buf) {
    if (!CRC_TABLE) {
        CRC_TABLE = [];
        for (let n = 0; n < 256; n++) {
            let c = n;
            for (let k = 0; k < 8; k++) c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
            CRC_TABLE[n] = c >>> 0;
        }
    }
    let crc = 0xFFFFFFFF;
    for (let i = 0; i < buf.length; i++) crc = CRC_TABLE[(crc ^ buf[i]) & 0xFF] ^ (crc >>> 8);
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
            raw[offset++] = r; raw[offset++] = g; raw[offset++] = b; raw[offset++] = a;
        }
    }
    const idat = zlib.deflateSync(raw, { level: 9 });
    const ihdr = Buffer.alloc(13);
    ihdr.writeUInt32BE(width, 0);
    ihdr.writeUInt32BE(height, 4);
    ihdr[8] = 8; ihdr[9] = 6; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
    const sig = Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]);
    return Buffer.concat([sig, chunk('IHDR', ihdr), chunk('IDAT', idat), chunk('IEND', Buffer.alloc(0))]);
}

function dist(x, y, cx, cy) { return Math.hypot(x - cx, y - cy); }
function lerp(a, b, t) { return a + (b - a) * t; }
function mixColor(c1, c2, t) {
    return [Math.round(lerp(c1[0], c2[0], t)), Math.round(lerp(c1[1], c2[1], t)), Math.round(lerp(c1[2], c2[2], t)), 255];
}

const SIZE = 512;
const CX = SIZE / 2, CY = SIZE / 2;
const CARD_RADIUS = SIZE * 0.49;
const TARGET_RADIUS = SIZE * 0.40;

const BG_TOP = [46, 50, 61, 255];
const BG_BOTTOM = [24, 26, 33, 255];
const RED = [196, 43, 43, 255];
const RED_DARK = [156, 30, 30, 255];
const WHITE = [238, 233, 221, 255];
const OUTLINE = [26, 20, 16, 255];
const RING_EDGE = [214, 178, 96, 255];

function logoPixel(x, y) {
    const SS = 3;
    let rSum = 0, gSum = 0, bSum = 0, aSum = 0;
    for (let sy = 0; sy < SS; sy++) {
        for (let sx = 0; sx < SS; sx++) {
            const px = x + (sx + 0.5) / SS;
            const py = y + (sy + 0.5) / SS;
            const dCard = dist(px, py, CX, CY);

            let c;
            if (dCard > CARD_RADIUS) {
                c = [0, 0, 0, 0];
            } else {
                const dTarget = dist(px, py, CX, CY);
                const bgT = Math.min(1, py / SIZE);
                const bg = mixColor(BG_TOP, BG_BOTTOM, bgT);

                if (dTarget > TARGET_RADIUS + 6) {
                    c = bg;
                } else if (dTarget > TARGET_RADIUS) {
                    c = RING_EDGE;
                } else if (dTarget > TARGET_RADIUS * 0.78) {
                    c = WHITE;
                } else if (dTarget > TARGET_RADIUS * 0.52) {
                    c = dTarget > TARGET_RADIUS * 0.65 ? RED : RED_DARK;
                } else if (dTarget > TARGET_RADIUS * 0.27) {
                    c = WHITE;
                } else {
                    c = dTarget > TARGET_RADIUS * 0.13 ? RED : RED_DARK;
                }

                // thin outline strokes between rings for definition
                const ringBoundaries = [TARGET_RADIUS, TARGET_RADIUS * 0.78, TARGET_RADIUS * 0.52, TARGET_RADIUS * 0.27, TARGET_RADIUS * 0.13];
                for (const rb of ringBoundaries) {
                    if (Math.abs(dTarget - rb) < 1.6) {
                        c = OUTLINE;
                        break;
                    }
                }

                // card edge vignette ring
                if (dCard > CARD_RADIUS - 5) {
                    c = mixColor(c, [10, 10, 12, 255], (dCard - (CARD_RADIUS - 5)) / 5);
                }
            }

            rSum += c[0] * c[3]; gSum += c[1] * c[3]; bSum += c[2] * c[3]; aSum += c[3];
        }
    }
    const n = SS * SS;
    const a = Math.round(aSum / n);
    if (a === 0) return [0, 0, 0, 0];
    return [Math.round(rSum / aSum), Math.round(gSum / aSum), Math.round(bSum / aSum), a];
}

const outDir = path.resolve(__dirname, '..');
fs.writeFileSync(path.join(outDir, 'logo.png'), encodePng(SIZE, SIZE, logoPixel));
console.log('Wrote logo.png (512x512)');
