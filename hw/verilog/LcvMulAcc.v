(* use_dsp48 = "yes" *)
module LcvMulAcc(
	input wire signed [26:0] a,
	input wire signed [17:0] b,
	input wire signed [47:0] c,
	input wire signed [47:0] d,
	input wire signed [47:0] e,
	output wire signed [47:0] outp
);
	wire signed [47:0] pcout;
	assign pcout = a * b + c;
	assign outp = pcout + d + e;
endmodule
