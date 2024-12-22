`timescale 1ns/1ps

class TypesCpkg #(
	parameter int INNER_VEC_SIZE=3,
	parameter int OUTER_VEC_SIZE=3
);
	typedef struct {
		logic [3:0][INNER_VEC_SIZE-1:0] data;
	} vec_t;

	typedef struct {
		vec_t v[OUTER_VEC_SIZE-1:0];
	} outer_vec_t;
endclass

interface VerilatorTest1Io #(
	parameter int INNER_VEC_SIZE=8,
	parameter int OUTER_VEC_SIZE=8
)(
);
	TypesCpkg#(.INNER_VEC_SIZE, .OUTER_VEC_SIZE)::outer_vec_t in;
	TypesCpkg#(.INNER_VEC_SIZE, .OUTER_VEC_SIZE)::outer_vec_t out;
endinterface

module VerilatorTest1 #(
	parameter int INNER_VEC_SIZE,
	parameter int OUTER_VEC_SIZE
)(
	input logic clk,
	VerilatorTest1Io io
);
	always_ff @(posedge clk) begin
		for (int jdx=0; jdx<OUTER_VEC_SIZE; ++jdx) begin: outer_blk
			for (int idx=0; idx<INNER_VEC_SIZE; ++idx) begin: inner_blk
				io.out.v[jdx].data[idx] <= io.in.v[jdx].data[idx] + 1;
			end
		end
	end
endmodule

module TopLevel #(
	parameter int INNER_VEC_SIZE = 4,
	parameter int OUTER_VEC_SIZE = 4
) (
	input logic clk,
	input TypesCpkg #(.INNER_VEC_SIZE, .OUTER_VEC_SIZE)::outer_vec_t in,
	output TypesCpkg #(.INNER_VEC_SIZE, .OUTER_VEC_SIZE)::outer_vec_t out
);
	VerilatorTest1Io #(.INNER_VEC_SIZE, .OUTER_VEC_SIZE)
	dut_io();
	VerilatorTest1 #(.INNER_VEC_SIZE, .OUTER_VEC_SIZE)
	dut(
		.clk(clk),
		.io(dut_io)
	);
endmodule
