(* use_dsp = "yes" *)
module LcvMulAcc32(
	input wire signed [15:0] a,
	input wire signed [15:0] b,
	input wire signed [32:0] c,
	input wire signed [32:0] d,
	input wire signed [32:0] e,
	output wire signed [32:0] outp
);
	wire signed [35:0] pcout;
	assign pcout = a * b + c;
	assign outp = pcout + d + e;
endmodule
