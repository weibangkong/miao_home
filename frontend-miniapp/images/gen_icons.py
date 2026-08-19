"""Generate simple colored circle PNG icons for tab bar"""
import struct, zlib, os

def create_png(filename, r, g, b):
    width, height = 81, 81
    sig = b'\x89PNG\r\n\x1a\n'

    # IHDR
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data)
    ihdr = struct.pack('>I', 13) + b'IHDR' + ihdr_data + struct.pack('>I', ihdr_crc & 0xffffffff)

    # IDAT - circle on white background
    raw = b''
    cx, cy = width // 2, height // 2
    for y in range(height):
        raw += b'\x00'  # filter byte
        for x in range(width):
            dist = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if dist < 28:
                raw += bytes([r, g, b])
            elif dist < 33:
                alpha = max(0, min(1, (33 - dist) / 5))
                rr = int(r * alpha + 255 * (1 - alpha))
                gg = int(g * alpha + 255 * (1 - alpha))
                bb = int(b * alpha + 255 * (1 - alpha))
                raw += bytes([rr, gg, bb])
            else:
                raw += b'\xff\xff\xff'

    compressed = zlib.compress(raw)
    idat_crc = zlib.crc32(b'IDAT' + compressed)
    idat = struct.pack('>I', len(compressed)) + b'IDAT' + compressed + struct.pack('>I', idat_crc & 0xffffffff)

    # IEND
    iend_crc = zlib.crc32(b'IEND')
    iend = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', iend_crc & 0xffffffff)

    with open(filename, 'wb') as f:
        f.write(sig + ihdr + idat + iend)
    print(f'Created {filename}')

os.chdir(os.path.dirname(__file__))

create_png('tab-cat.png', 102, 126, 234)
create_png('tab-cat-active.png', 102, 126, 234)
create_png('tab-range.png', 180, 180, 180)
create_png('tab-range-active.png', 102, 126, 234)
create_png('tab-notify.png', 180, 180, 180)
create_png('tab-notify-active.png', 102, 126, 234)
create_png('tab-mine.png', 180, 180, 180)
create_png('tab-mine-active.png', 102, 126, 234)
print('All icons generated!')
