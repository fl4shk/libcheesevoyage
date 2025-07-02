(* use_dsp = "yes" *)
module LcvMulAcc32(
	input wire signed [26:0] a,
	input wire signed [17:0] b,
	input wire signed [31:0] c,
	input wire signed [31:0] d,
	input wire signed [31:0] e,
	output wire signed [31:0] outp
);
	wire signed [31:0] pcout;
	assign pcout = a * b + c;
	assign outp = pcout + d + e;
endmodule
