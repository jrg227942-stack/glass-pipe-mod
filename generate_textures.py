#!/usr/bin/env python3
"""
Texture generator for Glass Pipe Transport mod.
Run this script once to generate all required PNG textures.
Requires Pillow: pip install Pillow
"""

import os
import struct
import zlib

def create_png(width, height, pixels):
    """
    Creates a minimal valid PNG file from pixel data.
    pixels: list of (R, G, B, A) tuples, row by row
    """
    def png_chunk(chunk_type, data):
        chunk_len = len(data)
        chunk_data = chunk_type + data
        crc = zlib.crc32(chunk_data) & 0xFFFFFFFF
        return struct.pack('>I', chunk_len) + chunk_data + struct.pack('>I', crc)

    # PNG signature
    signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr = png_chunk(b'IHDR', ihdr_data)

    # IDAT chunk (image data)
    raw_data = b''
    for row in range(height):
        raw_data += b'\x00'  # filter type: None
        for col in range(width):
            r, g, b, a = pixels[row * width + col]
            raw_data += bytes([r, g, b, a])

    compressed = zlib.compress(raw_data, 9)
    idat = png_chunk(b'IDAT', compressed)

    # IEND chunk
    iend = png_chunk(b'IEND', b'')

    return signature + ihdr + idat + iend


def save_png(path, width, height, pixels):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(create_png(width, height, pixels))
    print(f"Created: {path}")


def make_glass_pipe_texture():
    """
    16x16 glass pipe texture: transparent glass with a subtle blue-white tint
    and a slightly glowing inner border.
    """
    pixels = []
    for y in range(16):
        for x in range(16):
            # Border pixels (outer 1px ring)
            if x == 0 or x == 15 or y == 0 or y == 15:
                pixels.append((180, 220, 255, 200))  # Light blue border
            # Inner glow ring
            elif x == 1 or x == 14 or y == 1 or y == 14:
                pixels.append((200, 235, 255, 160))  # Slightly lighter
            # Corner highlights
            elif (x in [2, 13] and y in [2, 13]):
                pixels.append((220, 245, 255, 120))
            # Center: mostly transparent with slight blue tint
            else:
                pixels.append((150, 200, 255, 40))   # Very transparent center
    return pixels


def make_filter_pipe_texture():
    """
    16x16 filter pipe texture: amber/orange tinted glass to distinguish from regular pipes.
    """
    pixels = []
    for y in range(16):
        for x in range(16):
            if x == 0 or x == 15 or y == 0 or y == 15:
                pixels.append((255, 180, 80, 200))   # Orange border
            elif x == 1 or x == 14 or y == 1 or y == 14:
                pixels.append((255, 200, 100, 160))
            elif (x in [2, 13] and y in [2, 13]):
                pixels.append((255, 220, 120, 120))
            else:
                pixels.append((255, 160, 60, 40))    # Transparent orange center
    return pixels


def make_speed_upgrade_texture():
    """
    16x16 speed upgrade icon: lightning bolt shape on blue background.
    """
    pixels = []
    # Lightning bolt pattern
    bolt = {
        (8,1),(9,1),(10,1),(11,1),
        (7,2),(8,2),(9,2),(10,2),
        (6,3),(7,3),(8,3),(9,3),
        (5,4),(6,4),(7,4),(8,4),
        (4,5),(5,5),(6,5),(7,5),(8,5),(9,5),(10,5),(11,5),
        (5,6),(6,6),(7,6),(8,6),(9,6),(10,6),(11,6),(12,6),
        (6,7),(7,7),(8,7),(9,7),(10,7),(11,7),
        (7,8),(8,8),(9,8),(10,8),
        (8,9),(9,9),(10,9),
        (9,10),(10,10),
        (10,11),(11,11),
        (11,12),(12,12),
        (12,13),(13,13),
    }
    for y in range(16):
        for x in range(16):
            if (x, y) in bolt:
                pixels.append((255, 255, 0, 255))    # Yellow bolt
            else:
                pixels.append((30, 80, 180, 255))    # Blue background
    return pixels


