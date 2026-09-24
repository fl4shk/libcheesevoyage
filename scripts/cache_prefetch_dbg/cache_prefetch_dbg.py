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
    #0x4b2ab8,
    #0xfffd70
    #0xfffd74
    #0x4b2d00
    #0x4b2d40,
    #0x4b2d80,
    #0x4b3080,
    #0x100ac5c,
    #0x3fad00
    0x7150
]:
    print(
        f"{hex(x)} "
        + f"wordIdx:{hex(addr_to_word_idx(x))} "
        + f"set:{hex(addr_to_set(x))} " 
        + f"tag:{hex(addr_to_tag(x))}"
    )
