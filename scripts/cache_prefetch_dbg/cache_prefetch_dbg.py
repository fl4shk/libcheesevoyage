#!/usr/bin/env python3

def addr_to_word_idx(addr: int) -> int:
    return ((addr >> 2) & 0xfff)

def addr_to_set(addr: int) -> int:
    return ((addr >> 6) & 0xff)

def addr_to_tag(addr: int) -> int:
    return (addr >> 14)

for x in [
    #0x47f60, 0x47f98,
    ##0x47fd8,
    #0x47fdc,
    #0x47fbc,
    #0x47f7c
    0x4b2ab8,
    0xfffd70
]:
    print(
        hex(x),
        hex(addr_to_word_idx(x)), hex(addr_to_set(x)), hex(addr_to_tag(x))
    )