def make_sorting_upgrade_texture():
    """
    16x16 sorting upgrade icon: arrows pointing in multiple directions on green background.
    """
    pixels = []
    # Cross/arrow pattern
    arrows = set()
    # Horizontal arrow
    for x in range(2, 14):
        arrows.add((x, 7))
        arrows.add((x, 8))
    # Vertical arrow
    for y in range(2, 14):
        arrows.add((7, y))
        arrows.add((8, y))
    # Arrow heads
    for i in range(4):
        arrows.add((13+i if 13+i < 16 else 15, 7-i if 7-i >= 0 else 0))
        arrows.add((13+i if 13+i < 16 else 15, 8+i if 8+i < 16 else 15))

    for y in range(16):
        for x in range(16):
            if (x, y) in arrows:
                pixels.append((255, 255, 255, 255))  # White arrows
            else:
                pixels.append((30, 150, 60, 255))    # Green background
    return pixels


def make_pipe_wrench_texture():
    """
    16x16 pipe wrench icon: wrench shape on dark background.
    """
    pixels = []
    wrench = {
        (2,2),(3,2),(4,2),
        (2,3),(3,3),(4,3),(5,3),
        (3,4),(4,4),(5,4),(6,4),
        (4,5),(5,5),(6,5),(7,5),
        (5,6),(6,6),(7,6),(8,6),
        (6,7),(7,7),(8,7),(9,7),
        (7,8),(8,8),(9,8),(10,8),
        (8,9),(9,9),(10,9),(11,9),
        (9,10),(10,10),(11,10),(12,10),
        (10,11),(11,11),(12,11),(13,11),
        (11,12),(12,12),(13,12),
        (12,13),(13,13),
        # Wrench head
        (1,1),(2,1),(3,1),(4,1),(5,1),
        (1,2),(5,2),
        (1,3),(5,3),(6,3),
        (1,4),(2,4),(3,4),
    }
    for y in range(16):
        for x in range(16):
            if (x, y) in wrench:
                pixels.append((200, 200, 200, 255))  # Silver wrench
            else:
                pixels.append((40, 40, 40, 255))     # Dark background
    return pixels


def make_gui_texture():
    """
    176x166 GUI background texture for the filter pipe screen.
    """
    pixels = []
    for y in range(166):
        for x in range(176):
            # Outer border
            if x == 0 or x == 175 or y == 0 or y == 165:
                pixels.append((50, 50, 50, 255))
            # Inner border
            elif x == 1 or x == 174 or y == 1 or y == 164:
                pixels.append((180, 180, 180, 255))
            # Filter section background (top area)
            elif y < 70:
                pixels.append((100, 120, 140, 200))
            # Player inventory section
            else:
                pixels.append((130, 130, 130, 200))
    return pixels


def make_icon_texture():
    """
    64x64 mod icon.
    """
    pixels = []
    for y in range(64):
        for x in range(64):
            # Simple pipe cross icon
            cx, cy = 32, 32
            dx, dy = abs(x - cx), abs(y - cy)
            if dx <= 8 or dy <= 8:
                if dx <= 8 and dy <= 8:
                    pixels.append((200, 235, 255, 255))  # Center
                elif dx <= 8:
                    pixels.append((150, 200, 255, 255))  # Vertical arm
                else:
                    pixels.append((150, 200, 255, 255))  # Horizontal arm
            else:
                pixels.append((30, 60, 100, 255))        # Background
    return pixels


if __name__ == '__main__':
    base = "src/main/resources/assets/glass_pipe_transport"

    save_png(f"{base}/textures/block/glass_pipe.png", 16, 16, make_glass_pipe_texture())
    save_png(f"{base}/textures/block/filter_pipe.png", 16, 16, make_filter_pipe_texture())
    save_png(f"{base}/textures/item/speed_upgrade.png", 16, 16, make_speed_upgrade_texture())
    save_png(f"{base}/textures/item/sorting_upgrade.png", 16, 16, make_sorting_upgrade_texture())
    save_png(f"{base}/textures/item/pipe_wrench.png", 16, 16, make_pipe_wrench_texture())
    save_png(f"{base}/textures/gui/filter_pipe.png", 176, 166, make_gui_texture())
    save_png(f"{base}/icon.png", 64, 64, make_icon_texture())

    print("\nAll textures generated successfully!")
    print("You can now build the mod with: gradlew build")
