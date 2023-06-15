#!/usr/bin/env python3
from libcheesevoyage import *

#a = Splitrec(use_parent_name=True)
#a.arr_a = [unsigned(8)]
#a.arr_b = [signed(8)]
#a.arr_vec2 = [
#	{"x": signed(8), "y": signed(8)}
#	for i in range(4)
#]
#
#b = Splitrec.like(a, use_parent_name=True)
#b.c = Splitrec.like(
#	b.arr_vec2[0], use_parent_name=True, parent=b
#)
#c = Splitrec(use_parent_name=True)
#c.d = Splitarr.like(b.arr_vec2, use_parent_name=True)
#c.e = Splitarr.like(b.arr_vec2, use_parent_name=True, parent=c)
rgb555_layt = {"r": 5, "g": 5, "b": 5}
rgb888_layt = {"r": 8, "g": 8, "b": 8}
test_r = Splitrec(
	{
		#"r": FieldInfo(8, name="asdf_r", use_parent_name=True)
		"r": FieldInfo(8, name="asdf_r")
		#"r": FieldInfo(8, use_parent_name=True)
		"r": FieldInfo(8)
	},
		name="test_r",
		#use_parent_name=True,
		#in_like=True
	)
test_g = Splitrec({"g": FieldInfo(8)})
#vec2_layt = {"x": signed(8), "y": signed(8)}
c = Splitrec(
	{
		"rgb555": FieldInfo(rgb555_layt),
		"rgb888": rgb888_layt,
		"arr": FieldInfo([unsigned(8), unsigned(8)]),
		"arr4": FieldInfo([rgb888_layt]),
		"rgb555_2": FieldInfo([rgb555_layt]),
		"rgb555_arr": FieldInfo(
			[
				FieldInfo(rgb555_layt, True)
				for i in range(4)
			],
			False
		),
		"vec2": FieldInfo(
			{
				"x": signed(8),
				"y": signed(8),
				"rgb": {
					"r": 5,
					"g": 5,
					"b": 5,
					"z": {"x": signed(8)},
				},
				"rgb_1": FieldInfo(rgb555_layt, True),
			},
			#True,
			False,
			#name="vec2_testificate",
		),
		"view_arr": FieldInfo(ArrayLayout(signed(8), 10), True),
		"rgb_color": RgbColorLayt(5),
	},
	use_parent_name=True,
	#use_parent_name=False,
)
#c = Splitarr(
#)
#sl_blank = Blank()
#sl_blank.d = 
d = Splitrec.like(c, name="this_is_dee", use_parent_name=True)
#b = Splitarr.like(c.rgb555_arr, name="this_is_bee")
#b = Splitarr(c.rgb555_arr.shape())
#b = Splitrec.cast_elem(
#	c.rgb555_arr.shape(),
#	name="b_rgb555_arr",
#	#use_parent_name=True, 
#	#parent_name="b_rgb555_arr",
#)
#b = Splitarr(
#	[FieldInfo(unsigned(8), ]
#)

#print(a.flattened())
#print(b.flattened())
#print(c.flattened())
#for item in c.fields().items():
#	#print(item[0].extra_args_name(), item[1].flattened())
#	print(
#		#item[1].extra_args_name(),
#		item[0], item[1].flattened()
#	)
#	#print(item)
#print()
#print(c.flattened())
#do_print_flattened(c.rgb_color._View__layout)
#do_print_flattened(c.rgb_color)
do_print_flattened(test_r)
do_print_flattened(test_g)
do_print_flattened(c)
do_print_flattened(d)
#do_print_flattened(b)
