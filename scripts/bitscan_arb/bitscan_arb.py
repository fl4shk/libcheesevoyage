#!/usr/bin/env python3

from misc_util import *
#--------
def to_ubits(arg: int, width: int):
	#if arg >= 0:
	#temp_arg = arg
	#ret = list(reversed(bin(temp_arg)[2:]))
	ret = list(reversed(bin(arg)[2:]))
	temp_ret_len = len(ret)
	for i in range(temp_ret_len, width):
		ret.append(0)
	#ret = list(reversed(ret))
	#else: # if arg < 0:
	#	# two's complement inverse
	#	temp_arg = (abs(arg) ^ ((1 << width) - 1)) + 1
	#	ret = list(reversed(bin(temp_arg)[2:]))
	#print(ret)
	for i in range(temp_ret_len):
		ret[i] = int(ret[i])
	return ret

def from_ubits(arg: list):
	temp_arg = int("0b" + "".join(
		list(reversed([str(bit) for bit in arg]))),
	2)
	#if arg[-1] == 0:	# non-negative
	#if arg[-1] == 1:	# negative
	#	#temp_arg = -((temp_arg ^ ((1 << len(arg)) - 1)) + 1)
	#	#temp_arg = ((temp_arg ^ ((1 << len(arg)) - 1)) + 1)
	return temp_arg

def dbg_tofrom_ubits(width: int):
	for i in range(2 ** width):
		a = to_ubits(i, width)
		#if a[-1] == 0:
		#	temp_i = i
		#else: # if a[-1] == 1:
		#	#temp_i = -((i ^ ((1 << width) - 1)) + 1)
		#	temp_i = ((i ^ ((1 << width) - 1)) + 1)
		print(
			#temp_i,
			i,
			list(reversed(a)), from_ubits(a)
		)
#dbg_tofrom_ubits(3)

# ok, maybe I had the wrong expression.  In anycase, it's concat x,x.
# bitscan using the previous 1-hot as the power of two input, then
# un-concat the vector and bitwise or the upper/lower parts.

# the bitscan starts in the middle of the request vector and if nothing
# higher priority comes up, the carry propagates to the concat'd portion
# which represents the lower priority bits.

# x = {req, req}; // dense vector of requests, concat'd.  eg, 01100110
#     t = x & (~x + last_one_hot); // bitscan
#     next_one_hot = t[2*N-1:N] | t[N-1:0]; // handle wrap.

# IIRC, last_one_hot always needs to be a one-hot, even if no requests are
# being made.  IIRC this can be done by augmenting x as {1,x,x} and then
# setting grant to the expression above for next_one_hot.  and last_one_hot
# to that bitwise or this added msb that only comes into play for the all
# zeros case.

# x = {1,req,req};
# x1h = x & {~x + last_grant};
# output_grant = x1h[2*N-1:N] | x1h[N-1:0];
# last_grant = output_grant | x1h[2*N];
# // untested

# req = 0110
# lg = 0001
# x & {~x + lg} = 101100110 & (010011001 + 1) = 101100110 & 010011010 = 000000010.
# 
# req = 0011
# lg = 0100
# x & {~x + lg} = 100110011 & (011001100 + 1) = 100110011 & 011010000 = 000010000.
# og = 0001 | 0000 = 0001
# 
# // ok, I think the saved value for "last grant" needs to be rotated one place
# // otherwise the same requester will be used until exhausted.
# 
# req = 0000
# lg = 0001
# x & {~x + lg} = 100000000 & (011111111 + 1) = 100000000 & 100000000
# og =0, next lg = 0000 | 0000 | 1 = 0001

def leading_one(lst: list):
	for i in reversed(range(len(lst))):
		if lst[i] == 1:
			return i
	else:
		return None

def is_one_hot(lg: list):
	found = False
	#for i in reversed(range(len(lg))):
	#for gnt in reversed(lg):
	for gnt in lg:
		if gnt == 1:
			if found:
				return False
			found = True

	return True

def calc_next_gnt(req: list, last_gnt: list):
	if len(req) != len(last_gnt):
		raise TypeError(psconcat(
			"len(req):", len(req), " ",
			"len(last_gnt):", len(last_gnt),
		))
	#x = from_ubits(req + req)
	#x = list(reversed(req)) + list(reversed(req))
	x = req + req
	last_gnt_uint = from_ubits(last_gnt)
	#if last_gnt_uint == 0:
	#	#temp_last_gnt = last_gnt[:-1] + [1]
	#	#x = [1] + x + x
	#	#x = [1] + x
	#	#print(psconcat("testificate: ", x, " ", x + [1]))
	#	x = x + [1]
	#	#x = [1] + x
	x_uint = from_ubits(x)
	x1h_uint = x_uint & ((~x_uint) + last_gnt_uint) # bitscan
	#x2h_uint = x_uint & ((~from_ubits(x[:-1])) + last_gnt_uint) # bitscan
	x1h = to_ubits(x1h_uint, len(x))
	#x2h = to_ubits(x2h_uint, len(x))
	if last_gnt_uint != 0:
		outp_gnt_uint = (
			from_ubits(x1h[len(req):2 * len(req)])
			| from_ubits(x1h[:len(req)])
		)
	elif x_uint != 0:
		outp_gnt_uint = 2 ** leading_one(req)
	else:
		outp_gnt_uint = 0x0
	outp_gnt = to_ubits(outp_gnt_uint, len(req))

	#next_gnt_uint = (
	#	from_ubits(x2h[len(req):2 * len(req)])
	#	| from_ubits(x2h[:len(req)])
	#	| x2h[-1]
	#)
	#next_gnt_uint = outp_gnt_uint | x1h[-1]
	#if last_gnt_uint == 0:
	#	print(psconcat(
	#		"x_uint:", bin(x_uint), " ",
	#		"x1h", x1h, " ",
	#		"x2h", x2h
	#	))
	#next_gnt = to_ubits(next_gnt_uint, len(req))
	return " ".join([
		psconcat(key, value)
		for key, value in {
			"og": outp_gnt,
			#"ng": next_gnt,
		}.items()
	])
	#return {"x": x, "x_uint": x_uint, "ng": next_gnt}

def dbg_calc_next_gnt(width: int):
	for last_gnt_uint in range(2 ** width):
		last_gnt = to_ubits(last_gnt_uint, width)
		if not is_one_hot(last_gnt):
			continue
		for req_uint in range(2 ** width):
			req = to_ubits(req_uint, width)
			print(
				psconcat(
					"lg{} rq{} {}".format(
						last_gnt, req, calc_next_gnt(
							req=req, last_gnt=last_gnt
						),
					),
					#"\n",
				)
			)
		print()

dbg_calc_next_gnt(3)
