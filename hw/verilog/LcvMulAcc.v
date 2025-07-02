`default_nettype none
(* use_dsp48 = "yes" *)
module LcvMulAcc(
	input signed [26:0] a,
	input signed [17:0] b,
	input signed [47:0] c,
	input signed [47:0] d,
	input signed [47:0] e,
	output signed [47:0] outp
);
	wire signed [47:0] pcout;
	assign pcout = a * b + c;
	assign outp = pcout + d + e;
endmodule
